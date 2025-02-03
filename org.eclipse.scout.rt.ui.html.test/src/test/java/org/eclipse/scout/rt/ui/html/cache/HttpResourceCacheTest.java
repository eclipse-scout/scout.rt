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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpResourceCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpResourceCacheTest {

  private HttpResourceCache rc;

  @Before
  public void before() {
    rc = BEANS.get(HttpResourceCache.class);
  }

  @Test
  public void testPutNotCachable() throws Exception {
    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .build();
    HttpCacheKey key = new HttpCacheKey("/");
    HttpCacheObject obj = new HttpCacheObject(key, res);
    boolean b = rc.put(obj);
    Assert.assertFalse(b);
  }

  @Test
  public void testPutCachable() throws Exception {
    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = rc.put(obj);
    Assert.assertTrue(b);
  }

  @Test
  public void testGet() {
    HttpCacheKey key = new HttpCacheKey("/");
    HttpCacheObject obj = rc.get(key);
    Assert.assertNull(obj);
  }

  @Test
  public void testPutGet() throws Exception {
    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = rc.put(obj);
    Assert.assertTrue(b);

    HttpCacheObject obj2 = rc.get(new HttpCacheKey("/"));
    Assert.assertEquals(obj.getCacheKey(), obj2.getCacheKey());
  }

  @Test
  public void testPutRemoveGet() throws Exception {
    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = rc.put(obj);
    Assert.assertTrue(b);

    rc.remove(new HttpCacheKey("/"));

    HttpCacheObject obj2 = rc.get(new HttpCacheKey("/"));
    Assert.assertNull(obj2);
  }
}
