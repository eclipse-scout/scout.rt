/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestContext;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.eclipse.scout.rt.shared.session.SessionId;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.ui.html.json.testing.JsonTestUtility;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ClientSessionIdHttpProxyRequestOptionsModifierTest {

  /**
   * Request on HTTP session that knows the UI session - client session ID header should be present on proxied request
   */
  @Test
  public void testExistingUiSessionId() {
    UiSession uiSession = createAndInitializeUiSession();
    HttpServletRequest request = createRequest(getHttpSession(uiSession), uiSession.getUiSessionId());

    ClientSessionIdHttpProxyRequestOptionsModifier modifier = new ClientSessionIdHttpProxyRequestOptionsModifier();
    HttpProxyRequestOptions options = new HttpProxyRequestOptions();
    modifier.modify(options, createRequestContext(request));

    assertTrue(options.getCustomRequestHeaders().containsKey(SessionId.HTTP_HEADER_NAME));
    assertEquals(uiSession.getClientSessionId(), options.getCustomRequestHeaders().get(SessionId.HTTP_HEADER_NAME));
  }

  /**
   * Request on HTTP session with existing UI session, but unknown UI Session ID
   */
  @Test
  public void testNonExistingUiSessionId() {
    UiSession uiSession = createAndInitializeUiSession();
    HttpServletRequest request = createRequest(getHttpSession(uiSession), "12345");

    ClientSessionIdHttpProxyRequestOptionsModifier modifier = new ClientSessionIdHttpProxyRequestOptionsModifier();
    HttpProxyRequestOptions options = new HttpProxyRequestOptions();
    modifier.modify(options, createRequestContext(request));

    assertFalse(options.getCustomRequestHeaders().containsKey(SessionId.HTTP_HEADER_NAME));
  }

  /**
   * Request on different HTTP session that does not know the UI session
   */
  @Test
  public void testDifferentHttpSession() {
    UiSession uiSession = createAndInitializeUiSession();
    HttpServletRequest request = createRequest(mock(HttpSession.class), uiSession.getUiSessionId());

    ClientSessionIdHttpProxyRequestOptionsModifier modifier = new ClientSessionIdHttpProxyRequestOptionsModifier();
    HttpProxyRequestOptions options = new HttpProxyRequestOptions();
    modifier.modify(options, createRequestContext(request));

    assertFalse(options.getCustomRequestHeaders().containsKey(SessionId.HTTP_HEADER_NAME));
  }

  protected HttpProxyRequestContext createRequestContext(HttpServletRequest request) {
    return BEANS.get(HttpProxyRequestContext.class)
        .withRequest(request);
  }

  protected UiSession createAndInitializeUiSession() {
    // create ui session
    UiSession uiSession = (UiSession) JsonTestUtility.createAndInitializeUiSession();
    final HttpSession httpSession = getHttpSession(uiSession);

    // register ui session in session store
    final ISessionStore sessionStore = BEANS.get(HttpSessionHelper.class).getSessionStore(httpSession);
    sessionStore.registerUiSession(uiSession);
    return uiSession;
  }

  protected HttpSession getHttpSession(UiSession uiSession) {
    return UiSessionTestUtility.getHttpSession(uiSession);
  }

  protected HttpServletRequest createRequest(HttpSession httpSession, String uiSessionId) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(eq(IUiSession.ID_HTTP_HEADER_NAME))).thenReturn(uiSessionId);
    when(request.getSession(false)).thenReturn(httpSession);
    return request;
  }
}
