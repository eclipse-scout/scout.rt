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
import org.eclipse.scout.rt.dataobject.migration.IDoStructureMigrationTargetContextData;

@DoStructureMigrationContextDataTarget(typeNames = {"charlieFixture.HouseFixture"})
public class HouseFixtureRawOnlyStructureMigrationTargetContextData implements IDoStructureMigrationTargetContextData {

  private String m_name;

  public String getName() {
    return m_name;
  }

  @Override
  public boolean initialize(DataObjectMigrationContext ctx, IDoEntity doEntity) {
    m_name = doEntity.getString("name");
    return true;
  }
}
