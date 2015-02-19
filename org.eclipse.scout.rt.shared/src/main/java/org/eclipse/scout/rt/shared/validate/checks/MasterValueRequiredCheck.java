/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.validate.checks;

import java.lang.reflect.Array;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.validate.DefaultValidator.FormDataCheckContext;

/**
 * Check implementation to verify that a master exists
 */
public class MasterValueRequiredCheck implements IValidateCheck {
  public static final String ID = "masterValueRequired";

  private FormDataCheckContext m_ctx;

  public MasterValueRequiredCheck(FormDataCheckContext ctx) {
    m_ctx = ctx;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    return true;
  }

  @Override
  public void check(Object s) throws Exception {
    @SuppressWarnings("unchecked")
    Class<? extends AbstractValueFieldData<?>> masterFieldClass = (Class<? extends AbstractValueFieldData<?>>) m_ctx.ruleMap.get(ValidationRule.MASTER_VALUE_FIELD);
    if (masterFieldClass == null) {
      throw new ProcessingException(m_ctx.fieldName + " missing master field");
    }
    AbstractValueFieldData<?> masterField = m_ctx.formData.getFieldByClass(masterFieldClass);
    if (masterField == null) {
      throw new ProcessingException(m_ctx.fieldName + " missing master field " + masterFieldClass.getSimpleName());
    }
    Object masterValue = masterField.getValue();
    //if master value is null, then fail
    if (masterValue == null || (masterValue.getClass().isArray() && Array.getLength(masterValue) == 0)) {
      throw new ProcessingException(m_ctx.fieldName + " slave is set but master is null: " + masterFieldClass.getSimpleName() + " -> " + m_ctx.fieldName);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_ctx.fieldName;
  }
}
