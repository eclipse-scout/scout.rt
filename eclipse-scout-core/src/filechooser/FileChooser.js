/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, BoxButtons, ClickActiveElementKeyStroke, CloseKeyStroke, Device, dragAndDrop, Event, FileInput, files as fileUtil, FocusAdjacentElementKeyStroke, FocusRule, Form, FormLayout, GlassPaneRenderer, HtmlComponent, keys, KeyStrokeContext, MessageBoxes, scout, scrollbars, Status, Widget} from '../index';

export default class FileChooser extends Widget {

  constructor() {
    super();
    this.displayParent = null;
    this.files = [];
    this._glassPaneRenderer = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
    this.boxButtons = null;
    this.uploadButton = null;
    this.cancelButton = null;
    this.inheritAccessibility = false; // inherit not necessary. if the FileChooser can be opened, it must be editable. Opening a disabled chooser makes no sense.
    this._addWidgetProperties(['boxButtons', 'uploadButton', 'cancelButton']);
  }

  _init(model) {
    super._init(model);
    this._setDisplayParent(this.displayParent);
    this._glassPaneRenderer = new GlassPaneRenderer(this);

    this.fileInput = scout.create('FileInput', {
      parent: this,
      acceptTypes: this.acceptTypes,
      maximumUploadSize: this.maximumUploadSize,
      multiSelect: this.multiSelect,
      visible: !Device.get().supportsFile()
    });
    this.fileInput.on('change', this._onFileChange.bind(this));

    this.boxButtons = scout.create('BoxButtons', {parent: this});
    if (!this.fileInput.legacy) {
      let addFileButton = this.boxButtons.addButton({text: this.session.text('ui.Browse')});
      addFileButton.on('action', event => this._onAddFileButtonClicked(event));
    }

    this.uploadButton = this.boxButtons.addButton({
      text: this.session.text('ui.Upload'),
      enabled: false
    });
    this.uploadButton.on('action', event => this._onUploadButtonClicked(event));

    this.cancelButton = this.boxButtons.addButton({text: this.session.text('Cancel')});
    this.cancelButton.on('action', event => this._onCancelButtonClicked(event));
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke([
      new FocusAdjacentElementKeyStroke(this.session, this),
      new ClickActiveElementKeyStroke(this, [
        keys.SPACE, keys.ENTER
      ]),
      new CloseKeyStroke(this, (() => {
        if (!this.cancelButton) {
          return null;
        }
        return this.cancelButton.$container;
      }))
    ]);
  }

  _render() {
    this.$container = this.$parent.appendDiv('file-chooser')
      .on('mousedown', this._onMouseDown.bind(this));
    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$content = this.$container.appendDiv('file-chooser-content');
    this.$title = this.$content.appendDiv('file-chooser-title')
      .text(this.session.text(this.multiSelect ? 'ui.ChooseFiles' : 'ui.ChooseFile'));

    this.fileInput.render(this.$content);

    // DnD and Multiple files are only supported with the new file api
    if (!this.fileInput.legacy) {

      // explanation for file chooser
      this.$content.appendDiv('file-chooser-label')
        .text(this.session.text('ui.FileChooserHint'));

      // List of files
      this.$files = this.$content.appendElement('<ul>', 'file-chooser-files');
      this._installScrollbars();
    }

    // Buttons
    this.boxButtons.render();
    this.boxButtons.$container.addClass('file-chooser-buttons');

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new FormLayout(this));

    this.$container.addClassForAnimation('animate-open');
    // Prevent resizing when file chooser is dragged off the viewport
    this.$container.addClass('calc-helper');
    let windowSize = this.$container.windowSize();
    // Use css width, but ensure that it is not larger than the window (mobile)
    let w = Math.min(this.$container.width(), windowSize.width);
    this.$container.css('min-width', w);
    this.$container.css('max-width', w);
    this.$container.removeClass('calc-helper');

    // Render modality glass-panes
    this._glassPaneRenderer.renderGlassPanes();

