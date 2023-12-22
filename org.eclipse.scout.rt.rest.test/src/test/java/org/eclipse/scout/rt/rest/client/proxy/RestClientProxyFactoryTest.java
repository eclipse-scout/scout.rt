/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.client.proxy;

import static org.junit.Assert.*;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.RxInvoker;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.mockito.Mockito;

public class RestClientProxyFactoryTest {

  public static final String URI = "http:://localhost:80/test";

  @Rule
  public ErrorCollector m_errorCollector = new ErrorCollector();

  private RestClientProxyFactory m_factory;

  @Before
  public void before() {
    m_factory = new RestClientProxyFactory();
  }

  public RestClientProxyFactory getFactory() {
    return m_factory;
  }

  @Test
  public void testIsDiscouraged() throws Exception {
    assertFalse(getFactory().isDiscouraged(null));
    assertFalse(getFactory().isDiscouraged(Object.class.getDeclaredMethod("equals", Object.class)));
    assertFalse(getFactory().isDiscouraged(SyncInvoker.class.getDeclaredMethod("get")));

    assertTrue(getFactory().isDiscouraged(AsyncInvoker.class.getDeclaredMethod("post", Entity.class, GenericType.class)));
    assertTrue(getFactory().isDiscouraged(AsyncInvoker.class.getDeclaredMethod("post", Entity.class, InvocationCallback.class)));
    assertTrue(getFactory().isDiscouraged(Invocation.class.getDeclaredMethod("submit")));
    assertTrue(getFactory().isDiscouraged(Invocation.Builder.class.getDeclaredMethod("rx")));
    assertTrue(getFactory().isDiscouraged(RxInvoker.class.getDeclaredMethod("post", Entity.class, GenericType.class)));
  }

  @Test
  public void testIsProxy() {
    assertFalse(getFactory().isProxy(null));
    assertFalse(getFactory().isProxy("test"));
    assertFalse(getFactory().isProxy(new Object()));

    Client client = mockClient();
    assertFalse(getFactory().isProxy(client));
    Client proxy = getFactory().createClientProxy(client, null);
    assertRestProxy(proxy);
  }

  @Test
  public void testUnwrap() {
    assertNull(getFactory().unwrap(null));

    Object o = new Object();
    assertSame(o, getFactory().unwrap(o));

    Client client = mockClient();
    Client proxy = getFactory().createClientProxy(client, null);
    assertSame(client, getFactory().unwrap(proxy));
  }

  @Test
  public void testCreateAndConfigureProxiedClient() {
    Assert.assertThrows(AssertionException.class, () -> getFactory().createClientProxy(null, null));

    Client client = mockClient();
    // ensure client is not mistakenly considered as async proxy
    assertFalse(getFactory().isProxy(client));

    Client proxyClient = getFactory().createClientProxy(client, null);
    assertRestProxy(proxyClient);

    // create target
    WebTarget target = proxyClient.target(URI);
    assertRestProxy(target);

    // add param to target
    WebTarget targetWithParam = target.queryParam("param", "value");
    assertRestProxy(targetWithParam);

    // create request
    Builder invocationBuilder = targetWithParam.request();
    assertRestProxy(invocationBuilder);

    // accept
    Builder invocationBuilderWithAccept = invocationBuilder.accept(MediaType.APPLICATION_JSON);
    assertRestProxy(invocationBuilderWithAccept);

    invocationBuilderWithAccept.property("", null);
  }

  /**
   * Creates a new mock of {@link Client} that creates mocks for return values of invoked methods.
   */
  protected Client mockClient() {
    return Mockito.mock(Client.class, Mockito.RETURNS_MOCKS);
  }

  protected void assertRestProxy(Object proxy) {
    assertNotNull(proxy);
    assertTrue(getFactory().isProxy(proxy));
  }
}
