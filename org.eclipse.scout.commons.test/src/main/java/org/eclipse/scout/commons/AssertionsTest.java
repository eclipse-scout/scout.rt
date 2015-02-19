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

import org.junit.Test;

public class AssertionsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNotNull() {
    Assertions.assertNotNull(null);
  }

  @Test
  public void testNotNullOrEmpty() {
    // Verify 'NULL'
    try {
      Assertions.assertNotNullOrEmpty(null);
      fail();
    }
    catch (IllegalArgumentException e) {
      // NOOP
    }

    // Verify 'empty'
    try {
      Assertions.assertNotNullOrEmpty("");
      fail();
    }
    catch (IllegalArgumentException e) {
      // NOOP
    }
  }
}
