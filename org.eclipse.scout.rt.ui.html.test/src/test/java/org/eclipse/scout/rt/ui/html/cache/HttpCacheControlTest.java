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

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.platform.resource.BinaryResources;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheKey;
import org.eclipse.scout.rt.server.commons.servlet.cache.HttpCacheObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class HttpCacheControlTest {
  private static final VerificationMode ANY_TIMES = Mockito.atLeast(0);
  private static final VerificationMode ONCE = Mockito.times(1);

  private HttpSession session;
  private HttpServletRequest req;
  private HttpServletResponse resp;
  private HttpCacheControl cc;
  private boolean oldDevMode;

  @Before
  public void before() {
    oldDevMode = BEANS.get(PlatformDevModeProperty.class).getValue();
    BEANS.get(PlatformDevModeProperty.class).setValue(false);
    session = Mockito.mock(HttpSession.class);

    req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(req.getSession()).thenReturn(session);
    Mockito.when(req.getSession(false)).thenReturn(session);

    resp = Mockito.mock(HttpServletResponse.class);

    cc = BEANS.get(HttpCacheControl.class);
  }

  @After
  public void after() {
    //session
    Mockito.verify(session, ANY_TIMES).getAttribute(Mockito.matches(".*"));

    //req
    Mockito.verify(req, ANY_TIMES).getSession();
    Mockito.verify(req, ANY_TIMES).getSession(false);

    //resp
    Mockito.verifyNoMoreInteractions(session);
    Mockito.verifyNoMoreInteractions(req);
    Mockito.verifyNoMoreInteractions(resp);
    BEANS.get(PlatformDevModeProperty.class).setValue(oldDevMode);
  }

  @Test
  public void testCheckAndSet_DisableCaching() {
    Mockito.when(req.getPathInfo()).thenReturn("/");

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(false)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, no-store, no-cache, max-age=0");
  }

  @Test
  public void testCheckAndSet_EnableCaching() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModified(0L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
  }

  @Test
  public void testCheckAndSet_EnableCaching_MaxAge3() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withCacheMaxAge(3)
        .withLastModified(0L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=3, s-maxage=3");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
  }

  @Test
  public void testCheckAndSet_EnableCaching_LastModified() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
    Mockito.verify(resp, ONCE).setDateHeader(HttpCacheControl.LAST_MODIFIED, obj.getResource().getLastModified());
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfNoneMatch_false() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn("W/\"FooBar\"");//non-matching E-Tag
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
    Mockito.verify(resp, ONCE).setDateHeader(HttpCacheControl.LAST_MODIFIED, obj.getResource().getLastModified());
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfNoneMatch_true() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn("W/\"FooBar\", W/\"13-535168142\"");//matching E-Tag
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertTrue(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfModifiedSince_Modified() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModified(2000000L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
    Mockito.verify(resp, ONCE).setDateHeader(HttpCacheControl.LAST_MODIFIED, obj.getResource().getLastModified());
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfModifiedSince_ModifiedAtFidelityPlus1() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModified(1000000L + HttpCacheControl.IF_MODIFIED_SINCE_FIDELITY + 1L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.ETAG, obj.createETag());
    Mockito.verify(resp, ONCE).setDateHeader(HttpCacheControl.LAST_MODIFIED, obj.getResource().getLastModified());
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfModifiedSince_NotModifiedAtFidelity() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModified(1000000L + HttpCacheControl.IF_MODIFIED_SINCE_FIDELITY)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertTrue(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
  }

  @Test
  public void testCheckAndSet_EnableCaching_IfModifiedSince_NotModified() {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes(StandardCharsets.UTF_8))
        .withCachingAllowed(true)
        .withLastModified(900000L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, obj);
    Assert.assertTrue(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.ETAG);
    Mockito.verify(req, ANY_TIMES).getHeader(HttpCacheControl.IF_NONE_MATCH);
    Mockito.verify(req, ANY_TIMES).getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE);
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, max-age=0, must-revalidate");
    Mockito.verify(resp, ONCE).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
  }
}
