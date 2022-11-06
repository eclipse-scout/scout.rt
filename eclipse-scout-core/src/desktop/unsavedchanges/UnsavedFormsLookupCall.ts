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
import {Form, StaticLookupCall, UnsavedFormChangesForm, UnsavedFormsLookupCallModel} from '../../index';
import {SomeRequired} from '../../types';

export default class UnsavedFormsLookupCall extends StaticLookupCall<Form> implements UnsavedFormsLookupCallModel {
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
