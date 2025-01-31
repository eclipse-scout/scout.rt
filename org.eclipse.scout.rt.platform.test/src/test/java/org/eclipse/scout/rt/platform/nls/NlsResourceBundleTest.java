/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.nls;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class NlsResourceBundleTest {

  private Map<String, String> map(String... keyValues) {
    return Stream.of(keyValues).map(s -> s.split("=")).collect(Collectors.toMap(s -> s[0], s -> s[1]));
  }

  @Test
  public void testTextMapAndParent() {
    NlsResourceBundle root = new NlsResourceBundle(null, map("key1=value1", "key2=value2", "key3=value3"));
    NlsResourceBundle child1 = new NlsResourceBundle(root, map("key1=value1child1", "key2=value2child1"));
    NlsResourceBundle child11 = new NlsResourceBundle(child1, map("key1=value1child11", "key3=value3child11"));
    NlsResourceBundle child2 = new NlsResourceBundle(root, map("key1=value1child2", "key2=value2child2", "key3=value3child2"));

    assertEquals(root, child1.getParent());
    assertEquals(map("key1=value1child1", "key2=value2child1"), child1.getTextMap());

    assertEquals("value1", root.getText("key1"));
    assertEquals("value2", root.getText("key2"));
    assertEquals("value3", root.getText("key3"));
    assertEquals(null, root.getText("noKey"));

    assertEquals("value1child1", child1.getText("key1"));
    assertEquals("value2child1", child1.getText("key2"));
    assertEquals("value3", child1.getText("key3"));
    assertEquals(null, child1.getText("noKey"));

    assertEquals("value1child11", child11.getText("key1"));
    assertEquals("value2child1", child11.getText("key2"));
    assertEquals("value3child11", child11.getText("key3"));
    assertEquals(null, child11.getText("noKey"));

    assertEquals("value1child2", child2.getText("key1"));
    assertEquals("value2child2", child2.getText("key2"));
    assertEquals("value3child2", child2.getText("key3"));
    assertEquals(null, child2.getText("noKey"));
  }

  @Test
  public void testLoad() {
    assertNull(NlsResourceBundle.getBundle(null, "org.eclipse.scout.rt.platform.texts.Texts", Locale.forLanguageTag("de-CH"), NlsResourceBundleTest.class.getClassLoader()));

    assertEquals(
        map("testTextKey=default test text"),
        NlsResourceBundle.getBundle(null, "org.eclipse.scout.rt.platform.texts.Texts", Locale.ROOT, NlsResourceBundleTest.class.getClassLoader()).getTextMap());
  }
}
