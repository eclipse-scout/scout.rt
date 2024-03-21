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
 * @since 3.8.0
 */
@EnumName("scout.UiLayer")
public enum UiLayer implements IUiLayer {

  HTML("HTML", true),
  UNKNOWN("UNKNOWN", false);

  private final boolean m_webUi;
  private final String m_stringValue;

  UiLayer(String stringValue, boolean webUi) {
    m_stringValue = stringValue;
    m_webUi = webUi;
  }

  @Override
  public boolean isWebUi() {
    return m_webUi;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  public static IUiLayer createByIdentifier(String identifier) {
    return valueOf(identifier);
  }
}
