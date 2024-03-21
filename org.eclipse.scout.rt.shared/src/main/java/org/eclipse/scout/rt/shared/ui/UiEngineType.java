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

/**
 * @since 6.0
 */
@EnumName("scout.UiEngineType")
public enum UiEngineType implements IUiEngineType {
  ANDROID("ANDROID"),
  CHROME("CHROME"),
  SAFARI("SAFARI"),
  FIREFOX("FIREFOX"),
  IE("IE"),
  OPERA("OPERA"),
  KONQUEROR("KONQUEROR"),
  EDGE("EDGE"),
  UNKNOWN("UNKNOWN");

  private final String m_stringValue;

  UiEngineType(String stringValue) {
    m_stringValue = stringValue;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  public static UiEngineType createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
