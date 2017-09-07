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
    enabled: this.enabledComputed,
    maximumUploadSize: this.maximumUploadSize
  });
  this.fileInput.on('change', this._onFileChange.bind(this));
  this.on('propertyChange', function(event) {
    if (event.propertyName === 'enabledComputed') {
      // Propagate "enabledComputed" to inner widget
      this.fileInput.setEnabled(event.newValue);
    }
  }.bind(this));
};

scout.FileChooserField.prototype._initKeyStrokeContext = function() {
  scout.FileChooserField.parent.prototype._initKeyStrokeContext.call(this);
  if (!this.fileInput.legacy) {
    this.keyStrokeContext.registerKeyStroke(new scout.FileChooserFieldBrowseKeyStroke(this));
    this.keyStrokeContext.registerKeyStroke(new scout.FileChooserFieldDeleteKeyStroke(this));
  }
};

scout.FileChooserField.prototype._render = function() {
  this.addContainer(this.$parent, 'file-chooser-field has-icon');
  this.addLabel();
  this.addMandatoryIndicator();
  this._renderFileInput();
  this.$field
    .on('focus', this._onFieldFocus.bind(this))
    .on('blur', this._onFieldBlur.bind(this));
  this.addIcon();
  this.addClearIcon();
  this.addStatus();
};

scout.FileChooserField.prototype._renderProperties = function() {
  scout.FileChooserField.parent.prototype._renderProperties.call(this);
  this._renderClearable();
};

scout.FileChooserField.prototype._renderFileInput = function() {
  this.fileInput.render();
  this.addField(this.fileInput.$container);
};

scout.FileChooserField.prototype.setDisplayText = function(text) {
  scout.FileChooserField.parent.prototype.setDisplayText.call(this, text);
  this.fileInput.setText(text);
  if (!text) {
    this.fileInput.clear();
  }
};

/**
 * @override
 */
scout.FileChooserField.prototype._readDisplayText = function() {
  return this.fileInput.text;
};

/**
 * @override
 */
scout.FileChooserField.prototype._renderDisplayText = function() {
  scout.FileChooserField.parent.prototype._renderDisplayText.call(this);
  this._updateClearable();
};

scout.FileChooserField.prototype.setAcceptTypes = function(acceptTypes) {
  this.setProperty('acceptTypes', acceptTypes);
  this.fileInput.setAcceptTypes(acceptTypes);
};

scout.FileChooserField.prototype._renderEnabled = function() {
  scout.FileChooserField.parent.prototype._renderEnabled.call(this);
  this.$field.setTabbable(this.enabledComputed);
};

scout.FileChooserField.prototype.setMaximumUploadSize = function(maximumUploadSize) {
  this.setProperty('maximumUploadSize', maximumUploadSize);
  this.fileInput.setMaximumUploadSize(maximumUploadSize);
};

scout.FileChooserField.prototype._updateClearable = function() {
  this.setClearable(!this.fileInput.legacy && scout.strings.hasText(this._readDisplayText()) && this.focused);
};

scout.FileChooserField.prototype._clear = function() {
  this.fileInput.clear();
};

scout.FileChooserField.prototype.setFocused = function(focused) {
  this.setProperty('focused', focused);
};

scout.FileChooserField.prototype._renderFocused = function() {
  this._updateClearable();
};

scout.FileChooserField.prototype._onFieldBlur = function(event) {
  scout.FileChooserField.parent.prototype._onFieldBlur.call(this, event);
  this.setFocused(false);
};

scout.FileChooserField.prototype._onFieldFocus = function(event) {
  this.setFocused(true);
};

scout.FileChooserField.prototype._onIconMouseDown = function(event) {
  scout.FileChooserField.parent.prototype._onIconMouseDown.call(this, event);
  this.fileInput.browse();
};

scout.FileChooserField.prototype._onFileChange = function() {
  this.acceptInput();
  var success = this.fileInput.upload();
  if (!success) {
    this.fileInput.clear();
  }
  this._updateClearable();
};
