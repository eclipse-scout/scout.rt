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
scout.SmartField2Multiline = function() {
  scout.SmartField2Multiline.parent.call(this);
  this.options;
  this._$multilineField;
};
scout.inherits(scout.SmartField2Multiline, scout.SmartField2);

scout.SmartField2Multiline.prototype._render = function() {
  var $field, htmlComp;

  this.addContainer(this.$parent, 'smart-field', new scout.SmartField2Layout(this));
  this.addLabel();
  this.addFieldContainer(this.$parent.makeDiv());
  htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartField2MultilineLayout());

  $field = scout.fields.makeInputOrDiv(this, 'multiline')
    .on('mousedown', this._onFieldMouseDown.bind(this))
    .appendTo(this.$fieldContainer);

  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFieldFocus.bind(this))
      .keyup(this._onFieldKeyUp.bind(this))
      .keydown(this._onFieldKeyDown.bind(this))
      .on('input', this._onInputChanged.bind(this));
  }
  this.addField($field);
  this._$multilineField = this.$fieldContainer.appendDiv('multiline-field');
  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon(this.$fieldContainer);
  this.addStatus();
};

scout.SmartField2Multiline.prototype._renderDisplayText = function() {
  scout.SmartField2Multiline.parent.prototype._renderDisplayText.call(this);
  var additionalLines = this.additionalLines();
  if (additionalLines) {
    this._$multilineField.html(scout.arrays.formatEncoded(additionalLines, '<br/>'));
  } else {
    this._$multilineField.empty();
  }
};

scout.SmartField2Multiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.graphics.offsetBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.offsetBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

scout.SmartField2Multiline.prototype._readSearchText = function() {
  // Only read the first line
  return scout.fields.valOrText(this.$field);
};

scout.SmartField2Multiline.prototype._init = function(model) {
  scout.SmartField2Multiline.parent.prototype._init.call(this, model);
  this.on('deleteProposal', this._onDeleteProposal);
};

scout.SmartField2Multiline.prototype._onDeleteProposal = function(event) {
  if (event.source.rendered) {
    event.source._$multilineField.html('');
  }
};
