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

import org.eclipse.scout.rt.shared.validate.ValidationUtility;
import org.eclipse.scout.rt.shared.validate.annotations.MinValue;

/**
 * Check implementation of {@link MinValue} annotation for method parameter or field.
 */
public class MinValueCheck implements IValidateCheck {
  public static final String ID = "minValue";

  private Object m_minValue;

  public MinValueCheck(Object minValue) {
    m_minValue = minValue;
  }

  @Override
  public String getCheckId() {
    return ID;
  }

  @Override
  public boolean accept(Object obj) {
    if (obj instanceof Comparable<?>) {
      return true;
    }
    return false;
  }

  @Override
  public void check(Object s) throws Exception {
    ValidationUtility.checkMinValue(s, m_minValue);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_minValue;
  }
}
