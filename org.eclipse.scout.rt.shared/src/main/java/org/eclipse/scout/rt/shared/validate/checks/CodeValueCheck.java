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

import java.util.Set;

import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.CodeValue;

/**
 * Check implementation of {@link CodeValue} annotation for method parameter or field.
 */
public class CodeValueCheck implements IValidateCheck {
  public static final String ID = "codeValue";

  private ICodeType<?, ?> m_codeType;

  public CodeValueCheck(ICodeType<?, ?> codeType) {
    m_codeType = codeType;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    return obj != null;
  }

  @Override
  public void check(Object obj) throws Exception {
    if (obj instanceof Set<?>) {
      ValidationUtility.checkCodeTypeSet(obj, m_codeType);
    }
    else {
      ValidationUtility.checkCodeTypeValue(obj, m_codeType);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_codeType.getClass().getSimpleName();
  }
}
