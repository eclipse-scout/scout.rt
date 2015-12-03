/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
