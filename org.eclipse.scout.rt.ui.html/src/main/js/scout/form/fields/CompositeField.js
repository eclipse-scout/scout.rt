/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.CompositeField = function() {
  scout.CompositeField.parent.call(this);
};
scout.inherits(scout.CompositeField, scout.FormField);

/**
 * Returns an array of child-fields.
 */
scout.CompositeField.prototype.getFields = function() {
  throw new Error('Not implemented');
};

scout.CompositeField.prototype.setFields = function() {
  throw new Error('Not implemented');
};

/**
 * @override FormField.js
 */
scout.CompositeField.prototype.visitFields = function(visitor) {
  scout.CompositeField.parent.prototype.visitFields.call(this, visitor);
  this.getFields().forEach(function(field) {
    field.visitFields(visitor);
  });
};

/**
 * Sets the given fieldStyle recursively on all fields of the composite field.
 * @override FormField.js
 */
scout.CompositeField.prototype.setFieldStyle = function(fieldStyle) {
  this.getFields().forEach(function(field) {
    field.setFieldStyle(fieldStyle);
  });
  scout.CompositeField.parent.prototype.setFieldStyle.call(this, fieldStyle);
};
