/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.ui;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;

@EnumName("scout.UiSystem")
public enum UiSystem implements IUiSystem {
  WINDOWS("WINDOWS"),
  UNIX("UNIX"),
  OSX("OSX"),
  IOS("IOS"),
  ANDROID("ANDROID"),
  UNKNOWN("UNKNOWN");

  private final String m_stringValue;

  UiSystem(String stringValue) {
    m_stringValue = stringValue;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  public static UiSystem createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
