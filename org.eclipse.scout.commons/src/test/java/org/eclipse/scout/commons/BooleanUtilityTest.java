/*******************************************************************************
 * Copyright (c) 2014 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
