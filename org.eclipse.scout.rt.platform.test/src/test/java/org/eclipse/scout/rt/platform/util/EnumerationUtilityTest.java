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

import static java.util.Collections.enumeration;
import static java.util.stream.Collectors.toList;
import static org.eclipse.scout.rt.platform.util.EnumerationUtility.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

public class EnumerationUtilityTest {

  @Test
  public void testAsIterator() {
    Enumeration<String> enumeration = enumeration(Arrays.asList("foo", "bar", "baz"));
    Iterator<String> iter = asIterator(enumeration);
    assertTrue(iter.hasNext());
    assertEquals("foo", iter.next());
    assertEquals("bar", iter.next());
    assertEquals("baz", iter.next());
    assertFalse(iter.hasNext());
  }

  @Test
  public void testAsEnumeration() {
    Iterator<String> iterator = Arrays.asList("foo", "bar", "baz").iterator();
    Enumeration<String> enumeration = asEnumeration(iterator);
    assertTrue(enumeration.hasMoreElements());
    assertEquals("foo", enumeration.nextElement());
    assertEquals("bar", enumeration.nextElement());
    assertEquals("baz", enumeration.nextElement());
    assertFalse(enumeration.hasMoreElements());
  }

  @Test
  public void testAsStream() {
    String bElement = "bb";
    List<String> original = Arrays.asList("a", bElement, "ccc");

    // tests forEachRemaining
    List<String> fromStream = asStream(enumeration(original)).collect(toList());
    assertEquals(original, fromStream);

    // tests tryAdvance with early end
    String element = asStream(enumeration(original))
        .filter(s -> s.length() > 1)
        .findFirst()
        .get();
    assertEquals(bElement, element);

    // tests tryAdvance ending because no more elements are present
    Optional<String> longerString = asStream(enumeration(original))
        .filter(s -> s.length() > 5)
        .findFirst();
    assertFalse(longerString.isPresent());
  }
}
