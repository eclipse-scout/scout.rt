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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.dataobject.id.IIds;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestContext;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ClientNodeIdHttpProxyRequestOptionsModifierTest {

  /**
   * Client node ID header should be present on proxied request
   */
  @Test
  public void testClientNodeId() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpSession session = Mockito.mock(HttpSession.class);
    Mockito.when(request.getSession(false)).thenReturn(session);

    ClientNodeIdHttpProxyRequestOptionsModifier modifier = new ClientNodeIdHttpProxyRequestOptionsModifier();
    HttpProxyRequestOptions options = new HttpProxyRequestOptions();
    modifier.modify(options, createRequestContext(request));

    assertTrue(options.getCustomRequestHeaders().containsKey(NodeId.HTTP_HEADER_NAME));
    assertEquals(IIds.toString(NodeId.current()), options.getCustomRequestHeaders().get(NodeId.HTTP_HEADER_NAME));
  }

  protected HttpProxyRequestContext createRequestContext(HttpServletRequest request) {
    return BEANS.get(HttpProxyRequestContext.class)
        .withRequest(request);
  }
}
