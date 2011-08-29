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
package org.eclipse.scout.rt.shared.data.basic;

import java.io.Serializable;

import org.eclipse.scout.commons.CompareUtility;

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
        for (int i = 0; i < index; i++) {
          newData[i] = m_data[i];
        }
        for (int i = index; i < newData.length; i++) {
          newData[i] = m_data[i + 1];
        }
        m_data = newData;
        m_bits = (short) (m_bits - (1 << bitPos));
      }
      return !CompareUtility.equals(oldValue, newValue);
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
        for (int i = 0; i < index; i++) {
          newData[i] = m_data[i];
        }
        newData[index] = newValue;
        for (int i = index + 1; i < newData.length; i++) {
          newData[i] = m_data[i - 1];
        }
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
}
