/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.DragAndDropHandler = function(options) {
  options = options || {};
  this.additionalDropProperties = null;
  this.allowedTypes = null;
  this.dropMaximumSize = null;
  this.target = null;
  this.onDrop = null;

  $.extend(this, options);
  this.supportedScoutTypes = scout.arrays.ensure(options.supportedScoutTypes);
  this._onDragEnterHandler = this._onDragEnter.bind(this);
  this._onDragOverHandler = this._onDragOver.bind(this);
  this._onDropHandler = this._onDrop.bind(this);
};

scout.DragAndDropHandler.prototype.install = function($element, selector) {
  if (this.$element) {
    throw new Error('Already installed.');
  }
  this.selector = selector;
  this.$element = $element;
  this.$element.on('dragenter', this.selector, this._onDragEnterHandler)
    .on('dragover', this.selector, this._onDragOverHandler)
    .on('drop', this.selector, this._onDropHandler);
};

scout.DragAndDropHandler.prototype.uninstall = function() {
  this.$element.off('dragenter', this.selector, this._onDragEnterHandler)
    .off('dragover', this.selector, this._onDragOverHandler)
    .off('drop', this.selector, this._onDropHandler);
  this.$element = null;
  this.selector = null;
};

scout.DragAndDropHandler.prototype._onDragEnter = function(event) {
  this._onDragEnterOrOver(event);
};

scout.DragAndDropHandler.prototype._onDragOver = function(event) {
  this._onDragEnterOrOver(event);
};

scout.DragAndDropHandler.prototype._onDragEnterOrOver = function(event) {
  // set dropEffect to copy. otherwise outlook will move dropped mails into the deleted files folder.
  // see: https://bugs.chromium.org/p/chromium/issues/detail?id=322605#c33
  event.originalEvent.dataTransfer.dropEffect = 'copy';
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, this.supportedScoutTypes, this.dropType());
};

scout.DragAndDropHandler.prototype._onDrop = function(event) {
  if (this.supportedScoutTypes.indexOf(scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0 &&
    (this.dropType() & scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) === scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER && // NOSONAR
    scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    if (!this.onDrop ||
      !(event.originalEvent.dataTransfer.files instanceof FileList)) {
      return;
    }
    var files = Array.prototype.slice.call(event.originalEvent.dataTransfer.files);
    if (scout.arrays.empty(files)) {
      return;
    }
    try {
      this.validateFiles(files);
    } catch (error) {
      this._validationFailed(files, error);
      return;
    }
    event.stopPropagation();
    event.preventDefault();
    this.onDrop({
      originalEvent: event,
      files: files
    });
  }
};

scout.DragAndDropHandler.prototype.validateFiles = function(files) {
  if (!this.dropMaximumSize) {
    return;
  }
  var dropMaximumSize = this.dropMaximumSize();
  if (!scout.files.validateMaximumUploadSize(files, dropMaximumSize)) {
    throw {
      title: this.target.session.text('ui.FileSizeLimitTitle'),
      message: this.target.session.text('ui.FileSizeLimit', (dropMaximumSize / 1024 / 1024))
    };
  }
};

/**
 * @param {object} error object containing message and optionally a title
 */
scout.DragAndDropHandler.prototype._validationFailed = function(files, error) {
  $.log.isDebugEnabled() && $.log.debug('File validation failed', error);
  var title = '';
  var message = 'Invalid files';
  if (typeof error === 'object') {
    title = error.title || title;
    message = error.message || message;
  }
  scout.MessageBoxes.createOk(this.target)
    .withSeverity(scout.Status.Severity.ERROR)
    .withHeader(title)
    .withBody(message)
    .buildAndOpen();
};

scout.DragAndDropHandler.prototype.uploadFiles = function(event) {
  if (event && event.originalEvent && event.files.length >= 1) {
    this.target.session.uploadFiles(this.target, event.files,
      this.additionalDropProperties ? this.additionalDropProperties(event.originalEvent) : undefined,
      this.dropMaximumSize ? this.dropMaximumSize() : undefined,
      this.allowedTypes ? this.allowedTypes() : undefined);
  }
};
