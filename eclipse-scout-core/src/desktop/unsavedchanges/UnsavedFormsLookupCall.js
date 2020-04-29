/*
 * Copyright (c) 2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {StaticLookupCall, UnsavedFormChangesForm} from '../../index';

export default class UnsavedFormsLookupCall extends StaticLookupCall {

  constructor() {
    super();

    this.unsavedForms = [];
  }

  _data() {
    return this.unsavedForms.map(form => {
      let text = UnsavedFormChangesForm.getFormDisplayName(form);
      return [form, text];
    });
  }
}
