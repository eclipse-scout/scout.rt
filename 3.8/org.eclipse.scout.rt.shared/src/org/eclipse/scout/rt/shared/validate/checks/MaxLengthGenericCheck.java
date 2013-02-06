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
import org.eclipse.scout.rt.shared.validate.annotations.MaxLength;

/**
 * Generic check implementation of {@link MaxLength} annotation for method parameter or field.
 */
public class MaxLengthGenericCheck implements IValidateCheck {
  private int m_maxLengthString;
  private int m_maxLengthCharArray;
  private int m_maxLengthByteArray;
  private int m_maxLengthOtherArray;

  public MaxLengthGenericCheck(int maxLengthString, int maxLengthCharArray, int maxLengthByteArray, int maxLengthOtherArray) {
    m_maxLengthString = maxLengthString;
    m_maxLengthCharArray = maxLengthCharArray;
    m_maxLengthByteArray = maxLengthByteArray;
    m_maxLengthOtherArray = maxLengthOtherArray;
  }

  @Override
  public String getCheckId() {
    return MaxLengthCheck.ID;
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
    if (s instanceof String) {
      ValidationUtility.checkMaxLength(s, m_maxLengthString);
    }
    else if (s.getClass() == char[].class) {
      ValidationUtility.checkMaxLength(s, m_maxLengthCharArray);
    }
    else if (s.getClass() == byte[].class) {
      ValidationUtility.checkMaxLength(s, m_maxLengthByteArray);
    }
    else {
      ValidationUtility.checkMaxLength(s, m_maxLengthOtherArray);
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " string " + m_maxLengthString + ", char[] " + m_maxLengthCharArray + ", byte[] " + m_maxLengthByteArray + ", array " + m_maxLengthOtherArray;
  }
}
