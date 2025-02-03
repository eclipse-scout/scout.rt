/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit tests for {@link BooleanUtility}
 */
public class BooleanUtilityTest {

  /**
   * Test for {@link BooleanUtility#nvl(Boolean)}
   */
  @Test
  public void testNull() {
    assertFalse(BooleanUtility.nvl(null));
  }

  /**
   * Test for {@link BooleanUtility#nvl(Boolean, boolean))}
   */
  @Test
  public void testTrue() {
    assertEquals(BooleanUtility.nvl(Boolean.TRUE), true);
  }

  /**
   * Test for {@link BooleanUtility#nvl(Boolean, boolean))}
   */
  @Test
  public void testFalse() {
    assertEquals(BooleanUtility.nvl(Boolean.FALSE), false);
  }

  /**
   * Test for {@link BooleanUtility#nvl(Boolean, boolean))}
   */
  @Test
  public void testDefaultValue() {
    assertEquals(BooleanUtility.nvl(null, true), true);
  }
}
