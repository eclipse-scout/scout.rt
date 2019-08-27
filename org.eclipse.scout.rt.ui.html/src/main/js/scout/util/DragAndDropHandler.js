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
scout.DragAndDropHandler = function(options) {
  options = options || {};
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
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, this.supportedScoutTypes, this.dropType());
};

scout.DragAndDropHandler.prototype._onDrop = function(event) {
  if (this.supportedScoutTypes.indexOf(scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0 &&
    this.dropType() & scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER === scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER && // NOSONAR
    scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    event.stopPropagation();
    event.preventDefault();

    var files = event.originalEvent.dataTransfer.files;
    if (files.length >= 1) {
      this.target.session.uploadFiles(this.target, files,
        this.additionalDropProperties ? this.additionalDropProperties(event) : undefined,
        this.dropMaximumSize ? this.dropMaximumSize() : undefined,
        this.allowedTypes ? this.allowedTypes() : undefined);
    }
  }
};
