/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.api.data.prefs.migration;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.ScoutTypeVersions.Scout_25_2_002__uiPreferences;
import org.eclipse.scout.rt.dataobject.migration.AbstractDoStructureMigrationHandlerTest;
import org.junit.Test;

/**
 * Test for {@link UiPreferencesAddTypeVersionMigrationHandler_25_2_002}
 */
public class UiPreferencesAddTypeVersionMigrationHandler_25_2_002_Test extends AbstractDoStructureMigrationHandlerTest {

  @Test
  public void testMigration() throws IOException {
    testMigration("UiPreferences", "noVersion", Scout_25_2_002__uiPreferences.class);
  }
}
