/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.dataobject.testing;

import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for {@link DataObjectMigrationHandlerCompletenessTestSupport}.
 */
public abstract class AbstractDataObjectMigrationHandlerCompletenessTestSupportTest {

  @Test
  public void testFilePattern() {
    Pattern filePattern = BEANS.get(DataObjectMigrationHandlerCompletenessTestSupport.class).getFilePattern();
    Assert.assertTrue(filePattern.matcher("public class MyMigrationHandler_1_0_0_001 extends AbstractDoStructureMigrationHandler {}").find());
    Assert.assertTrue(filePattern.matcher("public class MyMigrationHandler_1_0_0_001 implements IDoStructureMigrationHandler {}").find());
    Assert.assertTrue(filePattern.matcher("public class MyMigrationHandler_1_0_0_001 extends SomeMigrationHandler_1_0_0_001 {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyValueMigrationHandler_1_0_0_001 implements IDoValueMigrationHandler {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyValueMigrationHandler_1_0_0_001 extends AbstractDoValueMigrationHandler<MyDoEntity> {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyValueMigrationHandler_1_0_0_001 extends SomeValueMigrationHandler {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyValueMigrationHandler_1_0_0_001 extends SomeValueMigrationHandler_1_0_0_001 {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyMigrationHandlerTest extends SomeMigrationHandlerTest {}").find());
    Assert.assertFalse(filePattern.matcher("public class MyValueMigrationHandlerTest extends SomeValueMigrationHandlerTest {}").find());
  }
}
