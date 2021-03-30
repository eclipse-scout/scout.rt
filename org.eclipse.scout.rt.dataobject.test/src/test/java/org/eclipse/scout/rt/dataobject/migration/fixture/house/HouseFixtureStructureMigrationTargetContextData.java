/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.migration.DoStructureMigrationContext;
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
  public boolean initialize(DoStructureMigrationContext ctx, IDoEntity doEntity) {
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
