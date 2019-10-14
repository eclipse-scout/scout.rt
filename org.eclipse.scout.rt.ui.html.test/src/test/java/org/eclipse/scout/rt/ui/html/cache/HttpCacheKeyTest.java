/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
    Map<String, String> atts = Collections.singletonMap((String) null, (String) null);
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertNull(key.getAttribute(null));
    Assert.assertNull(key.getAttribute("a"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testAttributeWithNullKey() {
    Map<String, String> atts = Collections.singletonMap((String) null, "v");
    HttpCacheKey key = new HttpCacheKey("/", atts);
    Assert.assertEquals("v", key.getAttribute(null));
    Assert.assertNull(key.getAttribute("a"));
    Assert.assertEquals("/".hashCode() + atts.hashCode(), key.hashCode());
  }

  @Test
  public void testAttributeWithNullValue() {
    Map<String, String> atts = Collections.singletonMap("a", (String) null);
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
    Assert.assertEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.<String, String> emptyMap()));
    Assert.assertEquals(new HttpCacheKey("/"), new HttpCacheKey("/"));
    Assert.assertEquals(new HttpCacheKey("/", null), new HttpCacheKey("/", null));
    Assert.assertEquals(new HttpCacheKey(null, Collections.singletonMap((String) null, (String) null)), new HttpCacheKey(null, Collections.singletonMap((String) null, (String) null)));
    Assert.assertEquals(new HttpCacheKey(null, Collections.singletonMap("a", "v")), new HttpCacheKey(null, Collections.singletonMap("a", "v")));

    Assert.assertNotEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.singletonMap((String) null, (String) null)));
    Assert.assertNotEquals(new HttpCacheKey(null), new HttpCacheKey(null, Collections.singletonMap("a", "v")));
  }

}
