/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

/**
 * Represents a value which can be marked as preferred value, meaning that it can only be overwritten by respective
 * preferred values.
 */
public class PreferredValue<VALUE> {
  private VALUE m_value;
  private boolean m_preferred;

  public PreferredValue(final VALUE value, final boolean preferred) {
    m_value = value;
    m_preferred = preferred;
  }

  /**
   * Sets the current value to the given value. If the current value itself is a preferred value, it can only be
   * overwritten by respective preferred values.
   *
   * @param value
   *          the value to be set.
   * @param preferred
   *          <code>true</code> to set the given value as preferred value. If <code>false</code>, the value is only set
   *          if the current value is not a preferred value yet.
   * @return <code>true</code> if the current value was overwritten.
   */
  public boolean set(final VALUE value, final boolean preferred) {
    if (preferred) {
      m_preferred = true;
    }

    if (preferred || !m_preferred) {
      m_value = value;
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * @return the current value.
   */
  public VALUE get() {
    return m_value;
  }

  /**
   * @return <code>true</code> if the current value is marked as preferred value.
   */
  public boolean isPreferredValue() {
    return m_preferred;
  }

  /**
   * Marks the current value as a preferred value, meaning that it is not overwritten when setting a non-preferred
   * value.
   */
  public void markAsPreferredValue() {
    m_preferred = true;
  }

  /**
   * @return a shallow copy of <code>this</code> PreferredValue.
   */
  public PreferredValue<VALUE> copy() {
    return new PreferredValue<>(m_value, m_preferred);
  }
}
