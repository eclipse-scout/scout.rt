/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Base class for fields where the value should be visualized.
 */
scout.BeanField = function() {
  scout.BeanField.parent.call(this);

  this.preventInitialFocus = true;
};
scout.inherits(scout.BeanField, scout.ValueField);

scout.BeanField.prototype._render = function() {
  this.addContainer(this.$parent, 'bean-field');
  this.addLabel();
  this.addField(this.$parent.makeDiv());
  this.addStatus();
};

scout.BeanField.prototype._renderProperties = function() {
  scout.BeanField.parent.prototype._renderProperties.call(this);
  this._renderValue();
};

/**
 * @override FormField.js
 */
scout.BeanField.prototype._initKeyStrokeContext = function() {
  scout.BeanField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.AppLinkKeyStroke(this, this._onAppLinkAction));
};

scout.BeanField.prototype._formatValue = function(value) {
  // The value cannot be changed by the user, therefore we always return the initial displayText property.
  //
  // Strange things happen, if an other value is returned... Example:
  // 1. The value is set asynchronously on the field using setValue().
  // 2. This causes the display text to be updated (using _formatValue).
  // 3. When acceptInput() is called (via aboutToBlurByMouseDown), the "current" displayText
  //    is read using _readDisplayText(). The default ValueField.js implementation returns
  //    an empty string, which is different from this.displayText (which is equal to the value,
  //    because of step 2).
  // 4. Because the displayText has changed, parseAndSetValue() is called, which
  //    causes the value to be set to the empty string. The _renderValue() method
  //    will then most likely clear the bean field's content.
  //
  // Test case:
  //   bf.setValue({...}) --> should not update displayText property
  //   bf.acceptInput() --> should not do anything
  return this.displayText;
};

scout.BeanField.prototype._parseValue = function(displayText) {
  // DisplayText cannot be converted to value, use original value (see comment in _formatValue).
  return this.value;
};

scout.BeanField.prototype._readDisplayText = function() {
  // DisplayText cannot be changed, therefore it must be equal to the current value (see comment in _formatValue)
  return this.displayText;
};

/**
 * @override
 */
scout.BeanField.prototype._renderDisplayText = function() {
  // nop
};

scout.BeanField.prototype._renderValue = function() {
  // to be implemented by the subclass
};

scout.BeanField.prototype.triggerAppLinkAction = function(ref) {
  this.trigger('appLinkAction', {
    ref: ref
  });
};

scout.BeanField.prototype._onAppLinkAction = function(event) {
  var $target = $(event.delegateTarget);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
};
