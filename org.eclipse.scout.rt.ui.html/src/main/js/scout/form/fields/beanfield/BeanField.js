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
 * Base class for fields where the value should be visualized.
 */
scout.BeanField = function() {
  scout.BeanField.parent.call(this);
};
scout.inherits(scout.BeanField, scout.ValueField);

scout.BeanField.prototype._render = function($parent) {
  this.addContainer($parent, 'bean-field');
  this.addLabel();
  this.addField($parent.makeDiv());
  this.addStatus();
};

scout.BeanField.prototype._renderProperties = function() {
  scout.BeanField.parent.prototype._renderProperties.call(this);
  this._renderValue(this.value);
};

/**
 * @override FormField.js
 */
scout.BeanField.prototype._initKeyStrokeContext = function() {
  scout.BeanField.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.AppLinkKeyStroke(this, this._onAppLinkAction));
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
  var $target = $(event.target);
  var ref = $target.data('ref');
  this.triggerAppLinkAction(ref);
};