    // Now that all texts, paddings, widths etc. are set, we can calculate the position
    this._position();
  }

  _renderProperties() {
    super._renderProperties();
    if (this.fileInput.legacy) {
      // Files may not be set into native control -> clear list in order to be sync again
      this.setFiles([]);
    }
    this._renderFiles();
  }

  _renderEnabled() {
    super._renderEnabled();
    this._installOrUninstallDragAndDropHandler();
  }

  _postRender() {
    super._postRender();
    this._installFocusContext();
  }

  _remove() {
    this._glassPaneRenderer.removeGlassPanes();
    dragAndDrop.uninstallDragAndDropHandler(this);
    this._uninstallFocusContext();
    super._remove();
  }

  _installFocusContext() {
    this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
  }

  _uninstallFocusContext() {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$files;
  }

  _position() {
    this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
  }

  setDisplayParent(displayParent) {
    this.setProperty('displayParent', displayParent);
  }

  _setDisplayParent(displayParent) {
    this._setProperty('displayParent', displayParent);
    if (displayParent) {
      this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
    }
  }

  setMaximumUploadSize(maximumUploadSize) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
    this.fileInput.setMaximumUploadSize(maximumUploadSize);
  }

  /**
   * Renders the file chooser and links it with the display parent.
   */
  open() {
    this.setDisplayParent(this.displayParent || this.session.desktop);
    this.displayParent.fileChooserController.registerAndRender(this);
  }

  /**
   * Destroys the file chooser and unlinks it from the display parent.
   */
  close() {
    if (!this.rendered) {
      this.cancel();
      return;
    }
    if (this.cancelButton && this.cancelButton.$container && this.session.focusManager.requestFocus(this.cancelButton.$container)) {
      this.cancelButton.doAction();
    }
  }

  cancel() {
    let event = new Event();
    this.trigger('cancel', event);
    if (!event.defaultPrevented) {
      this._close();
    }
  }

  /**
   * Destroys the file chooser and unlinks it from the display parent.
   */
  _close() {
    if (this.displayParent) {
      this.displayParent.fileChooserController.unregisterAndRemove(this);
    }
    this.destroy();
  }

  _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        onDrop: event => this.addFiles(event.files),
        dropMaximumSize: () => this.maximumUploadSize,
        // disable file validation
        validateFiles: (files, defaultValidator) => {
        }
      });
  }

  browse() {
    this.fileInput.browse();
  }

  setAcceptTypes(acceptTypes) {
    this.setProperty('acceptTypes', acceptTypes);
    this.fileInput.setAcceptTypes(acceptTypes);
  }

  setMultiSelect(multiSelect) {
    this.setProperty('multiSelect', multiSelect);
    this.fileInput.setMultiSelect(multiSelect);
  }

  addFiles(files) {
    if (files instanceof FileList) {
      files = fileUtil.fileListToArray(files);
    }
    files = arrays.ensure(files);
    if (files.length === 0) {
      return;
    }
    if (!this.multiSelect || this.fileInput.legacy) {
      files = [files[0]];
      this.setFiles([files[0]]);
    } else {
      // copy so that parameter stays untouched
      files = files.slice();
      // append new files to existing ones
      arrays.insertAll(files, this.files, 0);
      this.setFiles(files);
    }
  }

  removeFile(file) {
    let files = this.files.slice();
    arrays.remove(files, file);
    this.setFiles(files);
    // Clear the input, otherwise user could not choose the file which he has removed previously
    this.fileInput.clear();
  }

  setFiles(files) {
    if (files instanceof FileList) {
      files = fileUtil.fileListToArray(files);
    }
    files = arrays.ensure(files);

    try {
      this.fileInput.validateMaximumUploadSize(files);
    } catch (errorMessage) {
      MessageBoxes.createOk(this)
        .withHeader(this.session.text('ui.FileSizeLimitTitle'))
        .withBody(errorMessage)
        .withSeverity(Status.Severity.ERROR)
        .buildAndOpen();
      return;
    }

    this.setProperty('files', files);
  }

  _renderFiles() {
    let files = this.files;

    if (!this.fileInput.legacy) {
      this.$files.empty();
      files.forEach(function(file) {
        let $file = this.$files.appendElement('<li>', 'file', file.name);
        // Append a space to allow the browser to break the line here when it gets too long
        $file.append(' ');
        let $remove = $file
          .appendSpan('remove')
          .on('click', this.removeFile.bind(this, file));
        let $removeLink = $file.makeElement('<a>', 'remove-link', this.session.text('Remove'));
        $remove.append($removeLink);
      }, this);
      scrollbars.update(this.$files);
    }
    this.uploadButton.setEnabled(files.length > 0);
  }

  _onUploadButtonClicked(event) {
    this.trigger('upload');
  }

  _onCancelButtonClicked(event) {
    this.cancel();
  }

  _onAddFileButtonClicked(event) {
    this.browse();
  }

  _onFileChange(event) {
    this.addFiles(event.files);
  }

  _onMouseDown(event, option) {
    // If there is a dialog in the parent-hierarchy activate it in order to bring it on top of other dialogs.
    let parent = this.findParent(p => {
      return p instanceof Form && p.isDialog();
    });
    if (parent) {
      parent.activate();
    }
  }

  /**
   * @override Widget.js
   */
  _attach() {
    this.$parent.append(this.$container);
    super._attach();
  }

  /**
   * @override Widget.js
   */
  _detach() {
    this.$container.detach();
    super._detach();
  }
}
