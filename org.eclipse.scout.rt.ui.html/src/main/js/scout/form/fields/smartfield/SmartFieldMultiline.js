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
scout.SmartFieldMultiline = function() {
  scout.SmartFieldMultiline.parent.call(this);
  this.options;
  this._$multilineField;
};
scout.inherits(scout.SmartFieldMultiline, scout.SmartField);

scout.SmartFieldMultiline.prototype._render = function() {
  var $field, htmlComp;

  this.addContainer(this.$parent, 'smart-field', new scout.SmartFieldLayout(this));
  this.addLabel();
  this.addFieldContainer(this.$parent.makeDiv());
  htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartFieldMultilineLayout());

  $field = scout.fields.makeInputOrDiv(this, 'multiline')
    .on('mousedown', this._onMouseDown.bind(this))
    .appendTo(this.$fieldContainer);
  if (!this.touch) {
    $field
      .blur(this._onFieldBlur.bind(this))
      .focus(this._onFocus.bind(this))
      .keyup(this._onKeyUp.bind(this))
      .keydown(this._onKeyDown.bind(this));
  }
  this.addField($field);
  this._$multilineField = this.$fieldContainer.appendDiv('multiline-field');
  if (!this.embedded) {
    this.addMandatoryIndicator();
  }
  this.addIcon(this.$fieldContainer);
  this.addStatus();
};

/**
 * @override ValueField.js
 */
scout.SmartFieldMultiline.prototype._renderDisplayText = function() {
  scout.SmartFieldMultiline.parent.prototype._renderDisplayText.call(this);
  if (this._additionalLines) {
    this._$multilineField.html(scout.arrays.formatEncoded(this._additionalLines, '<br/>'));
  } else {
    this._$multilineField.empty();
  }
};

/**
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._getInputBounds = function() {
  var fieldBounds = scout.graphics.offsetBounds(this.$fieldContainer),
    textFieldBounds = scout.graphics.offsetBounds(this.$field);
  fieldBounds.height = textFieldBounds.height;
  return fieldBounds;
};

/**
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._readSearchText = function() {
  // Only read the first line
  return scout.fields.valOrText(this.$field);
};

/**
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._init = function(model) {
  scout.SmartFieldMultiline.parent.prototype._init.call(this, model);
  this.on('deleteProposal', this._onDeleteProposal);
};

scout.SmartFieldMultiline.prototype._onDeleteProposal = function(event) {
  if (event.source.rendered) {
    event.source._$multilineField.html('');
  }
};
