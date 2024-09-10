/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http.async;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.BiConsumer;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.shared.http.ApacheMultiSessionCookieStore;

/**
 * <p>
 * Acts as factory and cache for {@link org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient} (client is
 * initialized only once); implementations use different {@link HttpAsyncClients} methods and configurations to build
 * this client.
 * </p>
 * <p>
 * Also acts a {@link IPlatformListener}, listening for platform change events. In case of a
 * {@link State#PlatformStopping} event, the underlying {@link CloseableHttpAsyncClient} is shut down.
 * </p>
 */
@ApplicationScoped
public abstract class AbstractAsyncHttpClientManager<BUILDER> implements IPlatformListener {

  /**
   * Flag indicating if {@link AbstractAsyncHttpClientManager} is still active.
   */
  protected volatile boolean m_active = true;

  /**
   * Flag indicating if {@link AbstractAsyncHttpClientManager} was successfully initialized.
   */
  protected volatile boolean m_initialized = false;

  /**
   * Cached {@link CloseableHttpAsyncClient}.
   */
  protected volatile CloseableHttpAsyncClient m_client;

  /**
   *
   */
  protected LazyValue<ApacheMultiSessionCookieStore> m_cookieStore = new LazyValue<>(ApacheMultiSessionCookieStore.class);

  public CloseableHttpAsyncClient getClient() {
    init();
    return m_client;
  }

  public ApacheMultiSessionCookieStore getCookieStore() {
    return m_cookieStore.get();
  }

  /**
   * Return a function to be called to install a {@link CookieStore}; return null if not supported/no store should be
   * installed.
   */
  protected abstract BiConsumer<BUILDER, CookieStore> getInstallCookieStoreBiConsumer();

  protected void installMultiSessionCookieStore(BUILDER builder) {
    BiConsumer<BUILDER, CookieStore> installCookieStoreBiConsumer = getInstallCookieStoreBiConsumer();
    if (installCookieStoreBiConsumer == null) {
      return;
    }

    ApacheMultiSessionCookieStore cookieStore = getCookieStore();
    if (cookieStore == null) {
      return;
    }
    installCookieStoreBiConsumer.accept(builder, cookieStore);
  }

  /**
   * Initialize the manager (if not initialized yet). Method call should be cheap as this method is called plenty of
   * times.
   */
  protected void init() {
    if (!m_initialized) {
      initSynchronized();
    }
  }

  /**
   * Create the {@link CloseableHttpAsyncClient} (using outer factory), fill {@link #m_client} field.
   */
  protected synchronized void initSynchronized() {
    if (m_initialized || !m_active) {
      return;
    }

    BUILDER builder = createBuilder();
    installMultiSessionCookieStore(builder);
    interceptCreateClient(builder);

    m_client = createClient(builder);
    m_client.start();

    m_initialized = true;
  }

  protected abstract BUILDER createBuilder();

  protected void interceptCreateClient(BUILDER builder) {
  }

  protected abstract CloseableHttpAsyncClient createClient(BUILDER builder);

  @Override
  public void stateChanged(PlatformEvent event) {
    if (event.getState() == State.PlatformStopping && m_client != null) {
      removeClient();
    }
  }

  /**
   * Remove/close {@link CloseableHttpAsyncClient}
   */
  protected synchronized void removeClient() {
    if (m_client == null) {
      return;
    }

    try {
      m_client.close();
    }
    catch (IOException e) {
      throw new ProcessingException("Error during {} shut down.", m_client, e);
    }
    finally {
      m_active = false;
      m_client = null;
      m_initialized = false;
    }
  }

  public <T> T createAsyncInvocationHandler(Class<T> clazz, T actualObject) {
    if (actualObject == null) {
      return null;
    }

    @SuppressWarnings("unchecked") T proxy = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new AsyncHttpInvocationHandler<>(actualObject));
    return proxy;
  }

  public static class AsyncHttpInvocationHandler<T> implements InvocationHandler {

    private final RunContext m_runContext;
    private final T m_actualObject;

    public AsyncHttpInvocationHandler(T actualObject) {
      m_runContext = RunContexts.copyCurrent(true)
          .withTransactionScope(TransactionScope.REQUIRES_NEW);
      Assertions.assertNotNullOrEmpty(m_runContext.getCorrelationId());
      m_actualObject = actualObject;
    }

    public RunContext getRunContext() {
      return m_runContext;
    }

    public T getActualObject() {
      return m_actualObject;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      return getRunContext().call(() -> method.invoke(getActualObject(), args));
    }
  }
}
