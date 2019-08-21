/*******************************************************************************
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.security;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * Levels of permissions.
 */
public final class PermissionLevel implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * Used to add levels to the system. If a level with the given {@code value} was already registered, the registration
   * is not changed. Use this method in places where the permission level is registered as a constant.
   * <p>
   * If the active flag or the text of a registered level should be changed use
   * {@link #registerOrOverride(int, String, boolean, Supplier)}.
   * <p>
   * Note that a level can not be unregistered as this might lead to illegal states. Instead it might be set to
   * inactive.
   *
   * @return new or existing permission level
   */
  public static PermissionLevel register(int value, String stringValue, boolean active, Supplier<String> textSupplier) {
    return register(value, stringValue, active, textSupplier, false);
  }

  /**
   * Used to add levels to the system or to override {@link #isActive()} and {@link #getText()} of already registered
   * permission levels.
   * <p>
   * This method should not be used in static places (). Instead it should be only call in an explicit, well defined
   * place during startup.
   */
  public static void registerOrOverride(int value, String stringValue, boolean active, Supplier<String> textSupplier) {
    register(value, stringValue, active, textSupplier, true);
  }

  private static PermissionLevel register(int value, String stringValue, boolean active, Supplier<String> textSupplier, boolean override) {
    synchronized (PermissionLevel.class) {
      PermissionLevel permissionLevel = s_singletonMap.get(value);
      if (permissionLevel == null) {
        permissionLevel = new PermissionLevel(value, stringValue, active, textSupplier);
        Map<Integer, PermissionLevel> map = new HashMap<>(s_singletonMap);
        map.put(value, permissionLevel);
        s_singletonMap = map;
      }
      else if (override) {
        // if already registered; override active flag and text
        permissionLevel.setActive(active);
        permissionLevel.setTextSupplier(textSupplier);
      }
      return permissionLevel;
    }
  }

  /**
   * @return {@link PermissionLevel} for given value
   */
  public static PermissionLevel get(int value) {
    return Assertions.assertNotNull(opt(value), "There is no PermissionLevel {} registered", value);
  }

  /**
   * @return {@link PermissionLevel} for given value or null if no such permission level was registered
   */
  public static PermissionLevel opt(int value) {
    return s_singletonMap.get(value);
  }

  /**
   * @return new modifiable set with all known permission levels
   */
  public static Set<PermissionLevel> all() {
    return new HashSet<>(s_singletonMap.values());
  }

  // synchronized by copy-on-write
  private static volatile Map<Integer, PermissionLevel> s_singletonMap = Collections.emptyMap();

  public static final int LEVEL_UNDEFINED = -1;
  public static final int LEVEL_NONE = 0;
  public static final int LEVEL_ALL = 100;

  public static final PermissionLevel UNDEFINED = register(LEVEL_UNDEFINED, "UNDEFINED", false, () -> TEXTS.get("Undefined"));
  public static final PermissionLevel NONE = register(LEVEL_NONE, "NONE", false, () -> TEXTS.get("None"));
  public static final PermissionLevel ALL = register(LEVEL_ALL, "ALL", true, () -> TEXTS.get("All"));

  private final int m_value;
  private final transient String m_stringValue;
  private transient boolean m_active; // must not be synchronized, nothing bad can happen
  private transient Supplier<String> m_textSupplier; // must not be synchronized, nothing bad can happen

  private PermissionLevel(int value, String stringValue, boolean active, Supplier<String> textSupplier) {
    m_value = value;
    m_stringValue = Assertions.assertNotNullOrEmpty(stringValue);
    m_active = active;
    m_textSupplier = Assertions.assertNotNull(textSupplier);
  }

  /**
   * @return value of this level, eg. 100 = {@link IPermissionLevel#ALL}
   */
  public int getValue() {
    return m_value;
  }

  /**
   * @return a stable string identifier
   */
  public String getStringValue() {
    return m_stringValue;
  }

  /**
   * @return if false, level is usually not visible to a human
   */
  public boolean isActive() {
    return m_active;
  }

  private void setActive(boolean active) {
    m_active = active;
  }

  /**
   * @return text used for looking up a human readable display text.
   */
  public String getText() {
    return m_textSupplier.get();
  }

  private void setTextSupplier(Supplier<String> textSupplier) {
    m_textSupplier = Assertions.assertNotNull(textSupplier);
  }

  private Object readResolve() throws ObjectStreamException {
    PermissionLevel existing = opt(m_value);
    if (existing == null) {
      return register(m_value, "UNKNOWN[" + m_value + "]", false, () -> "Unknown PermssionLevel");
    }
    else {
      return existing;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + m_value;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PermissionLevel other = (PermissionLevel) obj;
    return m_value == other.m_value;
  }

  @Override
  public String toString() {
    return m_stringValue;
  }
}
