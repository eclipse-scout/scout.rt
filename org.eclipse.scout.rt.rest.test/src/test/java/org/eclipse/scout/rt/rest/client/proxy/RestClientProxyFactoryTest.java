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

import static org.eclipse.scout.rt.testing.platform.util.ScoutAssert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class RestClientProxyFactoryTest extends AbstractRestClientProxyFactoryTest {

  @Rule
  public ErrorCollector m_errorCollector = new ErrorCollector();

  @Test
  public void testResolveAsyncMethods() throws Exception {
    for (Method m : SyncInvoker.class.getMethods()) {
      m_errorCollector.checkThat("Expecting mapping for " + m, getFactory().resolveAsyncMethod(m), CoreMatchers.notNullValue());
    }

    for (Method m : Invocation.class.getMethods()) {
      if (!RestClientProxyFactory.INVOCATION_INVOKE_METHOD_NAME.equals(m.getName())) {
        continue;
      }
      m_errorCollector.checkThat("Expecting mapping for " + m, getFactory().resolveAsyncMethod(m), CoreMatchers.notNullValue());
    }

    assertNull(getFactory().resolveAsyncMethod(null));
    assertNull(getFactory().resolveAsyncMethod(Object.class.getMethod("toString")));

    // test method of Invocation.Builder, which is a sub-type of SyncInvoker
    assertNull(getFactory().resolveAsyncMethod(Invocation.Builder.class.getMethod("buildGet")));
  }

  @Test
  public void testIsUsingInvocationCallback() throws Exception {
    assertFalse(getFactory().isUsingInvocationCallback(null));
    assertFalse(getFactory().isUsingInvocationCallback(Object.class.getDeclaredMethod("equals", Object.class)));
    assertFalse(getFactory().isUsingInvocationCallback(AsyncInvoker.class.getDeclaredMethod("post", Entity.class, GenericType.class)));

    assertTrue(getFactory().isUsingInvocationCallback(AsyncInvoker.class.getDeclaredMethod("post", Entity.class, InvocationCallback.class)));
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
  public void testCreateAndConfigureProxiedClient() throws Exception {
    assertThrows(AssertionException.class, () -> getFactory().createClientProxy(null, null));

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
}
