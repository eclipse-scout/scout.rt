package org.eclipse.scout.rt.shared.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Test;

/**
 * Test for {@link ICacheRegistryService}
 */
public class CacheRegistryServiceTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testRegistry() {
    CacheRegistryService s = new CacheRegistryService();
    String testKey = "testkey";
    BasicCache<String, String> testCache = new BasicCache<String, String>(testKey, mock(ICacheValueResolver.class), new HashMap<String, String>(), false);
    s.register(testCache);
    assertEquals(testCache, s.get(testKey));
  }

  @Test(expected = AssertionException.class)
  public void testNotRegisteredCache() {
    CacheRegistryService s = new CacheRegistryService();
    assertNull(s.get("unknown"));
  }

  @Test
  public void testNotRegisteredOptCache() {
    CacheRegistryService s = new CacheRegistryService();
    assertNull(s.opt("unknown"));
  }

}
