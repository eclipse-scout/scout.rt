/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @since 3.8.1
 */
public class CompareUtilityTest {

  static final String TEST_STRING = "test";
  static final String FOO_STRING = "foo";
  static final String BAR_STRING = "bar";

  static final long NUMBER_12 = 12L;

  @Test
  public void testIsOneOf() {
    assertFalse(CompareUtility.isOneOf(TEST_STRING, (Object[]) null));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, new Object[0]));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, BAR_STRING));
    assertFalse(CompareUtility.isOneOf(TEST_STRING, FOO_STRING, NUMBER_12));
    assertFalse(CompareUtility.isOneOf(null, FOO_STRING, NUMBER_12));
    assertFalse(CompareUtility.isOneOf(null, (Object[]) null));
    assertTrue(CompareUtility.isOneOf(TEST_STRING, TEST_STRING, NUMBER_12));
    assertTrue(CompareUtility.isOneOf(null, TEST_STRING, NUMBER_12, null));
    assertTrue(CompareUtility.isOneOf(null, (Object) null));
  }
}
