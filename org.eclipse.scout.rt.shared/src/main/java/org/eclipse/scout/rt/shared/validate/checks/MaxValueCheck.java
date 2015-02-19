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
import org.eclipse.scout.rt.shared.validate.annotations.MaxValue;

/**
 * Check implementation of {@link MaxValue} annotation for method parameter or field.
 */
public class MaxValueCheck implements IValidateCheck {
  public static final String ID = "maxValue";

  private Object m_maxValue;

  public MaxValueCheck(Object maxValue) {
    m_maxValue = maxValue;
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
    ValidationUtility.checkMaxValue(s, m_maxValue);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + m_maxValue;
  }
}
