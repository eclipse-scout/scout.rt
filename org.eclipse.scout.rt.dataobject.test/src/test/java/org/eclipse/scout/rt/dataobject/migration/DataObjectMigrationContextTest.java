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
package org.eclipse.scout.rt.dataobject.migration;

import static org.junit.Assert.*;

import org.eclipse.scout.rt.dataobject.migration.fixture.house.HouseFixtureStructureMigrationTargetContextData;
import org.eclipse.scout.rt.dataobject.migration.fixture.house.ZipCodeFixtureGlobalContextData;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class DataObjectMigrationContextTest {

  @Test
  public void testDefaults() {
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class);
    assertTrue(ctx.getGlobal(IDataObjectMigrationLogger.class) instanceof DataObjectMigrationPassThroughLogger);
    assertTrue(ctx.getLogger() instanceof DataObjectMigrationPassThroughLogger);
    assertEquals(ctx.getLogger(), ctx.getGlobal(IDataObjectMigrationLogger.class));
  }

  @Test
  public void testGlobalBean() {
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class);
    assertTrue(ctx.getGlobal(DataObjectMigrationStatsContextData.class) instanceof DataObjectMigrationStatsContextData);
    assertTrue(ctx.getStats() instanceof DataObjectMigrationStatsContextData);
    assertEquals(ctx.getStats(), ctx.getGlobal(DataObjectMigrationStatsContextData.class));
  }

  @Test
  public void testGlobalManually() {
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class);
    assertNull(ctx.getGlobal(ZipCodeFixtureGlobalContextData.class));
    ZipCodeFixtureGlobalContextData zipCodeContextData = new ZipCodeFixtureGlobalContextData();
    ctx.putGlobal(zipCodeContextData);
    assertEquals(zipCodeContextData, ctx.getGlobal(ZipCodeFixtureGlobalContextData.class));
  }

  @Test
  public void testLocal() {
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class);
    assertNull(ctx.get(HouseFixtureStructureMigrationTargetContextData.class));

    HouseFixtureStructureMigrationTargetContextData houseFixtureContextData = BEANS.get(HouseFixtureStructureMigrationTargetContextData.class);
    ctx.push(houseFixtureContextData); // not initialized, for this test okay
    assertEquals(houseFixtureContextData, ctx.get(HouseFixtureStructureMigrationTargetContextData.class));

    HouseFixtureStructureMigrationTargetContextData houseFixtureContextData2 = BEANS.get(HouseFixtureStructureMigrationTargetContextData.class);
    ctx.push(houseFixtureContextData2); // not initialized, for this test okay

    assertEquals(houseFixtureContextData2, ctx.get(HouseFixtureStructureMigrationTargetContextData.class));

    assertThrows(AssertionException.class, () -> ctx.remove(houseFixtureContextData)); // not on top of the stack

    // remove instance on top of the stack
    ctx.remove(houseFixtureContextData2);

    // first instance is returned
    assertEquals(houseFixtureContextData, ctx.get(HouseFixtureStructureMigrationTargetContextData.class));

    // remove first instance
    ctx.remove(houseFixtureContextData);

    // no instance anymore
    assertNull(ctx.get(HouseFixtureStructureMigrationTargetContextData.class));
  }

  @Test
  public void testClone() {
    DataObjectMigrationContext ctx = BEANS.get(DataObjectMigrationContext.class);

    ZipCodeFixtureGlobalContextData zipCodeContextData = new ZipCodeFixtureGlobalContextData();
    ctx.putGlobal(zipCodeContextData);

    HouseFixtureStructureMigrationTargetContextData houseFixtureContextData = BEANS.get(HouseFixtureStructureMigrationTargetContextData.class);
    ctx.push(houseFixtureContextData); // not initialized, for this test okay

    DataObjectMigrationContext ctxCopy = ctx.copy();

    // Global (same reference)
    assertSame(ctx.getGlobal(ZipCodeFixtureGlobalContextData.class), ctxCopy.getGlobal(ZipCodeFixtureGlobalContextData.class));
    assertSame(ctx.getStats(), ctxCopy.getStats());
    assertSame(ctx.getLogger(), ctxCopy.getLogger());

    // Local (not copied)
    assertNull(ctxCopy.get(HouseFixtureStructureMigrationTargetContextData.class));
  }
}
