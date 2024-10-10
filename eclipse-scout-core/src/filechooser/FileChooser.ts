/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, arrays, BoxButtons, ClickActiveElementKeyStroke, CloseKeyStroke, Device, DisplayParent, dragAndDrop, DragAndDropHandler, Event, FileChooserEventMap, FileChooserModel, FileInput, FileInputChangeEvent, files as fileUtil,
  FocusAdjacentElementKeyStroke, FocusRule, Form, FormLayout, GlassPaneRenderer, HtmlComponent, InitModelOf, keys, KeyStrokeContext, MessageBoxes, scout, scrollbars, Status, Widget
} from '../index';

export class FileChooser extends Widget implements FileChooserModel {
  declare model: FileChooserModel;
  declare eventMap: FileChooserEventMap;
  declare self: FileChooser;

  maximumUploadSize: number;
  acceptTypes: string;
  multiSelect: boolean;
  displayParent: DisplayParent;

  files: File[];
  boxButtons: BoxButtons;
  uploadButton: Action;
  cancelButton: Action;
  fileInput: FileInput;
  $content: JQuery;
  $title: JQuery;
  $files: JQuery<HTMLUListElement>;
  dragAndDropHandler: DragAndDropHandler;

  protected _glassPaneRenderer: GlassPaneRenderer;

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

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setDisplayParent(this.displayParent);
    this._glassPaneRenderer = new GlassPaneRenderer(this);

    this.fileInput = scout.create(FileInput, {
      parent: this,
      acceptTypes: this.acceptTypes,
      maximumUploadSize: this.maximumUploadSize,
      multiSelect: this.multiSelect,
      visible: !Device.get().supportsFile()
    });
    this.fileInput.on('change', this._onFileChange.bind(this));

    this.boxButtons = scout.create(BoxButtons, {parent: this});
    let addFileButton = this.boxButtons.addButton({text: this.session.text('ui.Browse')});
    addFileButton.on('action', event => this._onAddFileButtonClicked(event));

    this.uploadButton = this.boxButtons.addButton({
      text: this.session.text('ui.Upload'),
      enabled: false
    });
    this.uploadButton.on('action', event => this._onUploadButtonClicked(event));

    this.cancelButton = this.boxButtons.addButton({text: this.session.text('Cancel')});
    this.cancelButton.on('action', event => this._onCancelButtonClicked(event));
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStrokes([
      new FocusAdjacentElementKeyStroke(this.session, this),
      new ClickActiveElementKeyStroke(this, [keys.SPACE, keys.ENTER]),
      new CloseKeyStroke(this, (() => {
        if (!this.cancelButton) {
          return null;
        }
        return this.cancelButton.$container;
      }))
    ]);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('file-chooser')
      .on('mousedown', this._onMouseDown.bind(this));
    let $handle = this.$container.appendDiv('drag-handle');
    this.$container.draggable($handle);

    this.$content = this.$container.appendDiv('file-chooser-content');
    this.$title = this.$content.appendDiv('file-chooser-title')
      .text(this.session.text(this.multiSelect ? 'ui.ChooseFiles' : 'ui.ChooseFile'));

    this.fileInput.render(this.$content);

    // explanation for file chooser
    this.$content.appendDiv('file-chooser-label')
      .text(this.session.text('ui.FileChooserHint'));

    // List of files
    this.$files = this.$content.appendElement('<ul>', 'file-chooser-files') as JQuery<HTMLUListElement>;
    this._installScrollbars();

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

  protected override _renderProperties() {
    super._renderProperties();
    this._renderFiles();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._installOrUninstallDragAndDropHandler();
  }

  protected override _postRender() {
    super._postRender();
    this._installFocusContext();
  }

  protected override _remove() {
    this._glassPaneRenderer.removeGlassPanes();
    dragAndDrop.uninstallDragAndDropHandler(this);
    this._uninstallFocusContext();
    super._remove();
  }

  protected override _installFocusContext() {
    this.session.focusManager.installFocusContext(this.$container, FocusRule.AUTO);
  }

  protected override _uninstallFocusContext() {
    this.session.focusManager.uninstallFocusContext(this.$container);
  }

  override get$Scrollable(): JQuery {
    return this.$files;
  }

  protected _position() {
    this.$container.cssMarginLeft(-this.$container.outerWidth() / 2);
  }

  setDisplayParent(displayParent: DisplayParent) {
    this.setProperty('displayParent', displayParent);
  }

