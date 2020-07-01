/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import org.eclipse.scout.rt.dataobject.enumeration.EnumName;
import org.eclipse.scout.rt.dataobject.enumeration.EnumVersion;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

@EnumName("scout.FixtureEnum")
@EnumVersion("scout-8.0.0.036")
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
