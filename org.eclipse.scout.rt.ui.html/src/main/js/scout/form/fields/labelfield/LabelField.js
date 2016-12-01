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
scout.LabelField = function() {
  scout.LabelField.parent.call(this);
  this.htmlEnabled = false;
  this.selectable = true;
  this.wrapText = false;
};
scout.inherits(scout.LabelField, scout.ValueField);

scout.LabelField.prototype._render = function($parent) {
  this.addContainer($parent, 'label-field');
  this.addLabel();
  this.addField($parent.makeDiv());
  this.addStatus();
};

scout.LabelField.prototype._renderProperties = function() {
  scout.LabelField.parent.prototype._renderProperties.call(this);
  this._renderWrapText(this.wrapText);
  // FIXME cgu: render selectable
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
};

scout.LabelField.prototype._renderWrapText = function() {
  this.$field.toggleClass('white-space-nowrap', !this.wrapText);
};

scout.LabelField.prototype._renderGridData = function() {
  scout.LabelField.parent.prototype._renderGridData.call(this);
  this.updateInnerAlignment({
    useHorizontalAlignment: true
  });
};
