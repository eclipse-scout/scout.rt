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
import {dragAndDrop} from '../index';
import {arrays} from '../index';
import * as $ from 'jquery';

export default class DragAndDropHandler {

constructor(options) {
  options = options || {};
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
  dragAndDrop.verifyDataTransferTypesScoutTypes(event, this.supportedScoutTypes, this.dropType());
}

_onDrop(event) {
  if (this.supportedScoutTypes.indexOf(dragAndDrop.SCOUT_TYPES.FILE_TRANSFER) >= 0 &&
    this.dropType() & dragAndDrop.SCOUT_TYPES.FILE_TRANSFER === dragAndDrop.SCOUT_TYPES.FILE_TRANSFER && // NOSONAR
    dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
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
}
}
