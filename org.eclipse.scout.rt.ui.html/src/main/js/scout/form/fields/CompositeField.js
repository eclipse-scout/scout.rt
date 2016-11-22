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

scout.CompositeField.prototype.visit = function(visitor) {
  scout.CompositeField.parent.prototype.visit.call(this, visitor);
  this.getFields().forEach(function(field) {
    field.visit(visitor);
  });
};

/**
 * Sets the given displayStyle recursively on all fields of the composite field.
 * @override FormField.js
 */
scout.CompositeField.prototype.setDisabledStyle = function(disabledStyle) {
  this.getFields().forEach(function(field) {
    field.setDisabledStyle(disabledStyle);
  });
  scout.CompositeField.parent.prototype.setDisabledStyle.call(this, disabledStyle);
};

/**
 * @override FormField.js
 */
scout.CompositeField.prototype.recomputeEnabled = function(parentEnabled) {
  scout.CompositeField.parent.prototype.recomputeEnabled.call(this, parentEnabled);
  this.getFields().forEach(function(field) {
    // FIXME [awe] 6.1 - talk to M.VI, C.GU, this throws an error because of Carousel.js
    // Maybe we should move recomputeEnabled from FormField to Widget? Carousel.js is not a FormField
    // but is as a field in CarouselForm.json (jswidgets app).
    if (field instanceof scout.FormField) {
      field.recomputeEnabled(this.enabledComputed);
    }
  }, this);
};
