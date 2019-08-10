/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.LabelField = function() {
  scout.LabelField.parent.call(this);
  this.htmlEnabled = false;
  this.selectable = true;
  this.wrapText = false;
};
scout.inherits(scout.LabelField, scout.ValueField);

/**
 * Resolves the text key if value contains one.
 * This cannot be done in _init because the value field would call _setValue first
 */
scout.LabelField.prototype._initValue = function(value) {
  value = scout.texts.resolveText(value, this.session.locale.languageTag);
  scout.LabelField.parent.prototype._initValue.call(this, value);
};

scout.LabelField.prototype._render = function() {
  this.addContainer(this.$parent, 'label-field');
  this.addLabel();
  this.addField(this.$parent.makeDiv());
  this.addStatus();
};

scout.LabelField.prototype._renderProperties = function() {
  scout.LabelField.parent.prototype._renderProperties.call(this);
  this._renderWrapText();
  // TODO [7.0] cgu: render selectable
};

/**
 * Since a LabelField cannot be changed by a user, acceptInput does nothing.
 * Otherwise LabelFields could 'become' touched, because value and displayText
 * of the LabelField don't match.
 */
scout.LabelField.prototype.acceptInput = function() {
  // NOP
};

scout.LabelField.prototype.setHtmlEnabled = function(htmlEnabled) {
  this.setProperty('htmlEnabled', htmlEnabled);
};

scout.LabelField.prototype._renderHtmlEnabled = function() {
  // Render the display text again when html enabled changes dynamically
  this._renderDisplayText();
};

/**
 * @override
 */
scout.LabelField.prototype._renderDisplayText = function() {
  var displayText = this.displayText || '';
  if (this.htmlEnabled) {
    this.$field.html(displayText);
  } else {
    this.$field.html(scout.strings.nl2br(displayText));
  }
  this.invalidateLayoutTree();
};

scout.LabelField.prototype.setWrapText = function(wrapText) {
  this.setProperty('wrapText', wrapText);
};

scout.LabelField.prototype._renderWrapText = function() {
  this.$field.toggleClass('white-space-nowrap', !this.wrapText);
  this.invalidateLayoutTree();
};

scout.LabelField.prototype._renderGridData = function() {
  scout.LabelField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};

scout.LabelField.prototype._renderGridDataHints = function() {
  scout.LabelField.parent.prototype._renderGridDataHints.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};
