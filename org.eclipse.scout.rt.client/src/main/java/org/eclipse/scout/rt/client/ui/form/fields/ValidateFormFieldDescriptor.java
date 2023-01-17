/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields;

import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * This interface is used to check fields for valid content and - in case invalid - activate / select / focus the
 * appropriate location
 * <p>
 * see {@link IFormField#validateContent()}
 */
public class ValidateFormFieldDescriptor extends AbstractValidateContentDescriptor {
  private final IFormField m_field;

  public ValidateFormFieldDescriptor(IFormField field) {
    m_field = field;
  }

  @Override
  public String getDisplayText() {
    String displayText = super.getDisplayText();
    if (StringUtility.isNullOrEmpty(displayText)) {
      return m_field.getFullyQualifiedLabel(LABEL_SEPARATOR); // do not set default in constructor. qualified label may change
    }
    return displayText;
  }

  @Override
  public IStatus getErrorStatus() {
    return m_field.getErrorStatus();
  }

  @Override
  protected void activateProblemLocationDefault() {
    // make sure the field is showing (activate parent tabs)
    selectAllParentTabsOf(m_field);
    m_field.requestFocus();
  }
}
