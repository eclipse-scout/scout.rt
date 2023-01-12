/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, dragAndDrop, DragAndDropHandler, FileInputEventMap, FileInputModel, files as fileUtil, InitModelOf, Widget} from '../index';

export class FileInput extends Widget implements FileInputModel {
  declare model: FileInputModel;
  declare eventMap: FileInputEventMap;
  declare self: FileInput;

  acceptTypes: string;
  maximumUploadSize: number;
  multiSelect: boolean;
  text: string;
  uploadController: Widget;
  files: File[];
  $fileInput: JQuery<HTMLInputElement>;
  $text: JQuery;
  dragAndDropHandler: DragAndDropHandler;

  constructor() {
    super();
    this.acceptTypes = null;
    this.maximumUploadSize = FileInput.DEFAULT_MAXIMUM_UPLOAD_SIZE;
    this.multiSelect = false;
    this.files = [];
    this.text = null;
  }

  /**
   * 50 MiB
   */
  static DEFAULT_MAXIMUM_UPLOAD_SIZE = 50 * 1024 * 1024;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.uploadController = model.uploadController || model.parent;
  }

  protected override _render() {
    this.$fileInput = this.$parent.makeElement('<input>')
      .attr('type', 'file')
      .on('change', this._onFileChange.bind(this)) as JQuery<HTMLInputElement>;
    this.$container = this.$parent.appendDiv('file-input input-field');
    this.$fileInput.appendTo(this.$container);
    this.$container.on('mousedown', this._onMouseDown.bind(this));
    this.$text = this.$container.appendDiv('file-input-text');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderText();
    this._renderAcceptTypes();
    this._renderMultiSelect();
  }

  protected override _renderEnabled() {
    super._renderEnabled();
    this._installOrUninstallDragAndDropHandler();
    this.$container.setTabbable(this.enabledComputed);
  }

  setText(text: string) {
    this.setProperty('text', text);
  }

  protected _renderText() {
    let text = this.text || '';
    this.$text.text(text);
  }

  setAcceptTypes(acceptTypes: string) {
    this.setProperty('acceptTypes', acceptTypes);
  }

  protected _renderAcceptTypes() {
    let acceptTypes = this.acceptTypes || '';
    this.$fileInput.attr('accept', acceptTypes);
  }

  setMultiSelect(multiSelect: boolean) {
    this.setProperty('multiSelect', multiSelect);
  }

  protected _renderMultiSelect() {
    this.$fileInput.prop('multiple', this.multiSelect);
  }

  setMaximumUploadSize(maximumUploadSize: number) {
    this.setProperty('maximumUploadSize', maximumUploadSize);
  }

  protected override _remove() {
    dragAndDrop.uninstallDragAndDropHandler(this);
    super._remove();
  }

  protected _installOrUninstallDragAndDropHandler() {
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
          // nop
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

  protected _setFiles(files: FileList | File[] | File) {
    let fileArray: File[];
    if (files instanceof FileList) {
      fileArray = fileUtil.fileListToArray(files);
    } else {
      fileArray = arrays.ensure(files);
    }
    if (arrays.equals(this.files, fileArray)) {
      return;
    }
    let name = '';
    if (fileArray.length > 0) {
      name = fileArray[0].name;
    }
    this.files = fileArray;
    this.setText(name);
    this.trigger('change', {
      files: fileArray
    });
  }

  upload(): boolean {
    if (this.files.length === 0) {
      return true;
    }
    return this.session.uploadFiles(this.uploadController, this.files, undefined, this.maximumUploadSize);
  }

  browse() {
    // Trigger browser's file chooser
    this.$fileInput.trigger('click');
  }

  protected _onFileChange(event: JQuery.ChangeEvent<HTMLInputElement>) {
    let files = this.$fileInput[0].files;
    if (files.length) {
      this._setFiles(files);
    }
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>) {
    if (!this.enabled) {
      return;
    }
    this.browse();
  }

  validateMaximumUploadSize(files: Blob | Blob[]) {
    if (!fileUtil.validateMaximumUploadSize(files, this.maximumUploadSize)) {
      throw this.session.text('ui.FileSizeLimit', (this.maximumUploadSize / 1024 / 1024) + '');
    }
  }
}
