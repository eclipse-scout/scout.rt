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

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.authentication.BearerAuthAccessController.HttpBearerAuthConfig;
import org.eclipse.scout.rt.server.commons.authentication.token.ITokenVerifier;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

@RunWith(PlatformTestRunner.class)
public class BearerAuthAccessControllerTest {

  @Test
  public void testParseBearerAuthRequest() {
    BearerAuthAccessController accessController = BEANS.get(BearerAuthAccessController.class).init(new HttpBearerAuthConfig()
        .withTokenVerifier(token -> ITokenVerifier.AUTH_OK));

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    // Single token
    setAuthorizationHeader(request, Base64Utility.encode("Test".getBytes()));
    List<byte[]> tokenParts = accessController.readBearerToken(request);
    assertEquals(1, tokenParts.size());
    assertEquals("Test", new String(tokenParts.get(0)));

    // Multiple token parts
    setAuthorizationHeader(request,
        Base64Utility.encode("Test1".getBytes()) + "-"
            + Base64Utility.encode("Test2".getBytes()) + "."
            + Base64Utility.encode("Test3".getBytes()) + "_"
            + Base64Utility.encode("Test4".getBytes()) + "~"
            + Base64Utility.encode("Test5".getBytes()));
    tokenParts = accessController.readBearerToken(request);
    assertEquals(5, tokenParts.size());
    assertEquals("Test1", new String(tokenParts.get(0)));
    assertEquals("Test2", new String(tokenParts.get(1)));
    assertEquals("Test3", new String(tokenParts.get(2)));
    assertEquals("Test4", new String(tokenParts.get(3)));
    assertEquals("Test5", new String(tokenParts.get(4)));

    // Illegal token
    setAuthorizationHeader(request, "Not BASE64 Encoded");
    assertTrue(accessController.readBearerToken(request).isEmpty());
  }

  @Test
  public void testHandle() throws ServletException, IOException {
    BearerAuthAccessController accessController = BEANS.get(BearerAuthAccessController.class).init(new HttpBearerAuthConfig()
        .withTokenVerifier(tokenParts -> CollectionUtility.size(tokenParts) == 1 && ObjectUtility.equals("Correct".getBytes(), tokenParts.get(0)) ? ITokenVerifier.AUTH_OK : ITokenVerifier.AUTH_FORBIDDEN));

    FilterChain filterChain = Mockito.mock(FilterChain.class);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    IntegerHolder statusCodeHolder = new IntegerHolder();
    HttpServletResponse response = setupHttpServletResponseMock(statusCodeHolder);
    Mockito
        .doAnswer(invocation -> {
          statusCodeHolder.setValue(HttpServletResponse.SC_OK);
          return null;
        })
        .when(filterChain).doFilter(request, response);

    statusCodeHolder.setValue(null);
    setAuthorizationHeader(request, Base64Utility.encode("Correct".getBytes()));
    assertTrue(accessController.handle(request, response, filterChain));
    assertEquals(HttpServletResponse.SC_OK, statusCodeHolder.getValue().intValue());
    Mockito.verify(filterChain).doFilter(request, response);

    statusCodeHolder.setValue(null);
    setAuthorizationHeader(request, Base64Utility.encode("Wrong".getBytes()));
    assertTrue(accessController.handle(request, response, filterChain));
    assertEquals(HttpServletResponse.SC_FORBIDDEN, statusCodeHolder.getValue().intValue());
    Mockito.verifyNoMoreInteractions(filterChain);

    statusCodeHolder.setValue(null);
    setAuthorizationHeader(request, null);
    assertTrue(accessController.handle(request, response, filterChain));
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, statusCodeHolder.getValue().intValue());
    Mockito.verifyNoMoreInteractions(filterChain);
  }

  /**
   * Sets the authorization header with the provided (base64 encoded) bearer token
   */
  private void setAuthorizationHeader(HttpServletRequest request, String bearerToken) {
    Mockito
        .when(request.getHeader(ArgumentMatchers.eq(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION)))
        .thenReturn(bearerToken == null ? null : BearerAuthAccessController.HTTP_BEARER_AUTH_NAME + " " + bearerToken);
  }

  /**
   * Provides a response object that provides the set error codes to the statusCodeHolder
   *
   * @param statusCodeHolder
   *          IntegerHolder that holds the error status if set
   */
  private HttpServletResponse setupHttpServletResponseMock(final IntegerHolder statusCodeHolder) throws IOException {
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    Answer statusCodeSetter = invocation -> {
      statusCodeHolder.setValue(invocation.getArgument(0, Integer.class));
      return null;
    };
    Mockito.doAnswer(statusCodeSetter).when(response).sendError(ArgumentMatchers.anyInt());
    Mockito.doAnswer(statusCodeSetter).when(response).sendError(ArgumentMatchers.anyInt(), ArgumentMatchers.anyString());
    return response;
  }
}
