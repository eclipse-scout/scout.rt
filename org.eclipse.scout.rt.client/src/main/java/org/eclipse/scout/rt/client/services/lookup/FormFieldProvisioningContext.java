/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.services.lookup;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;

public class FormFieldProvisioningContext implements IProvisioningContext {
  private final IFormField m_field;

  public FormFieldProvisioningContext(IFormField f) {
    m_field = f;
  }

  public IFormField getField() {
    return m_field;
  }
}
