/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Objects;

/**
 * Immutable composite object used as value in {@link RoomTypeCompositeFixtureDo}.
 *
 * @see RoomTypeCompositeFixtureDataObjectVisitorExtension
 */
public class RoomTypeCompositeFixture {

  private final RoomTypeFixtureStringId m_primaryRoomType;
  private final RoomTypeFixtureStringId m_secondaryRoomType;

  public RoomTypeCompositeFixture(RoomTypeFixtureStringId primaryRoomType, RoomTypeFixtureStringId secondaryRoomType) {
    m_primaryRoomType = assertNotNull(primaryRoomType);
    m_secondaryRoomType = assertNotNull(secondaryRoomType);
  }

  public RoomTypeFixtureStringId getPrimaryRoomType() {
    return m_primaryRoomType;
  }

  public RoomTypeFixtureStringId getSecondaryRoomType() {
    return m_secondaryRoomType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoomTypeCompositeFixture that = (RoomTypeCompositeFixture) o;
    return m_primaryRoomType.equals(that.m_primaryRoomType) && m_secondaryRoomType.equals(that.m_secondaryRoomType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(m_primaryRoomType, m_secondaryRoomType);
  }
}
