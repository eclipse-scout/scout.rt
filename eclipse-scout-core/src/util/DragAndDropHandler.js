/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, dragAndDrop, files as fileUtil, MessageBoxes, Status} from '../index';
import $ from 'jquery';

export default class DragAndDropHandler {

  constructor(options) {
    options = options || {};
    this.additionalDropProperties = null;
    this.allowedTypes = null;
    this.dropMaximumSize = null;
    this.target = null;
    this.onDrop = null;
    this.validateFiles = null;

    $.extend(this, options);
    this.supportedScoutTypes = arrays.ensure(options.supportedScoutTypes);
    this._onDragEnterHandler = this._onDragEnter.bind(this);
    this._onDragOverHandler = this._onDragOver.bind(this);
    this._onDropHandler = this._onDrop.bind(this);
  }

  install($element, selector) {
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

  _onDragEnter(event) {
    this._onDragEnterOrOver(event);
  }

  _onDragOver(event) {
    this._onDragEnterOrOver(event);
  }

  _onDragEnterOrOver(event) {
    // set dropEffect to copy. otherwise outlook will move dropped mails into the deleted files folder.
    // see: https://bugs.chromium.org/p/chromium/issues/detail?id=322605#c33
    event.originalEvent.dataTransfer.dropEffect = 'copy';
    dragAndDrop.verifyDataTransferTypesScoutTypes(event, this.supportedScoutTypes, this.dropType());
  }

  _onDrop(event) {
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
   *
   * @param {File[]} files
   * @private
   * @throws {dropValidationErrorMessage} validationErrorMessage
   */
  _validateFiles(files) {
    if (!this.dropMaximumSize) {
      return;
    }
    let dropMaximumSize = this.dropMaximumSize();
    if (!fileUtil.validateMaximumUploadSize(files, dropMaximumSize)) {
      throw {
        title: this.target.session.text('ui.FileSizeLimitTitle'),
        message: this.target.session.text('ui.FileSizeLimit', (dropMaximumSize / 1024 / 1024))
      };
    }
  }

  /**
   * @param {dropValidationErrorMessage} error
   */
  _validationFailed(error) {
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

  uploadFiles(event) {
    if (event && event.originalEvent && event.files.length >= 1) {
      this.target.session.uploadFiles(this.target, event.files,
        this.additionalDropProperties ? this.additionalDropProperties(event.originalEvent) : undefined,
        this.dropMaximumSize ? this.dropMaximumSize() : undefined,
        this.allowedTypes ? this.allowedTypes() : undefined);
    }
  }

  // ----------------- TYPEDEF -----------------

  /**
   * @typedef dropValidationErrorMessage
   * @property {string} title
   * @property {string} message
   */
}
