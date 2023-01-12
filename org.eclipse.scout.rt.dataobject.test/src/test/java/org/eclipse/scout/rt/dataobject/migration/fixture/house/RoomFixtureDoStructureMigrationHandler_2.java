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

import java.util.Map;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureRenameMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.CharlieFixtureTypeVersions.CharlieFixture_2;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;

/**
 * Changes `roomName` to `name`.
 */
@IgnoreBean
public class RoomFixtureDoStructureMigrationHandler_2 extends AbstractDoStructureRenameMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return CharlieFixture_2.class;
  }

  /**
   * References {@link RoomFixtureDo}.
   */
  @Override
  protected void initAttributeNameTranslations(Map<String, Map<String, String>> attributeNameTranslations) {
    attributeNameTranslations.put("charlieFixture.RoomFixture", CollectionUtility.hashMap(new ImmutablePair<>("roomName", "name")));
  }
}
