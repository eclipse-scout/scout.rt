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

import java.util.Objects;
import java.util.StringJoiner;

public class BiCompositeFixtureObject {

  private final FixtureStringId m_id1;
  private final FixtureStringId m_id2;

  protected BiCompositeFixtureObject(FixtureStringId id1, FixtureStringId id2) {
    m_id1 = id1;
    m_id2 = id2;
  }

  public static BiCompositeFixtureObject of(FixtureStringId id1, FixtureStringId id2) {
    return new BiCompositeFixtureObject(id1, id2);
  }

  public FixtureStringId getId1() {
    return m_id1;
  }

  public FixtureStringId getId2() {
    return m_id2;
  }

  // required for Junit assert
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BiCompositeFixtureObject that = (BiCompositeFixtureObject) o;
    return Objects.equals(m_id1, that.m_id1) && Objects.equals(m_id2, that.m_id2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_id1, m_id2);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", BiCompositeFixtureObject.class.getSimpleName() + "[", "]")
        .add("m_id1=" + m_id1)
        .add("m_id2=" + m_id2)
        .toString();
  }
}
