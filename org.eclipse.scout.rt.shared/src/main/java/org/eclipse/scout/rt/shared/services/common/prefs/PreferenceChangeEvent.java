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
package org.eclipse.scout.rt.shared.services.common.prefs;

import java.util.EventObject;

/**
 * Describes a preference change.
 *
 * @since 5.1
 * @see IPreferenceChangeListener
 * @see IPreferences#addPreferenceChangeListener(IPreferenceChangeListener)
 */
public class PreferenceChangeEvent extends EventObject {

  private static final long serialVersionUID = 1L;

  private final int m_kind;
  private final String m_key;
  private final String m_oldValue;
  private final String m_newValue;

  public PreferenceChangeEvent(Object source, int kind, String key, String oldVal, String newVal) {
    super(source);
    m_kind = kind;
    m_key = key;
    m_oldValue = oldVal;
    m_newValue = newVal;
  }

  /**
   * Gets the kind of change.<br>
   * May be one of {@link Preferences#EVENT_KIND_ADD}, {@link Preferences#EVENT_KIND_CHANGE},
   * {@link Preferences#EVENT_KIND_CLEAR}, {@link Preferences#EVENT_KIND_REMOVE}, or any custom kind according to the
   * preference implementation.
   *
   * @return The change kind.
   */
  public int getKind() {
    return m_kind;
  }

  /**
   * Gets the key of the preference that has been modified.
   *
   * @return The key or null if no key is available for the event kind.
   */
  public String getKey() {
    return m_key;
  }

  /**
   * Gets the old value that was associated with the preference node before this change call.
   * 
   * @return The old (no longer valid) value.
   */
  public String getOldValue() {
    return m_oldValue;
  }

  /**
   * Gets the new value that was associated with the preference node.
   * 
   * @return The new value.
   */
  public String getNewValue() {
    return m_newValue;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName());
    sb.append(" [kind=");
    sb.append(m_kind);
    sb.append(", key='");
    sb.append(m_key);
    sb.append("', oldVal='");
    sb.append(m_oldValue);
    sb.append("', newVal='");
    sb.append(m_newValue);
    sb.append("']");
    return sb.toString();
  }
}
