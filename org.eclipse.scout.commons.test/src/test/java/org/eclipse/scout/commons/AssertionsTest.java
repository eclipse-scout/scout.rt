/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.fail;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.junit.Test;

public class AssertionsTest {

  @Test
  public void testNotNull_Positive() {
    Assertions.assertNotNull(new Object());
  }

  @Test(expected = AssertionException.class)
  public void testNotNull_Negative() {
    Assertions.assertNotNull(null);
  }

  @Test
  public void testNotNullOrEmpty_Positive() {
    Assertions.assertNotNullOrEmpty("NOT-NULL");
  }

  @Test
  public void testNotNullOrEmpty_Negative1() {
    // Verify 'NULL'
    try {
      Assertions.assertNotNullOrEmpty(null);
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }

    // Verify 'empty'
    try {
      Assertions.assertNotNullOrEmpty("");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }
  }

  @Test
  public void testNotNullOrEmpty_Negative2() {
    // Verify 'NULL'
    try {
      Assertions.assertNotNullOrEmpty(null, "failure");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }

    // Verify 'empty'
    try {
      Assertions.assertNotNullOrEmpty("", "failure");
      fail();
    }
    catch (AssertionException e) {
      // NOOP
    }
  }

  @Test
  public void testTrue_Positive() {
    Assertions.assertTrue(true);
  }

  @Test(expected = AssertionException.class)
  public void testTrue_Negative() {
    Assertions.assertTrue(false);
  }

  @Test
  public void testFalse_Positive() {
    Assertions.assertFalse(false);
  }

  @Test(expected = AssertionException.class)
  public void testFalse_Negative() {
    Assertions.assertFalse(true);
  }
}
