/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, FormLifecycleModel, InitModelOf, Lifecycle, scout, Status, strings, ValidationResult, ValueField} from '../../index';

export class FormLifecycle<TValidationResult extends ValidationResult = ValidationResult> extends Lifecycle<TValidationResult> implements FormLifecycleModel {
  declare model: FormLifecycleModel;
  declare widget: Form;

  constructor() {
    super();

    this.validationFailedTextKey = 'FormValidationFailedTitle';
    this.emptyMandatoryElementsTextKey = 'FormEmptyMandatoryFieldsMessage';
    this.invalidElementsTextKey = 'FormInvalidFieldsMessage';
    this.saveChangesQuestionTextKey = 'FormSaveChangesQuestion';
  }

  override init(model: InitModelOf<this>) {
    scout.assertParameter('widget', model.widget, Form);
    super.init(model);
  }

  protected override _reset() {
    this.widget.visitFields(field => {
      if (field instanceof ValueField) {
        field.resetValue();
      }
    });
  }

  override invalidElements(): { missingElements: TValidationResult[]; invalidElements: TValidationResult[] } {
    let missingFields = [];
    let invalidFields = [];

    this.widget.visitFields(field => {
      let result = field.getValidationResult();
      if (result.valid) {
        return result.visitResult;
      }
      // error status has priority over mandatory
      if (!result.validByErrorStatus) {
        invalidFields.push(result);
      } else if (!result.validByMandatory) {
        missingFields.push(result);
      }
      return result.visitResult;
    });

    return {
      missingElements: missingFields,
      invalidElements: invalidFields
    };
  }

  protected override _invalidElementText(element: TValidationResult): string {
    return strings.plainText(element.label);
  }

  protected override _missingElementText(element: TValidationResult): string {
    return strings.plainText(element.label);
  }

  protected override _validateWidget(): Status {
    return this.widget._validate();
  }

  protected override _revealInvalidElement(invalidElement: TValidationResult) {
    this.widget.revealInvalidField(invalidElement);
  }

  override markAsSaved() {
    this.widget.markAsSaved();
  }

  override saveNeeded(): boolean {
    return this.widget.saveNeeded;
  }
}
