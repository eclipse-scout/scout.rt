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
scout.FormLifecycle = function() {
  scout.FormLifecycle.parent.call(this);

  this.emptyMandatoryElementsTextKey = 'FormEmptyMandatoryFieldsMessage';
  this.invalidElementsTextKey = 'FormInvalidFieldsMessage';
};
scout.inherits(scout.FormLifecycle, scout.Lifecycle);

scout.FormLifecycle.prototype.init = function(model) {
  scout.assertParameter('widget', model.widget, scout.Form);
  scout.FormLifecycle.parent.prototype.init.call(this, model);
};

scout.FormLifecycle.prototype._reset = function() {
  this.widget.visitFields(function(field) {
    if (field instanceof scout.ValueField) {
      field.resetValue();
    }
  });
};

scout.FormLifecycle.prototype._invalidElements = function() {
  var missingFields = [];
  var invalidFields = [];

  this.widget.visitFields(function(field) {
    var result = field.getValidationResult();
    if (result.valid) {
      return;
    }
    // when mandatory is not fulfilled, do not add to invalid fields
    if (!result.validByMandatory) {
      missingFields.push(field);
      return;
    }
    if (!result.validByErrorStatus) {
      invalidFields.push(field);
      return;
    }
  });

  return {
    missingElements: missingFields,
    invalidElements: invalidFields
  };
};

scout.FormLifecycle.prototype._invalidElementText = function(element) {
  return element.label;
};

scout.FormLifecycle.prototype._missingElementText = function(element) {
  return element.label;
};

scout.FormLifecycle.prototype.markAsSaved = function() {
  this.widget.visitFields(function(field) {
    field.markAsSaved();
  });
};

/**
 * Visits all form fields and calls the updateRequiresSave() function. If any
 * field has the requiresSave flag set to true, this function returns true,
 * false otherwise.
 *
 * @see (Java) AbstractFormField #checkSaveNeeded, #isSaveNeeded
 */
scout.FormLifecycle.prototype.requiresSave = function() {
  var requiresSave = false;
  this.widget.visitFields(function(field) {
    field.updateRequiresSave();
    if (field.requiresSave) {
      requiresSave = true;
    }
  });
  return requiresSave;
};
