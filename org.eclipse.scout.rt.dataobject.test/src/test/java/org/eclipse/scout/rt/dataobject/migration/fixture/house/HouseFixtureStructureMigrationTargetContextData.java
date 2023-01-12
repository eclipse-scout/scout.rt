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

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.DataObjectMigrationContext;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContextDataTarget;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationHelper;
import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationTargetContextData;
import org.eclipse.scout.rt.platform.BEANS;

@DoStructureMigrationContextDataTarget(doEntityClasses = {HouseFixtureDo.class}, typeNames = {"charlieFixture.HouseFixture"})
public class HouseFixtureStructureMigrationTargetContextData implements IDoStructureMigrationTargetContextData {

  private String m_name;
  private int m_numberOfRooms;

  public String getName() {
    return m_name;
  }

  public int getNumberOfRooms() {
    return m_numberOfRooms;
  }

  @Override
  public boolean initialize(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    if (doEntity instanceof HouseFixtureDo) {
      HouseFixtureDo house = (HouseFixtureDo) doEntity;
      m_name = house.getName();
      m_numberOfRooms = house.rooms().exists() ? house.getRooms().size() : 0;
      return true;
    }

    DoStructureMigrationHelper helper = BEANS.get(DoStructureMigrationHelper.class);
    String typeName = helper.getType(doEntity);
    if ("charlieFixture.HouseFixture".equals(typeName)) {
      m_name = doEntity.getString("name");
      m_numberOfRooms = doEntity.has("rooms") ? doEntity.getList("rooms").size() : 0;
      return true;
    }

    return false;
  }
}
