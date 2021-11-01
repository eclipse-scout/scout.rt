/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Form, Lifecycle, scout, strings, ValueField} from '../../index';

export default class FormLifecycle extends Lifecycle {

  constructor() {
    super();

    this.validationFailedTextKey = 'FormValidationFailedTitle';
    this.emptyMandatoryElementsTextKey = 'FormEmptyMandatoryFieldsMessage';
    this.invalidElementsTextKey = 'FormInvalidFieldsMessage';
    this.saveChangesQuestionTextKey = 'FormSaveChangesQuestion';
  }

  init(model) {
    scout.assertParameter('widget', model.widget, Form);
    super.init(model);
  }

  _reset() {
    this.widget.visitFields(field => {
      if (field instanceof ValueField) {
        field.resetValue();
      }
    });
  }

  _invalidElements() {
    let missingFields = [];
    let invalidFields = [];

    this.widget.visitFields(field => {
      let result = field.getValidationResult();
      if (result.valid) {
        return;
      }
      // error status has priority over mandatory
      if (!result.validByErrorStatus) {
        invalidFields.push(result);
        return;
      }
      if (!result.validByMandatory) {
        missingFields.push(result);
      }
    });

    return {
      missingElements: missingFields,
      invalidElements: invalidFields
    };
  }

  _invalidElementText(element) {
    return strings.plainText(element.label);
  }

  _missingElementText(element) {
    return strings.plainText(element.label);
  }

  _validateWidget() {
    return this.widget._validate();
  }

  _revealInvalidElement(invalidElement) {
    this.widget.revealInvalidField(invalidElement);
  }

  markAsSaved() {
    this.widget.visitFields(field => {
      field.markAsSaved();
    });
  }

  /**
   * Visits all form fields and calls the updateRequiresSave() function. If any
   * field has the requiresSave flag set to true, this function returns true,
   * false otherwise.
   *
   * @see (Java) AbstractFormField #checkSaveNeeded, #isSaveNeeded
   */
  requiresSave() {
    let requiresSave = false;
    this.widget.visitFields(field => {
      field.updateRequiresSave();
      if (field.requiresSave) {
        requiresSave = true;
      }
    });
    return requiresSave;
  }
}
