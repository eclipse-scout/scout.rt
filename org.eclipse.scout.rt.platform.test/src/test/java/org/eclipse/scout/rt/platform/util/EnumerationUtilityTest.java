/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

import org.junit.Test;

public class EnumerationUtilityTest {

  @Test
  public void testAsIterator() {
    Enumeration<String> enumeration = Collections.enumeration(Arrays.asList("foo", "bar", "baz"));
    Iterator<String> iter = EnumerationUtility.asIterator(enumeration);
    assertTrue(iter.hasNext());
    assertEquals("foo", iter.next());
    assertEquals("bar", iter.next());
    assertEquals("baz", iter.next());
    assertFalse(iter.hasNext());
  }

  @Test
  public void testAsEnumeration() {
    Iterator<String> iterator = Arrays.asList("foo", "bar", "baz").iterator();
    Enumeration<String> enumeration = EnumerationUtility.asEnumeration(iterator);
    assertTrue(enumeration.hasMoreElements());
    assertEquals("foo", enumeration.nextElement());
    assertEquals("bar", enumeration.nextElement());
    assertEquals("baz", enumeration.nextElement());
    assertFalse(enumeration.hasMoreElements());
  }
}
