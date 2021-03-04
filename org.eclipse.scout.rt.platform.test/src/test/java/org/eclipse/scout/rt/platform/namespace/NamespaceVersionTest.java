/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.namespace;

import static org.eclipse.scout.rt.platform.namespace.NamespaceVersion.*;
import static org.junit.Assert.*;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

public class NamespaceVersionTest {

  @Test
  public void testOf() {
    assertNull(of(null));
    assertNull(of(""));
    assertThrows(IllegalArgumentException.class, () -> of(" ")); // only null or empty resolves in null, other invalid calls fail

    assertThrows(IllegalArgumentException.class, () -> of("test"));
    assertThrows(IllegalArgumentException.class, () -> of("1.2.3"));
    assertThrows(IllegalArgumentException.class, () -> of("test-a"));
    assertThrows(IllegalArgumentException.class, () -> of("test-1a"));
    assertThrows(IllegalArgumentException.class, () -> of("test-1-1"));
    assertThrows(IllegalArgumentException.class, () -> of("test-1,1"));
    assertThrows(IllegalArgumentException.class, () -> of("test-1..1"));

    assertEquals(new NamespaceVersion("test", 1), of("test-1"));
    assertEquals(new NamespaceVersion("test_a", 1), of("test_a-1"));
    assertEquals(new NamespaceVersion("test", 1, 1), of("test-1.1"));
    assertEquals(new NamespaceVersion("test", 1, 1), of("test-1.01"));
    assertEquals(new NamespaceVersion("test", 1, 1, 2), of("test-1.01.2"));
    assertEquals(new NamespaceVersion("test", 1, 2, 3, 4, 5, 6, 7, 8, 9), of("test-1.2.3.4.5.6.7.8.9"));
    assertEquals(new NamespaceVersion("test", 1, 2, 3, 4, 5, 6, 7, 8, 9), of("test-1.02.003.0004.00005.000006.0000007.00000008.000000009"));
    assertEquals(of("test-1.2.3.4.5.6.7.8.9"), of("test-1.02.003.0004.00005.000006.0000007.00000008.000000009"));
  }

  @Test
  public void testCompareVersion() {
    assertThrows(NullPointerException.class, () -> compareVersion(null, null));
    assertThrows(NullPointerException.class, () -> compareVersion(of("test-1"), null));
    assertThrows(NullPointerException.class, () -> compareVersion(null, of("test-1")));

    assertEquals(0, compareVersion(of("test-1"), of("test-1")));
    assertEquals(0, compareVersion(of("test-1"), of("test-01")));
    assertEquals(0, compareVersion(of("test-1.1"), of("test-01.01")));

    assertEquals(-1, compareVersion(of("test-1"), of("test-2")));
    assertEquals(-1, compareVersion(of("test-1.1"), of("test-1.2")));
    assertEquals(-1, compareVersion(of("test-1.01"), of("test-1.2")));
    assertEquals(-1, compareVersion(of("test-1"), of("test-1.2")));

    assertEquals(1, compareVersion(of("test-2"), of("test-1")));
    assertEquals(1, compareVersion(of("test-1.2"), of("test-1.1")));
    assertEquals(1, compareVersion(of("test-1.2"), of("test-1.01")));
    assertEquals(1, compareVersion(of("test-1.2"), of("test-1")));

    assertEquals(-1, compareVersion(of("test-1"), of("test-1.0")));
    assertEquals(-1, compareVersion(of("test-1"), of("test-1.1")));

    assertEquals(1, compareVersion(of("test-1.0"), of("test-1")));
    assertEquals(1, compareVersion(of("test-1.1"), of("test-1")));

    assertEquals(0, compareVersion(of("test-12.03"), of("test2-12.03"))); // does ignore name (test vs. test2)

    assertEquals(-1, compareVersion(of("test-1.1.999.001"), of("test-1.2.001")));
    assertEquals(-1, compareVersion(of("test-1.2.2"), of("test-1.2.3.1")));
    assertEquals(-1, compareVersion(of("test-1.2.2.7"), of("test-1.2.3")));
  }

  @Test
  public void testEquals() {
    NamespaceVersion test1 = of("test-1");
    assertEquals(test1, test1);
    assertEquals(of("test-1.1"), of("test-1.001")); // version text is not relevant for equality
    assertNotEquals(of("test-1"), of("test-2"));
    assertNotEquals(of("test-1"), null);
    assertNotEquals(of("foo-1"), of("bar-1"));
    assertNotEquals(of("foo-1"), of("bar-1"));
  }

  @Test
  public void testUnwrap() {
    assertEquals("test-1", NamespaceVersion.of("test-1").unwrap());
    assertEquals("test-1", NamespaceVersion.of("test", "1").unwrap());
    assertEquals("test-1.0.001", NamespaceVersion.of("test-1.0.001").unwrap());
    assertEquals("test-1.0.001", NamespaceVersion.of("test", "1.0.001").unwrap());

    assertThrows(AssertionException.class, () -> new NamespaceVersion("test", 1).unwrap()); // unwrap only allowed if text version is available
  }

  @Test
  public void testNameRequired() {
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("12.23"));
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("-12.23"));
  }

  @Test
  public void testVersionRequired() {
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("test-"));
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("test-SNAPSHOT"));
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("test"));
    assertThrows(IllegalArgumentException.class, () -> NamespaceVersion.of("test-o123"));
  }
}
