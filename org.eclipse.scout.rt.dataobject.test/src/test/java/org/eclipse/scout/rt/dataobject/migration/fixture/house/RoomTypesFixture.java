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
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

public interface RoomTypesFixture {
  RoomTypeFixtureStringId ROOM = RoomTypeFixtureStringId.of("room"); // was 'standard-room'
  RoomTypeFixtureStringId BEDROOM = RoomTypeFixtureStringId.of("bedroom");
  RoomTypeFixtureStringId LIVING_ROOM = RoomTypeFixtureStringId.of("living-room");
  RoomTypeFixtureStringId KITCHEN = RoomTypeFixtureStringId.of("kitchen");
}
