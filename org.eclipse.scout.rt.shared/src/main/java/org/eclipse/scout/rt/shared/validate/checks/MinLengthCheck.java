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

import java.util.Collection;
import java.util.Map;

import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.MinLength;

/**
 * Check implementation of {@link MinLength} annotation for method parameter or field.
 */
public class MinLengthCheck implements IValidateCheck {
  public static final String ID = "minLength";

  private int m_minLength;

  public MinLengthCheck(int minLength) {
    m_minLength = minLength;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    if (obj == null) {
      return false;
    }
    Class<?> c = obj.getClass();
    if (c.isArray()) {
      return true;
    }
    if (obj instanceof Collection || obj instanceof Map) {
      return true;
    }
    if (obj instanceof String) {
      return true;
    }
    return false;
  }

  @Override
  public void check(Object s) throws Exception {
    ValidationUtility.checkMinLength(s, m_minLength);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_minLength;
  }
}
