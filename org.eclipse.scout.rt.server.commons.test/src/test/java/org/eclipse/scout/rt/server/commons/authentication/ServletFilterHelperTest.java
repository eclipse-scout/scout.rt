/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ServletFilterHelperTest {

  @Test
  public void testIsIdempotent() {
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    Assert.assertThrows(NullPointerException.class, () -> helper.isIdempotent(null));
    assertTrue(helper.isIdempotent(mockRequest("GET")));
    assertTrue(helper.isIdempotent(mockRequest("PUT")));
    assertTrue(helper.isIdempotent(mockRequest("HEAD")));
    assertTrue(helper.isIdempotent(mockRequest("OPTIONS")));
    assertTrue(helper.isIdempotent(mockRequest("DELETE")));
    assertTrue(helper.isIdempotent(mockRequest("TRACE")));
    assertFalse(helper.isIdempotent(mockRequest("")));
    assertFalse(helper.isIdempotent(mockRequest("POST")));
    assertFalse(helper.isIdempotent(mockRequest("XYZ"))); // unknown method
  }

  private HttpServletRequest mockRequest(String method) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn(method);
    return req;
  }

  @Test
  public void testRedirectIncompleteBasePath() throws IOException {
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ArgumentCaptor<String> locationCapture = ArgumentCaptor.forClass(String.class);
    doNothing().when(response).sendRedirect(locationCapture.capture());

    // No redirection because non-idempotent
    assertFalse(helper.redirectIncompleteBasePath(mockRequest("POST", "/my-app", "/my-servlet", "http://server/my-app/", "a=1&b=2"), response, false));

    // Redirect (missing slash)
    assertTrue(helper.redirectIncompleteBasePath(mockRequest("GET", "/my-app", "/my-servlet", "http://server/my-app", "a=1&b=2"), response, false));
    assertEquals("http://server/my-app/?a=1&b=2", locationCapture.getValue());

    // No redirect (slash is present)
    assertFalse(helper.redirectIncompleteBasePath(mockRequest("GET", "/my-app", "/my-servlet", "http://server/my-app/", "a=1&b=2"), response, false));

    // Redirect (missing slash)
    assertTrue(helper.redirectIncompleteBasePath(mockRequest("GET", "/my-app", "/my-servlet", "http://server/my-app/my-servlet", "a=1&b=2"), response, true));
    assertEquals("http://server/my-app/my-servlet/?a=1&b=2", locationCapture.getValue());

    // No redirect (slash is present)
    assertFalse(helper.redirectIncompleteBasePath(mockRequest("GET", "/my-app", "/my-servlet", "http://server/my-app/my-servlet/", "a=1&b=2"), response, true));
  }

  private HttpServletRequest mockRequest(String method, String contextPath, String servletPath, String requestUri, String queryString) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getMethod()).thenReturn(method);
    when(req.getServletPath()).thenReturn(servletPath);
    ServletContext ctx = mock(ServletContext.class);
    when(req.getServletContext()).thenReturn(ctx);
    when(ctx.getContextPath()).thenReturn(contextPath);
    when(req.getRequestURI()).thenReturn(requestUri);
    when(req.getQueryString()).thenReturn(queryString);
    return req;
  }

  @Test
  public void testCreateBasicAuthRequest() {
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);

    Assert.assertThrows(NullPointerException.class, () -> helper.createBasicAuthRequest(null, null));
    Assert.assertThrows(NullPointerException.class, () -> helper.createBasicAuthRequest("scott", null));

    assertEquals("Basic bnVsbDo=", helper.createBasicAuthRequest(null, "".toCharArray()));
    assertEquals("Basic c2NvdHQ6", helper.createBasicAuthRequest("scott", "".toCharArray()));
    assertEquals("Basic c2NvdHQ6dGlnZXI=", helper.createBasicAuthRequest("scott", "tiger".toCharArray()));
    assertEquals("Basic c2NvdHQ6MTIzOnRpZ2Vy", helper.createBasicAuthRequest("scott:123", "tiger".toCharArray()));
    assertEquals("Basic c2NvdHQ6dGlnZXI6ZWxlcGhhbnQ=", helper.createBasicAuthRequest("scott", "tiger:elephant".toCharArray()));
  }

  @Test
  public void testParseBasicAuthRequest() {
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);

    assertArrayEquals(null, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, null)));
    assertArrayEquals(null, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Some string")));

    // "null" is probably unwanted, but it asserts the current implementation
    assertArrayEquals(new String[]{"null", ""}, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Basic bnVsbDo=")));
    assertArrayEquals(new String[]{"scott", ""}, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Basic c2NvdHQ6")));
    assertArrayEquals(new String[]{"scott", "tiger"}, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Basic c2NvdHQ6dGlnZXI=")));
    // The following is actually unwanted, but it asserts the current implementation
    assertArrayEquals(new String[]{"scott", "123:tiger"}, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Basic c2NvdHQ6MTIzOnRpZ2Vy")));
    assertArrayEquals(new String[]{"scott", "tiger:elephant"}, helper.parseBasicAuthRequest(mockRequest(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION, "Basic c2NvdHQ6dGlnZXI6ZWxlcGhhbnQ=")));
  }

  private HttpServletRequest mockRequest(String header, String value) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader(header)).thenReturn(value);
    return req;
  }
}
