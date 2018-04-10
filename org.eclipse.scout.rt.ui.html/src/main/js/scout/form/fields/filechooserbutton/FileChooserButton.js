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
scout.FileChooserButton = function() {
  scout.FileChooserButton.parent.call(this);

  this.button = null;
  this.fileInput = null;
};
scout.inherits(scout.FileChooserButton, scout.ValueField);

scout.FileChooserButton.prototype._init = function(model) {
  scout.FileChooserButton.parent.prototype._init.call(this, model);

  this.button = scout.create('Button', {
    parent: this,
    label: this._buttonLabel(),
    iconId : this.iconId
  });

  this.fileInput = scout.create('FileInput', {
    parent: this,
    acceptTypes: this.acceptTypes,
    text: this.displayText,
    enabled: this.enabledComputed,
    maximumUploadSize: this.maximumUploadSize,
    visible: !scout.device.supportsFile()
  });
  this.fileInput.on('change', this._onFileChange.bind(this));
  this.on('propertyChange', function(event) {
    if (event.propertyName === 'enabledComputed') {
      // Propagate "enabledComputed" to inner widgets
      this.button.setEnabled(event.newValue);
      this.fileInput.setEnabled(event.newValue);
    }
  }.bind(this));
};

scout.FileChooserButton.prototype._buttonLabel = function() {
  return scout.strings.hasText(this.label) ? this.label : '%Upload'; // FIXME [awe] i18n text
};

scout.FileChooserButton.prototype._render = function() {
  this.addContainer(this.$parent, 'file-chooser-button has-icon');
  this.addLabel();
//  this.addMandatoryIndicator();

  var $field = this.$parent.makeDiv();
  var fieldHtmlComp = scout.HtmlComponent.install($field, this.session);
  this.button.render($field);
  this.button.on('click', this._onButtonClick.bind(this));
  fieldHtmlComp.setLayout(new scout.SingleLayout(this.button.htmlComp));
  this.fileInput.render($field);
  this.addField($field);
  $field.setTabbable(false);

  this.addIcon();
  this.addStatus();
};

scout.FileChooserButton.prototype._onButtonClick = function(event) {
  this.fileInput.browse();
};

scout.FileChooserButton.prototype.setLabel = function(label) {
  scout.FileChooserButton.parent.prototype.setLabel.call(this, label);
  this.button.setLabel(this._buttonLabel());
};

scout.FileChooserButton.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
  this.button.setIconId(iconId);
};

scout.FileChooserButton.prototype.setAcceptTypes = function(acceptTypes) {
  this.setProperty('acceptTypes', acceptTypes);
  this.fileInput.setAcceptTypes(acceptTypes);
};

scout.FileChooserButton.prototype.setMaximumUploadSize = function(maximumUploadSize) {
  this.setProperty('maximumUploadSize', maximumUploadSize);
  this.fileInput.setMaximumUploadSize(maximumUploadSize);
};

scout.FileChooserButton.prototype._onFileChange = function() {
  var success = this.fileInput.upload();
  if (!success) {
    this.fileInput.clear();
  }
};
