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

import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.junit.Test;

public class HttpCacheObjectTest {

  @Test(expected = Assertions.AssertionException.class)
  public void testNullNull() {
    new HttpCacheObject(null, null);
  }

  @Test(expected = Assertions.AssertionException.class)
  public void testNullOk() {
    new HttpCacheObject(null, BinaryResources.create().build());
  }

  @Test(expected = Assertions.AssertionException.class)
  public void testOkNull() {
    new HttpCacheObject(new HttpCacheKey(null), null);
  }

  @Test
  public void testOkOk() {
    new HttpCacheObject(new HttpCacheKey(null), BinaryResources.create().build());
  }
}
