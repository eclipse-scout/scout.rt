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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
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

  @Before
  public void before() {
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
  }

  @Test
  public void testCheckAndSet_DisableCaching_forward() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/subPath");
    Mockito.when(req.getAttribute("javax.servlet.forward.path_info")).thenReturn("/");

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(false)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
  }

  @Test
  public void testCheckAndSet_EnableCaching_forward() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/subPath");
    Mockito.when(req.getAttribute("javax.servlet.forward.path_info")).thenReturn("/");

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
  }

  @Test
  public void testCheckAndSet_DisableCaching() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(false)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
    Assert.assertFalse(b);

    Mockito.verify(req, ANY_TIMES).getPathInfo();
    Mockito.verify(req, ANY_TIMES).getAttribute("javax.servlet.forward.path_info");
    Mockito.verify(resp, ONCE).setHeader(HttpCacheControl.CACHE_CONTROL, "private, no-store, no-cache, max-age=0");
  }

  @Test
  public void testCheckAndSet_EnableCaching() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModified(0L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_MaxAge3() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withCacheMaxAge(3)
        .withLastModified(0L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_LastModified() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfNoneMatch_false() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn("W/\"FooBar\"");//non-matching E-Tag
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfNoneMatch_true() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn("W/\"FooBar\", W/\"13-535168142\"");//matching E-Tag
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(0L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModifiedNow()
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfModifiedSince_Modified() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModified(2000000L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfModifiedSince_ModifiedAtFidelityPlus1() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModified(1000000L + HttpCacheControl.IF_MODIFIED_SINCE_FIDELITY + 1L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfModifiedSince_NotModifiedAtFidelity() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModified(1000000L + HttpCacheControl.IF_MODIFIED_SINCE_FIDELITY)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
  public void testCheckAndSet_EnableCaching_IfModifiedSince_NotModified() throws Exception {
    Mockito.when(req.getPathInfo()).thenReturn("/");
    Mockito.when(req.getHeader(HttpCacheControl.ETAG)).thenReturn(null);
    Mockito.when(req.getHeader(HttpCacheControl.IF_NONE_MATCH)).thenReturn(null);
    Mockito.when(req.getDateHeader(HttpCacheControl.IF_MODIFIED_SINCE)).thenReturn(1000000L);

    BinaryResource res = BinaryResources.create()
        .withFilename("a.html")
        .withContent("<html></html>".getBytes("UTF-8"))
        .withCachingAllowed(true)
        .withLastModified(900000L)
        .build();
    HttpCacheObject obj = new HttpCacheObject(new HttpCacheKey("/"), res);
    boolean b = cc.checkAndSetCacheHeaders(req, resp, null, obj);
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
