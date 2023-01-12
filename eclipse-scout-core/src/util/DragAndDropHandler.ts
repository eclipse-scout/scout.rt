/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, dragAndDrop, DragAndDropOptions, DropType, DropValidationErrorMessage, FileDropEvent, files as fileUtil, MessageBoxes, MessageBoxOption, Status, Widget} from '../index';
import $ from 'jquery';

export class DragAndDropHandler {
  additionalDropProperties: (event: JQuery.DropEvent) => Record<string, string | Blob>;
  allowedTypes: () => string[];
  dropType: () => DropType;
  dropMaximumSize: () => number;
  target: Widget;
  onDrop: (data: FileDropEvent) => void;
  validateFiles: (files: File[], defaultValidator: (f: File[]) => void) => void;
  supportedScoutTypes: DropType[];
  $element: JQuery;
  selector: JQuery.Selector;
  protected _onDragEnterHandler: (event: JQuery.DragEnterEvent) => void;
  protected _onDragOverHandler: (event: JQuery.DragOverEvent) => void;
  protected _onDropHandler: (event: JQuery.DropEvent) => void;

  constructor(options?: DragAndDropOptions) {
    options = options || {} as DragAndDropOptions;
    this.additionalDropProperties = null;
    this.allowedTypes = null;
    this.dropType = null;
    this.dropMaximumSize = null;
    this.target = null;
    this.onDrop = null;
    this.validateFiles = null;
    this.selector = null;
    this.$element = null;

    $.extend(this, options);
    this.supportedScoutTypes = arrays.ensure(options.supportedScoutTypes);
    this._onDragEnterHandler = this._onDragEnter.bind(this);
    this._onDragOverHandler = this._onDragOver.bind(this);
    this._onDropHandler = this._onDrop.bind(this);
  }

  install($element: JQuery, selector: JQuery.Selector) {
    if (this.$element) {
      throw new Error('Already installed.');
    }
    this.selector = selector;
    this.$element = $element;
    this.$element.on('dragenter', this.selector, this._onDragEnterHandler)
      .on('dragover', this.selector, this._onDragOverHandler)
      .on('drop', this.selector, this._onDropHandler);
  }

  uninstall() {
    this.$element.off('dragenter', this.selector, this._onDragEnterHandler)
      .off('dragover', this.selector, this._onDragOverHandler)
      .off('drop', this.selector, this._onDropHandler);
    this.$element = null;
    this.selector = null;
  }

  protected _onDragEnter(event: JQuery.DragEnterEvent) {
    this._onDragEnterOrOver(event);
  }

  protected _onDragOver(event: JQuery.DragOverEvent) {
    this._onDragEnterOrOver(event);
  }

  protected _onDragEnterOrOver(event: JQuery.DragEventBase) {
    // set dropEffect to copy. otherwise outlook will move dropped mails into the deleted files folder.
    // see: https://bugs.chromium.org/p/chromium/issues/detail?id=322605#c33
    event.originalEvent.dataTransfer.dropEffect = 'copy';
    dragAndDrop.verifyDataTransferTypesScoutTypes(event, this.supportedScoutTypes, this.dropType());
  }

  protected _onDrop(event: JQuery.DropEvent) {
    if (this.supportedScoutTypes.indexOf(dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0 &&
      (this.dropType() & dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) === dragAndDrop.SCOUT_TYPES.FILE_TRANSFER && // NOSONAR
      dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
      if (!this.onDrop ||
        !(event.originalEvent.dataTransfer.files instanceof FileList)) {
        return;
      }
      let files = fileUtil.fileListToArray(event.originalEvent.dataTransfer.files);
      if (arrays.empty(files)) {
        return;
      }
      try {
        this.validateFiles(files, this._validateFiles.bind(this));
      } catch (error) {
        this._validationFailed(error);
        return;
      }
      event.stopPropagation();
      event.preventDefault();
      this.onDrop({
        originalEvent: event,
        files: files
      });
    }
  }

  /**
   * @throws {DropValidationErrorMessage} validationErrorMessage
   */
  private _validateFiles(files: Blob | Blob[]) {
    if (!this.dropMaximumSize) {
      return;
    }
    let dropMaximumSize = this.dropMaximumSize();
    if (!fileUtil.validateMaximumUploadSize(files, dropMaximumSize)) {
      throw {
        title: this.target.session.text('ui.FileSizeLimitTitle'),
        message: this.target.session.text('ui.FileSizeLimit', (dropMaximumSize / 1024 / 1024) + '')
      };
    }
  }

  protected _validationFailed(error: DropValidationErrorMessage): JQuery.Promise<MessageBoxOption> {
    $.log.isDebugEnabled() && $.log.debug('File validation failed', error);
    let title = '';
    let message = 'Invalid files';
    if (error) {
      title = error.title || title;
      message = error.message || message;
    }
    return MessageBoxes.createOk(this.target)
      .withSeverity(Status.Severity.ERROR)
      .withHeader(title)
      .withBody(message)
      .buildAndOpen();
  }

  uploadFiles(event: FileDropEvent) {
    if (event && event.originalEvent && event.files.length >= 1) {
      this.target.session.uploadFiles(this.target, event.files,
        this.additionalDropProperties ? this.additionalDropProperties(event.originalEvent) : undefined,
        this.dropMaximumSize ? this.dropMaximumSize() : undefined,
        this.allowedTypes ? this.allowedTypes() : undefined);
    }
  }
}
