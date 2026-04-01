/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.cache;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.junit.Test;
import org.mockito.Mockito;

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

  /**
   * Tests insertion order stability of IHttpResponseInterceptor executions.
   */
  @Test
  public void testInterceptorOrder() {
    HttpCacheObject httpCacheObject = new HttpCacheObject(new HttpCacheKey(null), BinaryResources.create().build());
    IHttpResponseInterceptor i1 = (req, resp) -> resp.setHeader(ContentSecurityPolicy.HTTP_HEADER, "1");
    IHttpResponseInterceptor i2 = (req, resp) -> resp.setHeader(ContentSecurityPolicy.HTTP_HEADER, "2");
    IHttpResponseInterceptor i3 = (req, resp) -> resp.setHeader(ContentSecurityPolicy.HTTP_HEADER, "3");
    httpCacheObject.addHttpResponseInterceptor(i1);
    httpCacheObject.addHttpResponseInterceptor(i2);
    httpCacheObject.addHttpResponseInterceptor(i3);

    HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);
    StringHolder result = new StringHolder();
    doAnswer(a -> {
      result.setValue(a.getArgument(1));
      return null;
    }).when(resp).setHeader(anyString(), anyString());
    httpCacheObject.applyHttpResponseInterceptors(null, resp);
    assertEquals("3", result.getValue());
  }
}
