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
 * Test cases for {@link IdStructureTestSupport}.
 */
public abstract class AbstractIdStructureTestSupportTest {

  @Test
  public void testFilePattern() {
    Pattern filePattern = BEANS.get(IdStructureTestSupport.class).getFilePattern();
    Assert.assertTrue(filePattern.matcher("public final class MyId extends AbstractStringId {}").find());
    Assert.assertTrue(filePattern.matcher("public final class MyId extends AbstractRootId {}").find());
    Assert.assertFalse(filePattern.matcher("public final class MyId extends MyClass {}").find());
    Assert.assertTrue(filePattern.matcher("public final class MyId implements IRootId {}").find());
    Assert.assertFalse(filePattern.matcher("public final class MyId extends MyOtherId {}").find());
  }
}
