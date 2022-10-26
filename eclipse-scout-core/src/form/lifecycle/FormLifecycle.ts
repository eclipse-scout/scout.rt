/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Form, FormLifecycleModel, Lifecycle, scout, Status, strings, ValueField} from '../../index';
import {ValidationResult} from '../Form';

export default class FormLifecycle<VALIDATION_RESULT extends ValidationResult = ValidationResult> extends Lifecycle<VALIDATION_RESULT> implements FormLifecycleModel {
  declare model: FormLifecycleModel;
  declare widget: Form;

  constructor() {
    super();

    this.validationFailedTextKey = 'FormValidationFailedTitle';
    this.emptyMandatoryElementsTextKey = 'FormEmptyMandatoryFieldsMessage';
    this.invalidElementsTextKey = 'FormInvalidFieldsMessage';
    this.saveChangesQuestionTextKey = 'FormSaveChangesQuestion';
  }

  override init(model: FormLifecycleModel) {
    scout.assertParameter('widget', model.widget, Form);
    super.init(model);
  }

  protected _reset() {
    this.widget.visitFields(field => {
      if (field instanceof ValueField) {
        field.resetValue();
      }
    });
  }

  protected override _invalidElements(): { missingElements: VALIDATION_RESULT[]; invalidElements: VALIDATION_RESULT[] } {
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

  protected override _invalidElementText(element: VALIDATION_RESULT): string {
    return strings.plainText(element.label);
  }

  protected override _missingElementText(element: VALIDATION_RESULT): string {
    return strings.plainText(element.label);
  }

  protected override _validateWidget(): Status {
    // @ts-ignore
    return this.widget._validate();
  }

  protected override _revealInvalidElement(invalidElement: VALIDATION_RESULT) {
    this.widget.revealInvalidField(invalidElement);
  }

  override markAsSaved() {
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
  override requiresSave(): boolean {
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