  protected _setDisplayParent(displayParent: DisplayParent) {
    this._setProperty('displayParent', displayParent);

    // Overwrite the parent with the displayParent, unless the displayParent is a descendant of the parent.
    // This is necessary to automatically remove this element when the display parent is removed.
    if (displayParent && !displayParent.isOrHas(this.parent)) {
      this.setParent(this.findDesktop().computeParentForDisplayParent(displayParent));
    }
  }

  setMaximumUploadSize(maximumUploadSize: number) {
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
    let event = this.trigger('cancel');
    if (!event.defaultPrevented) {
      this._close();
    }
  }

  /**
   * Destroys the file chooser and unlinks it from the display parent.
   */
  protected _close() {
    if (this.displayParent) {
      this.displayParent.fileChooserController.unregisterAndRemove(this);
    }
    this.destroy();
  }

  protected _installOrUninstallDragAndDropHandler() {
    dragAndDrop.installOrUninstallDragAndDropHandler(
      {
        target: this,
        onDrop: event => this.addFiles(event.files),
        dropMaximumSize: () => this.maximumUploadSize,
        // disable file validation
        validateFiles: (files, defaultValidator) => {
          // nop
        }
      });
  }

  browse() {
    this.fileInput.browse();
  }

  setAcceptTypes(acceptTypes: string) {
    this.setProperty('acceptTypes', acceptTypes);
    this.fileInput.setAcceptTypes(acceptTypes);
  }

  setMultiSelect(multiSelect: boolean) {
    this.setProperty('multiSelect', multiSelect);
    this.fileInput.setMultiSelect(multiSelect);
  }

  addFiles(files: FileList | File[] | File) {
    if (files instanceof FileList) {
      files = fileUtil.fileListToArray(files);
    }
    files = arrays.ensure(files);
    if (files.length === 0) {
      return;
    }
    if (!this.multiSelect) {
      this.setFiles([files[0]]);
    } else {
      // copy so that parameter stays untouched
      files = files.slice();
      // append new files to existing ones
      arrays.insertAll(files, this.files, 0);
      this.setFiles(files);
    }
  }

  removeFile(file: File) {
    let files = this.files.slice();
    arrays.remove(files, file);
    this.setFiles(files);
    // Clear the input, otherwise user could not choose the file which he has removed previously
    this.fileInput.clear();
  }

  setFiles(files: File[] | FileList | File) {
    if (files instanceof FileList) {
      files = fileUtil.fileListToArray(files);
    }
    files = arrays.ensure(files);

    try {
      this.fileInput.validateMaximumUploadSize(files);
    } catch (errorMessage) {
      MessageBoxes.createOk(this)
        .withHeader(this.session.text('ui.FileSizeLimitTitle'))
        .withBody(fileUtil.getErrorMessageMaximumUploadSizeExceeded(this.session, this.fileInput.maximumUploadSize))
        .withSeverity(Status.Severity.ERROR)
        .buildAndOpen();
      return;
    }

    this.setProperty('files', files);
  }

  protected _renderFiles() {
    let files = this.files;
    this.$files.empty();
    files.forEach(file => {
      let $file = this.$files.appendElement('<li>', 'file', file.name);
      // Append a space to allow the browser to break the line here when it gets too long
      $file.append(' ');
      let $remove = $file
        .appendSpan('remove')
        .on('click', this.removeFile.bind(this, file));
      let $removeLink = $file.makeElement('<a>', 'remove-link', this.session.text('Remove'));
      $remove.append($removeLink);
    });
    scrollbars.update(this.$files);
    this.uploadButton.setEnabled(files.length > 0);
  }

  protected _onUploadButtonClicked(event: Event<Action>) {
    this.trigger('upload');
  }

  protected _onCancelButtonClicked(event: Event<Action>) {
    this.cancel();
  }

  protected _onAddFileButtonClicked(event: Event<Action>) {
    this.browse();
  }

  protected _onFileChange(event: FileInputChangeEvent) {
    this.addFiles(event.files);
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    // If there is a dialog in the parent-hierarchy activate it in order to bring it on top of other dialogs.
    let parent = this.findParent(p => p instanceof Form && p.isDialog()) as Form;
    if (parent) {
      parent.activate();
    }
  }

  protected override _attach() {
    this.$parent.append(this.$container);
    super._attach();
  }

  protected override _detach() {
    this.$container.detach();
    super._detach();
  }
}
