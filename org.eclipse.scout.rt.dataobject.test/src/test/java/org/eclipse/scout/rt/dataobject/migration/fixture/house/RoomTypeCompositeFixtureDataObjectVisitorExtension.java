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

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.eclipse.scout.rt.dataobject.AbstractDataObjectVisitorExtension;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Handles the components within a {@link RoomTypeCompositeFixture}.
 */
public class RoomTypeCompositeFixtureDataObjectVisitorExtension extends AbstractDataObjectVisitorExtension<RoomTypeCompositeFixture> {

  @Override
  public void visit(RoomTypeCompositeFixture value, Consumer<Object> chain) {
    chain.accept(value.getPrimaryRoomType());
    chain.accept(value.getSecondaryRoomType());
  }

  @Override
  public RoomTypeCompositeFixture replaceOrVisit(RoomTypeCompositeFixture value, UnaryOperator<Object> chain) {
    RoomTypeFixtureStringId migratedPrimaryRoomType = (RoomTypeFixtureStringId) chain.apply(value.getPrimaryRoomType());
    RoomTypeFixtureStringId migratedSecondaryRoomType = (RoomTypeFixtureStringId) chain.apply(value.getSecondaryRoomType());

    if (ObjectUtility.equals(migratedPrimaryRoomType, value.getPrimaryRoomType()) && ObjectUtility.equals(migratedSecondaryRoomType, value.getSecondaryRoomType())) {
      // no changes detected: return value itself
      return value;
    }

    // create a new instance of the immutable composite object with the migrated components
    return new RoomTypeCompositeFixture(migratedPrimaryRoomType, migratedSecondaryRoomType);
  }
}
