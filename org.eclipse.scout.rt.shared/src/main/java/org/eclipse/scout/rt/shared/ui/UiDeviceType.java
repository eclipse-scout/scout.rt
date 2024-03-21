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
@EnumName("scout.UiDeviceType")
public enum UiDeviceType implements IUiDeviceType {
  DESKTOP("DESKTOP"),
  TABLET("TABLET"),
  MOBILE("MOBILE"),
  UNKNOWN("UNKNOWN");

  private final String m_stringValue;

  UiDeviceType(String stringValue) {
    m_stringValue = stringValue;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  public static IUiDeviceType createByIdentifier(String identifier) {
    return valueOf(identifier);
  }

}
