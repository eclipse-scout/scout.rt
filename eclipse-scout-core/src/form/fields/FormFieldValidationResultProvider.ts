/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {fields, FormField, InitModelOf, ObjectModel, ObjectWithType, scout, SomeRequired, Status, ValidationResult} from '../..';

export class FormFieldValidationResultProvider implements ObjectWithType {
  declare model: FormFieldValidationResultProviderModel;
  declare initModel: SomeRequired<this['model'], 'field'>;
  field: FormField;
  objectType: string;

  init(model: InitModelOf<this>) {
    scout.assertProperty(model, 'field');
    $.extend(this, model);
  }

  provide(errorStatus: Status): ValidationResult {
    const validByErrorStatus = !errorStatus || errorStatus.isValid();
    const validByMandatory = !this.field.mandatory || !this.field.empty;
    const valid = validByErrorStatus && validByMandatory;
    return {
      valid,
      validByMandatory,
      errorStatus,
      field: this.field,
      label: this.field.label,
      reveal: () => {
        fields.selectAllParentTabsOf(this.field);
        this.field.focus();
      }
    };
  }
}

export interface FormFieldValidationResultProviderModel extends ObjectModel<FormFieldValidationResultProvider> {
  field?: FormField;
}
