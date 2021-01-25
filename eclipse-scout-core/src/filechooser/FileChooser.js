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
import {
  arrays,
  BoxButtons,
  ClickActiveElementKeyStroke,
  CloseKeyStroke,
  Device,
  dragAndDrop,
  Event,
  FileInput,
  files as fileUtil,
  FocusAdjacentElementKeyStroke,
  FocusRule,
  Form,
  FormLayout,
  GlassPaneRenderer,
  HtmlComponent,
  keys,
  KeyStrokeContext,
  MessageBoxes,
  scout,
  scrollbars,
  Status,
  Widget
} from '../index';

export default class FileChooser extends Widget {

  constructor() {
    super();
    this.displayParent = null;
    this.files = [];
    this._glassPaneRenderer = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
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
        return this.$cancelButton;
      }))
    ]);
  }

  _render() {
    this.$container = this.$parent.appendDiv('file-chooser')
      .on('mousedown', this._onMouseDown.bind(this));
    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$container.appendDiv('closable')
      .on('click', () => {
        this.cancel();
      });

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
    this.$buttons = this.$container.appendDiv('file-chooser-buttons');
    let boxButtons = new BoxButtons(this.$buttons);
    if (!this.fileInput.legacy) {
      this.$addFileButton = boxButtons.addButton({
        text: this.session.text('ui.Browse'),
        onClick: this._onAddFileButtonClicked.bind(this),
        needsClick: true
      });
    }
    this.$uploadButton = boxButtons.addButton({
      text: this.session.text('ui.Upload'),
      onClick: this._onUploadButtonClicked.bind(this),
      enabled: false
    });
    this.$cancelButton = boxButtons.addButton({
      text: this.session.text('Cancel'),
      onClick: this._onCancelButtonClicked.bind(this)
    });

    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new FormLayout(this));

    this.$container.addClassForAnimation('animate-open');
    // Prevent resizing when file chooser is dragged off the viewport
    this.$container.addClass('calc-helper');
    let windowSize = this.$container.windowSize();
    // Use css width, but ensure that it is not larger than the window (mobile)
    let w = Math.min(this.$container.width(), windowSize.width - 20);
    this.$container.css('min-width', w);
    this.$container.css('max-width', w);
    this.$container.removeClass('calc-helper');
    boxButtons.updateButtonWidths(this.$container.width());

    // Render modality glasspanes
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
    this._uninstallDragAndDropHandler();
    this._uninstallFocusContext();
    super._remove();
  }

  _installFocusContext() {
    this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
  }

  _uninstallFocusContext() {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }

  _createDragAndDropHandler() {
    return dragAndDrop.handler(this, {
      supportedScoutTypes: dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      validateFiles: () => {
      },
      onDrop: event => this.addFiles(event.files),
      dropType: () => dragAndDrop.SCOUT_TYPES.FILE_TRANSFER,
      dropMaximumSize: () => this.maximumUploadSize
    });
  }

  _installOrUninstallDragAndDropHandler() {
    if (this.enabledComputed) {
      this._installDragAndDropHandler();
    } else {
      this._uninstallDragAndDropHandler();
    }
  }

  _installDragAndDropHandler() {
    if (this.dragAndDropHandler) {
      return;
    }
    this.dragAndDropHandler = this._createDragAndDropHandler();
    if (!this.dragAndDropHandler) {
      return;
    }
    this.dragAndDropHandler.install(this.$container);
  }

  _uninstallDragAndDropHandler() {
    if (!this.dragAndDropHandler) {
      return;
    }
    this.dragAndDropHandler.uninstall();
    this.dragAndDropHandler = null;
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
    if (this.$cancelButton && this.session.focusManager.requestFocus(this.$cancelButton)) {
      this.$cancelButton.click();
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
          .appendSpan('remove menu-item')
          .on('click', this.removeFile.bind(this, file));
        let $removeLink = $file.makeElement('<a>', 'remove-link', this.session.text('Remove'));
        $remove.appendTextNode('(');
        $remove.append($removeLink);
        $remove.appendTextNode(')');
      }, this);
      scrollbars.update(this.$files);
    }
    this.$uploadButton.setEnabled(files.length > 0);
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
