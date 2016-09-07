/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FileChooserField = function() {
  scout.FileChooserField.parent.call(this);
};
scout.inherits(scout.FileChooserField, scout.ValueField);

/**
 * @override Widget.js
 */
scout.FileChooserField.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.FileChooserField.prototype._render = function($parent) {
  this.addContainer($parent, 'file-chooser-field');
  this.addLabel();
  this.addMandatoryIndicator();

  this.addField(scout.fields.makeTextField($parent)
    .on('blur', this._onFieldBlur.bind(this))
    .on('dragenter', this._onDragEnterOrOver.bind(this))
    .on('dragover', this._onDragEnterOrOver.bind(this))
    .on('drop', this._onDrop.bind(this))
    .on('keydown', this._onKeydown.bind(this))
  );

  this.addIcon();
  this.addStatus();
};

scout.FileChooserField.prototype._remove = function() {
  scout.FileChooserField.parent.prototype._remove.call(this);
  if (this.$fileInputField) {
    this.$fileInputField.remove();
    this.$fileInputField = null;
  }
};

scout.FileChooserField.prototype._onDragEnterOrOver = function(event) {
  scout.dragAndDrop.verifyDataTransferTypesScoutTypes(event, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER);
};

scout.FileChooserField.prototype._onDrop = function(event) {
  if (scout.dragAndDrop.dataTransferTypesContainsScoutTypes(event.originalEvent.dataTransfer, scout.dragAndDrop.SCOUT_TYPES.FILE_TRANSFER)) {
    event.stopPropagation();
    event.preventDefault();

    var files = event.originalEvent.dataTransfer.files;
    if (files.length >= 1) {
      this.session.uploadFiles(this, [files[0]], undefined, this.maximumUploadSize);
    }
  }
};

scout.FileChooserField.prototype._onKeydown = function(event) {
  if (event.which === scout.keys.DOWN) {
    this.openFileChooser();
    return false;
  }
  return true;
};

scout.FileChooserField.prototype._onClick = function(event) {};

scout.FileChooserField.prototype._onIconClick = function(event) {
  scout.FileChooserField.parent.prototype._onIconClick.call(this, event);
  this.openFileChooser();
};

scout.FileChooserField.prototype.openFileChooser = function() {
  // FIXME CGU [6.1] offline case?
  this.trigger('chooseFile');
};
