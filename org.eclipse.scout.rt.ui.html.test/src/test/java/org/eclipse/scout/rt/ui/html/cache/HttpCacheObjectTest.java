/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
