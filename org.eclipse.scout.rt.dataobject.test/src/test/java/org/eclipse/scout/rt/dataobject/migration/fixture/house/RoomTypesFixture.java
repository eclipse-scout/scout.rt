/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

public interface RoomTypesFixture {
  RoomTypeFixtureStringId ROOM = RoomTypeFixtureStringId.of("room"); // was 'standard-room'
  RoomTypeFixtureStringId BEDROOM = RoomTypeFixtureStringId.of("bedroom");
  RoomTypeFixtureStringId LIVING_ROOM = RoomTypeFixtureStringId.of("living-room");
  RoomTypeFixtureStringId KITCHEN = RoomTypeFixtureStringId.of("kitchen");
}
