/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.migration.fixture.house;

import java.util.Map;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoValueMigrationHandlerByMap;
import org.eclipse.scout.rt.dataobject.migration.DoValueMigrationId;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_1;
import org.eclipse.scout.rt.platform.IgnoreBean;

@IgnoreBean
public class StreetNameStringIdValueMigrationHandlerByMap_1 extends AbstractDoValueMigrationHandlerByMap<StreetNameStringId> {

  @Override
  public DoValueMigrationId id() {
    return DoValueMigrationId.of("f2ccdfaf-e6cf-42af-963f-30aecd761d23");
  }

  @Override
  public Class<? extends ITypeVersion> typeVersionClass() {
    return CharlieFixture_1.class;
  }

  @Override
  protected Map<StreetNameStringId, StreetNameStringId> initMigrationMap() {
    return Map.of(
        StreetNameStringId.of("Old Street"), StreetNamesFixture.NEW_STREET,
        StreetNameStringId.of("Evergreen Terrace"), StreetNamesFixture.WEST_57TH_STREET
    );
  }
}
