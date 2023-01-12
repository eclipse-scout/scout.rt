/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

@EnumName("scout.FixtureEnumWithCustomResolve")
public enum FixtureEnumWithCustomResolve implements IEnum {

  ONE("one"),
  TWO("two"),
  THREE("three");

  private final String m_stringValue;

  FixtureEnumWithCustomResolve(String stringValue) {
    m_stringValue = stringValue;
  }

  /**
   * Custom implemented resolve method ignoring case of given string value.
   */
  public static FixtureEnumWithCustomResolve resolve(String stringValue) {
    if (stringValue == null) {
      return null;
    }
    return valueOf(stringValue.toUpperCase());
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }
}
