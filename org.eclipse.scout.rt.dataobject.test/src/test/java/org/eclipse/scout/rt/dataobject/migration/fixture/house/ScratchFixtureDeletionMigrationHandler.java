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

import java.util.Collections;
import java.util.Set;

import org.eclipse.scout.rt.dataobject.ITypeVersion;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoDeletionMigrationHandler;
import org.eclipse.scout.rt.dataobject.migration.fixture.version.BravoFixtureTypeVersions.BravoFixture_2;
import org.eclipse.scout.rt.platform.IgnoreBean;

@IgnoreBean
public class ScratchFixtureDeletionMigrationHandler extends AbstractDoDeletionMigrationHandler {

  @Override
  public Class<? extends ITypeVersion> toTypeVersionClass() {
    return BravoFixture_2.class;
  }

  @Override
  public Set<String> getTypeNames() {
    return Collections.singleton("bravoFixture.ScratchFixture");
  }
}
