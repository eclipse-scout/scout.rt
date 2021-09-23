/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, Device, dragAndDrop, files as fileUtil, InputFieldKeyStrokeContext, strings, URL, Widget} from '../index';
import $ from 'jquery';

export default class FileInput extends Widget {

  constructor() {
    super();
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
    this.multiSelect = false;
    this.files = [];
    this.legacyFileUploadUrl = null;
    this.text = null;
  }

  static DEFAULT_MAXIMUM_UPLOAD_SIZE = 50 * 1024 * 1024; // 50 MB

  _init(model) {
    super._init(model);
    this.uploadController = model.uploadController || model.parent;
    let url = new URL(model.legacyFileUploadUrl || 'upload/' + this.session.uiSessionId + '/' + this.uploadController.id);
    url.setParameter('legacy', true);
    this.legacyFileUploadUrl = url.toString();
    this.legacy = !Device.get().supportsFile();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    // Need to create keystroke context here because this.legacy is not set at the time the constructor is executed
    this.keyStrokeContext = this._createKeyStrokeContext();
    super._initKeyStrokeContext();
  }

  _createKeyStrokeContext() {
    if (this.legacy) {
      // native input control is a text field -> use input field context to make sure backspace etc. does not bubble up
      return new InputFieldKeyStrokeContext();
    }
  }

  _render() {
    this.$fileInput = this.$parent.makeElement('<input>')
      .attr('type', 'file')
      .on('change', this._onFileChange.bind(this));

    if (!this.legacy) {
      this.$container = this.$parent.appendDiv('file-input input-field');
      this.$fileInput.appendTo(this.$container);
      this.$container.on('mousedown', this._onMouseDown.bind(this));
      this.$text = this.$container.appendDiv('file-input-text');
    } else {
      this._renderLegacyMode();
    }

    if (this.legacy) {
      // Files may not be set into native control -> clear list in order to be sync again
      this.clear();
    }
  }

  _renderLegacyMode() {
    this.$legacyFormTarget = this.$fileInput.appendElement('<iframe>')
      .attr('name', 'legacyFileUpload' + this.uploadController.id)
      .on('load', () => {
        // Manually handle JSON response from iframe
        try {
          // "onAjaxDone"
          let text = this.$legacyFormTarget.contents().text();
          if (strings.hasText(text)) {
            // Manually handle JSON response
            let json = $.parseJSON(text);
            this.session.responseQueue.process(json);
          }
        } finally {
          // "onAjaxAlways"
          this.session.setBusy(false);
        }
      });
    this.$fileInput
      .attr('name', 'file')
      .addClass('legacy-upload-file-input');
    this.$legacyForm = this.$parent.appendElement('<form>', 'legacy-upload-form')
      .attr('action', this.legacyFileUploadUrl)
      .attr('enctype', 'multipart/form-data')
      .attr('method', 'post')
      .attr('target', 'legacyFileUpload' + this.uploadController.id)
      .append(this.$fileInput);
    this.$container = this.$legacyForm;
  }

  _renderProperties() {
    super._renderProperties();
    this._renderText();
    this._renderAcceptTypes();
    this._renderMultiSelect();
  }

  _renderEnabled() {
    super._renderEnabled();
    this._installOrUninstallDragAndDropHandler();

    if (this.legacy) {
      this.$fileInput.setEnabled(this.enabledComputed);
    } else {
      this.$container.setTabbable(this.enabledComputed);
    }
  }

  setText(text) {
    this.setProperty('text', text);
  }

  _renderText() {
    if (this.legacy) {
      return;
    }
    let text = this.text || '';
    this.$text.text(text);
  }

  setAcceptTypes(acceptTypes) {
    this.setProperty('acceptTypes', acceptTypes);
  }

  _renderAcceptTypes() {
    let acceptTypes = this.acceptTypes || '';
    this.$fileInput.attr('accept', acceptTypes);
  }

  setMultiSelect(multiSelect) {
    this.setProperty('multiSelect', multiSelect);
  }

  _renderMultiSelect() {
    this.$fileInput.prop('multiple', this.multiSelect);
  }

  setMaximumUploadSize(maximumUploadSize) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
  }

  _remove() {
    dragAndDrop.uninstallDragAndDropHandler(this);
    super._remove();
  }

  _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        onDrop: event => {
          if (event.files.length >= 1) {
            this._setFiles(event.files);
          }
        },
        dropMaximumSize: () => this.maximumUploadSize,
        // disable file validation
        validateFiles: (files, defaultValidator) => {
        }
      });
  }

  clear() {
    this._setFiles([]);
    // _setFiles actually sets the text as well, but only if files have changed.
    // Make sure text is cleared as well if there are no files but a text set.
    this.setText(null);
    if (this.rendered) {
      this.$fileInput.val(null);
    }
  }

  _setFiles(files) {
    if (files instanceof FileList) {
      files = fileUtil.fileListToArray(files);
    }
    files = arrays.ensure(files);
    if (arrays.equals(this.files, files)) {
      return;
    }
    let name = '';
    if (files.length > 0) {
      if (this.legacy) {
        name = files[0];
      } else {
        name = files[0].name;
      }
    }
    this.files = files;
    this.setText(name);
    this.trigger('change', {
      files: files
    });
  }

  upload() {
    if (this.files.length === 0) {
      return true;
    }
    if (!this.legacy) {
      return this.session.uploadFiles(this.uploadController, this.files, undefined, this.maximumUploadSize);
    }
    this.session.setBusy(true);
    this.$legacyForm[0].submit();
    return true;
  }

  browse() {
    // Trigger browser's file chooser
    this.$fileInput.click();
  }

  _onFileChange(event) {
    let files = [];

    if (!this.legacy) {
      files = this.$fileInput[0].files;
    } else {
      if (this.$fileInput[0].value) {
        files.push(this.$fileInput[0].value);
      }
    }
    if (files.length) {
      this._setFiles(files);
    }
  }

  _onMouseDown() {
    if (!this.enabled) {
      return;
    }
    this.browse();
  }

  /**
   * @deprecated use files.fileListToArray instead
   */
  static fileListToArray(fileList) {
    return fileUtil.fileListToArray(fileList);
  }

  validateMaximumUploadSize(files) {
    if (!fileUtil.validateMaximumUploadSize(files, this.maximumUploadSize)) {
      throw this.session.text('ui.FileSizeLimit', (this.maximumUploadSize / 1024 / 1024));
    }
  }
}
