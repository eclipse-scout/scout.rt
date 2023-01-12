/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Form, SomeRequired, StaticLookupCall, UnsavedFormChangesForm, UnsavedFormsLookupCallModel} from '../../index';

export class UnsavedFormsLookupCall extends StaticLookupCall<Form> implements UnsavedFormsLookupCallModel {
  declare model: UnsavedFormsLookupCallModel;
  declare initModel: SomeRequired<this['model'], 'session' | 'unsavedForms'>;

  unsavedForms: Form[];

  constructor() {
    super();
    this.unsavedForms = [];
  }

  protected override _data(): any {
    return this.unsavedForms.map(form => {
      let text = UnsavedFormChangesForm.getFormDisplayName(form);
      return [form, text];
    });
  }
}
