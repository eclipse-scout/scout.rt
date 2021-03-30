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
  protected void initAttributeNameTranslations(Map<String, Map<String, String>> attributNameTranslations) {
    attributNameTranslations.put("charlieFixture.RoomFixture", CollectionUtility.hashMap(new ImmutablePair<>("roomName", "name")));
  }
}
