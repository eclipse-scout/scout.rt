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
scout.SmartFieldButton = function() {
  scout.SmartFieldButton.parent.call(this);

  this._popup;
  this._smartField;
};
scout.inherits(scout.SmartFieldButton, scout.ValueField);

/**
 * @override Widget.js
 */
scout.SmartFieldButton.prototype._createKeyStrokeContext = function() {
  return new scout.InputFieldKeyStrokeContext();
};

scout.SmartFieldButton.prototype._render = function($parent) {
  var cssClass = this.proposal ? 'proposal-field' : 'smart-field';
  this.addContainer($parent, cssClass);
  this.addLabel();

  this.addField(scout.fields.makeTextField($parent)
    .attr('readonly', 'readonly')
    .click(this._onClick.bind(this)));

  this.addMandatoryIndicator();
  this.addIcon();
  this.addStatus();
  this.addSmartFieldButtonPopup();
};

scout.SmartFieldButton.prototype.addSmartFieldButtonPopup = function() {
  this._popup = scout.create('SmartFieldButtonPopup', {
    parent: this,
    $anchor: this.$field,
    smartFieldButton: this
  });
};

/**
 * Method invoked if being rendered within a cell-editor (mode='scout.FormField.MODE_CELLEDITOR'), and once the editor finished its rendering.
 */
scout.SmartFieldButton.prototype.onCellEditorRendered = function(options) {
  if (options.openFieldPopup) {
    this._onClick();
  }
};

scout.SmartFieldButton.prototype._renderProposalChooser = function() {};

/**
 * This method is called after a valid option has been selected in the proposal chooser.
 */
scout.SmartFieldButton.prototype._removeProposalChooser = function() {};

scout.SmartFieldButton.prototype._onClick = function(e) {
  if (!this._popup.rendered) {
    this._popup.open();
  }
};

/**
 * Returns the bounds of the text-input element. Subclasses may override this method when their
 * text-field is not === this.$field.
 */
scout.SmartFieldButton.prototype._fieldBounds = function() {
  return scout.graphics.offsetBounds(this.$field);
};
