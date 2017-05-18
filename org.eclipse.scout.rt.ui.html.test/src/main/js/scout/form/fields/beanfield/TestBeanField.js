/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TestBeanField = function() {
  scout.TestBeanField.parent.call(this);
};
scout.inherits(scout.TestBeanField, scout.BeanField);

scout.TestBeanField.prototype._render = function($parent) {
  scout.TestBeanField.parent.prototype._render.call(this, $parent);
  this.$container.addClass('test-bean-field');
};

/**
 * @override
 */
scout.TestBeanField.prototype._renderValue = function() {
  this.$field.empty();
  if (!this.value) {
    return;
  }

  this.$field.appendDiv('msg-from')
    .text('Message from ' + this.value.sender);

  this.$field.appendDiv('msg-text')
    .textOrNbsp(this.value.message);
};
