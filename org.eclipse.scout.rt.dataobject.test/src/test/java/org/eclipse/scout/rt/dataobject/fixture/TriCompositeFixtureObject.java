/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.fixture;

import java.util.Objects;
import java.util.StringJoiner;

public class TriCompositeFixtureObject extends BiCompositeFixtureObject {

  private final FixtureStringId m_id3;

  protected TriCompositeFixtureObject(FixtureStringId id1, FixtureStringId id2, FixtureStringId id3) {
    super(id1, id2);
    m_id3 = id3;
  }

  public static TriCompositeFixtureObject of(FixtureStringId id1, FixtureStringId id2, FixtureStringId id3) {
    return new TriCompositeFixtureObject(id1, id2, id3);
  }

  public FixtureStringId getId3() {
    return m_id3;
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
    if (!super.equals(o)) {
      return false;
    }
    TriCompositeFixtureObject that = (TriCompositeFixtureObject) o;
    return Objects.equals(m_id3, that.m_id3);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), m_id3);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", TriCompositeFixtureObject.class.getSimpleName() + "[", "]")
        .add("m_id1=" + getId1())
        .add("m_id2=" + getId2())
        .add("m_id3=" + getId3())
        .toString();
  }
}
