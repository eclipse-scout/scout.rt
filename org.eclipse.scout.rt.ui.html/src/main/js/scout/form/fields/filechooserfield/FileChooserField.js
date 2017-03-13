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

scout.FileChooserField.prototype._init = function(model) {
  scout.FileChooserField.parent.prototype._init.call(this, model);

  this.fileInput = scout.create('FileInput', {
    parent: this,
    acceptTypes: this.acceptTypes,
    text: this.displayText,
    enabled: this.enabled,
    maximumUploadSize: this.maximumUploadSize
  });
  this.fileInput.on('change', this._onFileChange.bind(this));
};

scout.FileChooserField.prototype._initKeyStrokeContext = function() {
  scout.FileChooserField.parent.prototype._initKeyStrokeContext.call(this);
  this.keyStrokeContext.registerKeyStroke(new scout.FileChooserFieldBrowseKeyStroke(this));
  this.keyStrokeContext.registerKeyStroke(new scout.FileChooserFieldDeleteKeyStroke(this));
};

scout.FileChooserField.prototype._render = function($parent) {
  this.addContainer($parent, 'file-chooser-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this._renderFileInput();
  this.addIcon();
  this.addStatus();
};

scout.FileChooserField.prototype._renderFileInput = function() {
  this.fileInput.render(this.$container);
  this.addField(this.fileInput.$container);
};

scout.FileChooserField.prototype.setDisplayText = function(text) {
  scout.FileChooserField.parent.prototype.setDisplayText.call(this, text);
  this.fileInput.setText(text);
};

/**
 * @override
 */
scout.FileChooserField.prototype._readDisplayText = function() {
  return this.fileInput.text;
};

scout.FileChooserField.prototype.setAcceptTypes = function(acceptTypes) {
  this.setProperty('acceptTypes', acceptTypes);
  this.fileInput.setAcceptTypes(acceptTypes);
};

scout.FileChooserField.prototype.setEnabled = function(enabled) {
  scout.FileChooserField.parent.prototype.setEnabled.call(this, enabled);
  this.fileInput.setEnabled(enabled);
};

scout.FileChooserField.prototype.setMaximumUploadSize = function(maximumUploadSize) {
  this.setProperty('maximumUploadSize', maximumUploadSize);
  this.fileInput.setMaximumUploadSize(maximumUploadSize);
};

scout.FileChooserField.prototype._onIconClick = function(event) {
  scout.FileChooserField.parent.prototype._onIconClick.call(this, event);
  this.fileInput.browse();
};

scout.FileChooserField.prototype._onFileChange = function() {
  this.acceptInput();
  this.fileInput.upload();
};
