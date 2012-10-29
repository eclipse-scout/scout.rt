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

  @Test
  public void testIsOneOf() {
    assertFalse(CompareUtility.isOneOf("test", (Object[]) null));
    assertFalse(CompareUtility.isOneOf("test", new Object[0]));
    assertFalse(CompareUtility.isOneOf("test", "foo", "bar"));
    assertFalse(CompareUtility.isOneOf("test", "foo", 12L));
    assertFalse(CompareUtility.isOneOf(null, "foo", 12L));
    assertFalse(CompareUtility.isOneOf(null, (Object[]) null));
    assertTrue(CompareUtility.isOneOf("test", "test", 12L));
    assertTrue(CompareUtility.isOneOf(null, "test", 12L, null));
    assertTrue(CompareUtility.isOneOf(null, (Object) null));
  }
}
