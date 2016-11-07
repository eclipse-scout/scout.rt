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
package org.eclipse.scout.rt.shared.data.basic;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

public class MemoryOptimizedObject implements Serializable {
  private static final long serialVersionUID = 1L;

  private short m_bits;
  private Object[] m_data;

  public MemoryOptimizedObject() {
    m_data = new Object[0];
  }

  protected synchronized boolean setValueInternal(int bitPos, Object newValue) {
    int index = getIndexFor(bitPos);
    if (index >= 0) {
      Object oldValue = m_data[index];
      if (newValue != null) {
        // replace existing object
        m_data[index] = newValue;
      }
      else {
        // remove object
        Object[] newData = new Object[m_data.length - 1];
        System.arraycopy(m_data, 0, newData, 0, index);
        System.arraycopy(m_data, index + 1, newData, index, m_data.length - index - 1);
        m_data = newData;
        m_bits = (short) (m_bits - (1 << bitPos));
      }
      return ObjectUtility.notEquals(oldValue, newValue);
    }
    else {
      if (newValue != null) {
        // add object
        int setCountBefore = 0;
        for (int i = 0; i < bitPos; i++) {
          if ((m_bits & (1 << i)) != 0) {
            setCountBefore++;
          }
        }
        index = setCountBefore;
        Object[] newData = new Object[m_data.length + 1];
        System.arraycopy(m_data, 0, newData, 0, index);
        newData[index] = newValue;
        System.arraycopy(m_data, index, newData, index + 1, m_data.length - index);
        m_data = newData;
        m_bits = (short) (m_bits | (1 << bitPos));
        return true;
      }
      else {
        // object is still null
        return false;
      }
    }
  }

  protected synchronized Object getValueInternal(int bitPos) {
    int index = getIndexFor(bitPos);
    if (index >= 0) {
      return m_data[index];
    }
    else {
      return null;
    }
  }

  private int getIndexFor(int bitPos) {
    if ((m_bits & (1 << bitPos)) != 0) {
      int setCount = 0;
      for (int i = 0; i <= bitPos; i++) {
        if ((m_bits & (1 << i)) != 0) {
          setCount++;
        }
      }
      return setCount - 1;
    }
    else {
      return -1;
    }
  }

  /**
   * Sets the value, if it is not equal to the default.
   */
  protected void setIfNotDefault(int bit, boolean value, boolean defaultValue) {
    if (value == defaultValue) {
      setValueInternal(bit, null);
    }
    else {
      setValueInternal(bit, value);
    }
  }

  /**
   * @return the value, if non-null or else the given default value.
   */
  @SuppressWarnings("unchecked")
  protected <T> T getOrElse(int valueBit, T defaultValue) {
    if (getValueInternal(valueBit) == null) {
      return defaultValue;
    }
    return (T) getValueInternal(valueBit);
  }
}
