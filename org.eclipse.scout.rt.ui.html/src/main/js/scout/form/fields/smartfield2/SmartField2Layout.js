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
/**
 * SmartFieldLayout works like FormLayout but additionally layouts its proposal-chooser popup.
 */
scout.SmartField2Layout = function(smartField) {
  scout.SmartField2Layout.parent.call(this, smartField);
  this._smartField = smartField;
};
scout.inherits(scout.SmartField2Layout, scout.FormFieldLayout);

scout.SmartField2Layout.prototype.layout = function($container) {
  scout.SmartField2Layout.parent.prototype.layout.call(this, $container);

  // when embedded smart-field layout must not validate the popup
  // since this would lead to an endless recursion because the smart-field
  // is a child of the popup.
  if (this._smartField.embedded) {
    return;
  }

  var popup = this._smartField.popup;
  if (popup && popup.rendered) {
    // Make sure the popup is correctly layouted and positioned
    popup.position();
    popup.validateLayout();
  }
};

/**
 * Layout for icon in multiline smart-field works a bit different because the icon here is _inside_
 * an additional field container, which contains the INPUT field and the icon.
 *
 * @override FormFieldLayout.js
 */
scout.SmartField2Layout.prototype._layoutIcon = function(formField, fieldBounds, right, top) {
  var multiline = formField instanceof scout.SmartField2Multiline;
  // Cannot use field bounds because icon should have same height as input field
  var height = this._smartField.$field.outerHeight();
  formField.$icon
    .cssRight(formField.$field.cssBorderRightWidth() + (multiline ? 0 : right))
    .cssTop(top)
    .cssHeight(height)
    .cssLineHeight(height);
};
