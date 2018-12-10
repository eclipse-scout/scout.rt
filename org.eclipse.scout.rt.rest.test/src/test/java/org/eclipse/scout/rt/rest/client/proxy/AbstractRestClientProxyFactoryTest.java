/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.rest.client.proxy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.mockito.Mockito;

public abstract class AbstractRestClientProxyFactoryTest {

  public static final String URI = "http:://localhost:80/test";

  protected RestClientProxyFactory m_factory;

  @Before
  public void before() {
    m_factory = new RestClientProxyFactory();
  }

  public RestClientProxyFactory getFactory() {
    return m_factory;
  }

  /**
   * Creates a new mock of {@link Client} that creates mocks for return values of invoked methods.
   */
  protected Client mockClient() {
    return Mockito.mock(Client.class, Mockito.RETURNS_MOCKS);
  }

  protected AsyncInvoker mockAsyncInvoker(Builder builder) {
    AsyncInvoker asyncInvoker = mock(AsyncInvoker.class);
    when(getFactory().unwrap(builder).async()).thenReturn(asyncInvoker);
    return asyncInvoker;
  }

  protected static Response mockResponse() {
    Response response = Mockito.spy(Response.class);
    when(response.getStatusInfo()).thenReturn(Response.Status.OK);
    return response;
  }

  protected void assertRestProxy(Object proxy) {
    assertNotNull(proxy);
    assertTrue(getFactory().isProxy(proxy));
  }
}
