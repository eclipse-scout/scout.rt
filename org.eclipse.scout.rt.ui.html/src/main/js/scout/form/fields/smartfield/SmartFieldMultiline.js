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

scout.SmartFieldMultiline.prototype._render = function($parent) {
  var $field, htmlComp;

  this.addContainer($parent, 'smart-field', new scout.SmartFieldLayout(this));
  this.addLabel();
  this.addFieldContainer($parent.makeDiv());
  htmlComp = new scout.HtmlComponent(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.SmartFieldMultilineLayout());

  $field = scout.fields.makeInputOrDiv(this, 'multiline')
    .click(this._onClick.bind(this))
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
  this.addPopup();
};

/**
 * @override ValueField.js
 */
scout.SmartFieldMultiline.prototype._renderDisplayText = function() {
  var tmp = this.displayText.split('\n'),
    firstLine = tmp.shift(),
    additionalLines = scout.arrays.formatEncoded(tmp, '<br/>');
  scout.fields.valOrText(this, this.$field, firstLine);
  this._$multilineField.html(additionalLines);
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
 * Concatenates the text in the input-field and from the other additional lines in model.displayText
 * (which are displayed in the text-box below the input-field).
 *
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._readDisplayText = function() {
  var i,
    firstLine = scout.fields.valOrText(this, this.$field),
    newDisplayText = [firstLine],
    oldDisplayText = this.displayText.split('\n');
  for (i = 1; i < oldDisplayText.length; i++) {
    newDisplayText.push(oldDisplayText[i]);
  }
  return newDisplayText.join('\n');
};

/**
 * @override SmartField.js
 */
scout.SmartFieldMultiline.prototype._readSearchText = function() {
  // Only read the first line
  return scout.fields.valOrText(this, this.$field);
};
