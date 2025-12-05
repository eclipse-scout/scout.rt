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

import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.rest.resource.ApiExposedFilter;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxy;
import org.eclipse.scout.rt.server.commons.servlet.HttpProxyRequestOptions;
import org.junit.Test;
import org.mockito.internal.util.MockUtil;

/**
 * Abstract test for {@link AbstractRestProxyRequestHandler} instances
 */
public abstract class AbstractRestProxyRequestHandlerTest {

  @Test
  public void testProxiedHeader() throws IOException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    doReturn("").when(req).getPathInfo();
    HttpServletResponse resp = mock(HttpServletResponse.class);

    AbstractRestProxyRequestHandler handlerToTest = getHandlerToTest();
    AbstractRestProxyRequestHandler handler = MockUtil.isMock(handlerToTest) ? handlerToTest : spy(handlerToTest);
    HttpProxyRequestOptions options = mock(HttpProxyRequestOptions.class);
    doReturn(options).when(handler).createHttpProxyRequestOptions(eq(req), eq(resp));

    HttpProxy proxy = mock(HttpProxy.class);
    when(handler.getProxy()).thenReturn(proxy);
    handler.proxy(req, resp);

    // header has been added
    verify(options, times(1)).withCustomRequestHeader(eq(ApiExposedFilter.HTTP_HEADER_NAME), any());
    verifyNoMoreInteractions(options);

    verify(proxy, times(1)).proxy(eq(req), eq(resp), eq(options));
    verifyNoMoreInteractions(proxy);
  }

  protected abstract AbstractRestProxyRequestHandler getHandlerToTest();
}
