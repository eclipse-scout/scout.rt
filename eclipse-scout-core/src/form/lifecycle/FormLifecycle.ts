/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, FormField, FormLifecycleModel, InitModelOf, Lifecycle, scout, Status, strings, TreeVisitResult, ValidationResult, ValueField} from '../../index';

export class FormLifecycle<TValidationResult extends ValidationResult = ValidationResult> extends Lifecycle<TValidationResult> implements FormLifecycleModel {
  declare model: FormLifecycleModel;
  declare widget: Form;

  constructor() {
    super();

    this.emptyMandatoryElementsTextKey = 'FormEmptyMandatoryFieldsMessage';
    this.invalidElementsErrorTextKey = 'FormInvalidFieldsMessage';
    this.invalidElementsWarningTextKey = 'FormInvalidFieldsWarningMessage';
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
      if (field.lifecycleBoundary) {
        return TreeVisitResult.SKIP_SUBTREE;
      }
    });
  }

  override invalidElements(): { missingElements: TValidationResult[]; invalidElements: TValidationResult[] } {
    const missingElements = [],
      invalidElements = [];

    this.widget.visitFields((field: FormField) => {
      let result = field.getValidationResult();
      if (!result.valid) {
        // error status has priority over mandatory
        if (result.errorStatus && result.errorStatus.isError()) { // ERROR
          invalidElements.push(result);
        } else if (!result.validByMandatory) { // empty mandatory
          missingElements.push(result);
        } else if (result.errorStatus && !result.errorStatus.isValid()) { // WARNING
          invalidElements.push(result);
        }
      }
      if (field.lifecycleBoundary) {
        return TreeVisitResult.SKIP_SUBTREE;
      }
      return result.visitResult;
    });

    return {missingElements, invalidElements};
  }

  protected override _invalidElementText(element: TValidationResult): string {
    const label = strings.plainText(element.label),
      message = element.errorStatus && strings.box('\'', strings.plainText(element.errorStatus.message), '\'');
    return strings.join(': ', label, message);
  }

  protected override _missingElementText(element: TValidationResult): string {
    return strings.plainText(element.label);
  }

  protected override _validateWidget(): Status | JQuery.Promise<Status> {
    return this.widget._lifecycleValidate();
  }

  protected override _handleInvalid(status: Status): JQuery.Promise<Status> {
    return this.widget._handleInvalid(status);
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
