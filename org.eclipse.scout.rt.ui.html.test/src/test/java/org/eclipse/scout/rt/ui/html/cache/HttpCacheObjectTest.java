package org.eclipse.scout.rt.ui.html.cache;

import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.junit.Test;

public class HttpCacheObjectTest {

  @Test(expected = Assertions.AssertionException.class)
  public void testNullNull() throws Exception {
    new HttpCacheObject(null, null);
  }

  @Test(expected = Assertions.AssertionException.class)
  public void testNullOk() throws Exception {
    new HttpCacheObject(null, BinaryResources.create().build());
  }

  @Test(expected = Assertions.AssertionException.class)
  public void testOkNull() throws Exception {
    new HttpCacheObject(new HttpCacheKey(null), null);
  }

  @Test
  public void testOkOk() throws Exception {
    new HttpCacheObject(new HttpCacheKey(null), BinaryResources.create().build());
  }
}
