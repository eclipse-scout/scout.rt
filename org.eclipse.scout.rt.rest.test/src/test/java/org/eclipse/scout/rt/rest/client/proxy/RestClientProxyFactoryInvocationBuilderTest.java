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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RestClientProxyFactoryInvocationBuilderTest<T> extends AbstractRestClientProxyFactoryTest {

  private InvocationSpec<T> m_spec;

  @Parameters(name = "{0}")
  public static List<InvocationSpec<?>> values() {
    final GenericType<List<String>> genericType = new GenericType<List<String>>() {
    };
    final Entity<String> entity = Entity.json("{'key': 'value'}");
    final String customHttpMethod = "custom";

    return Arrays.asList(
        // GET
        InvocationSpec.of("GET -> Response", sync -> sync.get(), async -> async.get(), mockResponse()),
        InvocationSpec.of("GET -> Entity", sync -> sync.get(String.class), async -> async.get(String.class), "test"),
        InvocationSpec.of("GET -> GenericType", sync -> sync.get(genericType), async -> async.get(genericType), Arrays.asList("a", "b")),

        // PUT
        InvocationSpec.of("PUT -> Response", sync -> sync.put(entity), async -> async.put(entity), mockResponse()),
        InvocationSpec.of("PUT -> Entity", sync -> sync.put(entity, String.class), async -> async.put(entity, String.class), "test"),
        InvocationSpec.of("PUT -> GenericType", sync -> sync.put(entity, genericType), async -> async.put(entity, genericType), Arrays.asList("a", "b")),

        // POST
        InvocationSpec.of("POST -> Response", sync -> sync.post(entity), async -> async.post(entity), mockResponse()),
        InvocationSpec.of("POST -> Entity", sync -> sync.post(entity, String.class), async -> async.post(entity, String.class), "test"),
        InvocationSpec.of("POST -> GenericType", sync -> sync.post(entity, genericType), async -> async.post(entity, genericType), Arrays.asList("a", "b")),

        // DELETE
        InvocationSpec.of("DELETE -> Response", sync -> sync.delete(), async -> async.delete(), mockResponse()),
        InvocationSpec.of("DELETE -> Entity", sync -> sync.delete(String.class), async -> async.delete(String.class), "test"),
        InvocationSpec.of("DELETE -> GenericType", sync -> sync.delete(genericType), async -> async.delete(genericType), Arrays.asList("a", "b")),

        // HEAD
        InvocationSpec.of("HEAD -> Response", sync -> sync.head(), async -> async.head(), mockResponse()),

        // OPTIONS
        InvocationSpec.of("OPTIONS -> Response", sync -> sync.options(), async -> async.options(), mockResponse()),
        InvocationSpec.of("OPTIONS -> Entity", sync -> sync.options(String.class), async -> async.options(String.class), "test"),
        InvocationSpec.of("OPTIONS -> GenericType", sync -> sync.options(genericType), async -> async.options(genericType), Arrays.asList("a", "b")),

        // TRACE
        InvocationSpec.of("TRACE -> Response", sync -> sync.trace(), async -> async.trace(), mockResponse()),
        InvocationSpec.of("TRACE -> Entity", sync -> sync.trace(String.class), async -> async.trace(String.class), "test"),
        InvocationSpec.of("TRACE -> GenericType", sync -> sync.trace(genericType), async -> async.trace(genericType), Arrays.asList("a", "b")),

        // ARBITRARY METHOD
        InvocationSpec.of("CUSTOM() -> Response", sync -> sync.method(customHttpMethod), async -> async.method(customHttpMethod), mockResponse()),
        InvocationSpec.of("CUSTOM() -> Entity", sync -> sync.method(customHttpMethod, String.class), async -> async.method(customHttpMethod, String.class), "test"),
        InvocationSpec.of("CUSTOM() -> GenericType", sync -> sync.method(customHttpMethod, genericType), async -> async.method(customHttpMethod, genericType), Arrays.asList("a", "b")),
        InvocationSpec.of("CUSTOM(Entity) -> Response", sync -> sync.method(customHttpMethod, entity), async -> async.method(customHttpMethod, entity), mockResponse()),
        InvocationSpec.of("CUSTOM(Entity) -> Entity", sync -> sync.method(customHttpMethod, entity, String.class), async -> async.method(customHttpMethod, entity, String.class), "test"),
        InvocationSpec.of("CUSTOM(Entity) -> GenericType", sync -> sync.method(customHttpMethod, entity, genericType), async -> async.method(customHttpMethod, entity, genericType), Arrays.asList("a", "b")));
  }

  public RestClientProxyFactoryInvocationBuilderTest(InvocationSpec<T> spec) {
    m_spec = spec;
  }

  @Test
  public void testInvocationBuilderUpgradeSyncInvocationToAsync() {
    // create new request
    Builder builder = getFactory().createClientProxy(mockClient(), null).target(URI).request();
    assertSyncInvocationIsUpgradedToAsync(builder);
  }

  @Test
  public void testInvocationBuilderPreserveAsyncInvocation() throws Exception {
    // create new request
    Builder builder = getFactory().createClientProxy(mockClient(), null).target(URI).request();

    // create async invoker and record expected behavior
    AsyncInvoker asyncInvoker = mockAsyncInvoker(builder);
    when(m_spec.getAsyncCall().apply(asyncInvoker)).thenReturn(CompletableFuture.completedFuture(m_spec.getExpectedResult()));

    // invoke service using sync method
    Future<T> future = m_spec.getAsyncCall().apply(asyncInvoker);
    assertSame(m_spec.getExpectedResult(), future.get());

    // verify async method has been invoked
    m_spec.getAsyncCall().apply(verify(asyncInvoker));
    Builder proxiedBuilder = getFactory().unwrap(builder);
    verifyNoMoreInteractions(asyncInvoker, proxiedBuilder);
  }

  @Test
  public void testClientInvocationUpgradeSyncInvocationToAsync() {
    // create new request
    Link link = mock(Link.class);
    Builder builder = getFactory().createClientProxy(mockClient(), null).invocation(link);
    assertSyncInvocationIsUpgradedToAsync(builder);
  }

  private void assertSyncInvocationIsUpgradedToAsync(Builder builder) {
    // create async invoker and record expected behavior
    AsyncInvoker asyncInvoker = mockAsyncInvoker(builder);
    when(m_spec.getAsyncCall().apply(asyncInvoker)).thenReturn(CompletableFuture.completedFuture(m_spec.getExpectedResult()));

    // invoke service using sync method
    T response = m_spec.getSyncCall().apply(builder);
    assertSame(m_spec.getExpectedResult(), response);

    // verify async method has been invoked
    m_spec.getAsyncCall().apply(verify(asyncInvoker));
    Builder proxiedBuilder = getFactory().unwrap(builder);
    verify(proxiedBuilder).async();
    verifyNoMoreInteractions(asyncInvoker, proxiedBuilder);
  }

  protected static class InvocationSpec<T> {
    private final String m_name;
    private final Function<SyncInvoker, T> m_syncCall;
    private final Function<AsyncInvoker, Future<T>> m_asyncCall;
    private final T m_expectedResult;

    public static <T> InvocationSpec of(String name, Function<SyncInvoker, T> syncCall, Function<AsyncInvoker, Future<T>> asyncCall, T expectedResult) {
      return new InvocationSpec<T>(name, syncCall, asyncCall, expectedResult);
    }

    private InvocationSpec(String name, Function<SyncInvoker, T> syncCall, Function<AsyncInvoker, Future<T>> asyncCall, T expectedResult) {
      m_name = name;
      m_syncCall = syncCall;
      m_asyncCall = asyncCall;
      m_expectedResult = expectedResult;
    }

    public Function<SyncInvoker, T> getSyncCall() {
      return m_syncCall;
    }

    public Function<AsyncInvoker, Future<T>> getAsyncCall() {
      return m_asyncCall;
    }

    public T getExpectedResult() {
      return m_expectedResult;
    }

    @Override
    public String toString() {
      return m_name;
    }
  }
}
