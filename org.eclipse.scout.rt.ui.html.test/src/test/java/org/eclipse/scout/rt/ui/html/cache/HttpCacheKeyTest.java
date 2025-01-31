/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.cache;

import java.util.Collections;
import java.util.Map;

import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.junit.Assert;
import org.junit.Test;

public class HttpCacheKeyTest {

  @Test
  public void testNullPath() {
    HttpCacheKey key = new HttpCacheKey(null);
    Assert.assertNull(key.getResourcePath());
    Assert.assertEquals(0, key.hashCode());
  }

  @Test
  public void testEmptyPath() {
    HttpCacheKey key = new HttpCacheKey("");
    Assert.assertEquals("", key.getResourcePath());
    Assert.assertEquals(0, key.hashCode());
  }

  @Test
  public void testSimplePath() {
    String path = "/test.html";
    HttpCacheKey key = new HttpCacheKey(path);
    Assert.assertEquals(path, key.getResourcePath());
    Assert.assertEquals(path.hashCode(), key.hashCode());
  }

  @Test
  public void testNoAttributes() {
    HttpCacheKey key = new HttpCacheKey("/");
    Assert.assertNull(key.getAttribute("a"));
  }

  @Test
  public void testAttributeWithNullKeyAndValue() {
    Map<String, String> atts = Collections.singletonMap(null, null);
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertNull(key.getAttribute(null));
    Assert.assertNull(key.getAttribute("a"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testAttributeWithNullKey() {
    Map<String, String> atts = Collections.singletonMap(null, "v");
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertEquals("v", key.getAttribute(null));
    Assert.assertNull(key.getAttribute("a"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testAttributeWithNullValue() {
    Map<String, String> atts = Collections.singletonMap("a", null);
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertNull(key.getAttribute(null));
    Assert.assertNull(key.getAttribute("a"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testAttribute() {
    Map<String, String> atts = Collections.singletonMap("a", "v");
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertNull(key.getAttribute(null));
    Assert.assertEquals("v", key.getAttribute("a"));
    Assert.assertNull(key.getAttribute("b"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testEquals() {
    Assert.assertEquals(new HttpCacheKey(null), new HttpCacheKey(null));
    Assert.assertEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.emptyMap()));
    Assert.assertEquals(new HttpCacheKey("/"), new HttpCacheKey("/"));
    Assert.assertEquals(new HttpCacheKey("/", null), new HttpCacheKey("/", null));
    Assert.assertEquals(new HttpCacheKey(null, Collections.singletonMap(null, null)), new HttpCacheKey(null, Collections.singletonMap(null, null)));
    Assert.assertEquals(new HttpCacheKey(null, Collections.singletonMap("a", "v")), new HttpCacheKey(null, Collections.singletonMap("a", "v")));

    Assert.assertNotEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.singletonMap(null, null)));
    Assert.assertNotEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.singletonMap("a", "v")));
  }
}
