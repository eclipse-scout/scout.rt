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

@EnumName("scout.FixtureEnum")
public enum FixtureEnum implements IEnum {

  ONE("one", "One"),
  TWO("two", "Two"),
  THREE("three", "Three");

  private final String m_stringValue;
  private final String m_text;

  FixtureEnum(String stringValue, String text) {
    m_stringValue = stringValue;
    m_text = text;
  }

  @Override
  public String stringValue() {
    return m_stringValue;
  }

  @Override
  public String text() {
    return m_text;
  }
}
