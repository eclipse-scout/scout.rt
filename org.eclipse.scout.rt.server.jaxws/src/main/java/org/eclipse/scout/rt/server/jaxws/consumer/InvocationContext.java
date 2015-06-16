/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jaxws.consumer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

import javax.jws.WebMethod;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * This context represents a webservice Port to interact with a webservice endpoint, and is associated with a dedicated
 * Port, meaning that it can be used concurrently among other Ports. Request properties are inherited from
 * {@link AbstractJaxWsClient}, and can be overwritten for the scope of this context. That is useful if having a port
 * with some operations require some different properties set, e.g. another read-timeout to transfer big data. Also, if
 * associated with a transaction, respective commit or rollback listeners are called upon leaving the transaction
 * boundary, e.g. to implement a <code>2-phase-commit-protocol (2PC)</code> for the webservice operations invoked.
 *
 * @since 5.1
 */
public class InvocationContext<PORT> {

  public static final String PROP_USERNAME = InvocationContext.class.getName() + ".username";
  public static final String PROP_PASSWORD = InvocationContext.class.getName() + ".password";

  protected InvocationHandler m_invocationHandler;
  protected final JaxWsImplementorSpecifics m_implementorSpecifics;

  protected ICommitListener m_commitListener;
  protected IRollbackListener m_rollbackListener;

  private final String m_name;
  protected final PORT m_portProxy;
  protected final Map<String, Object> m_requestContext;

  @SuppressWarnings("unchecked")
  public InvocationContext(final PORT port, final String name) throws ProcessingException {
    m_name = name;
    m_implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);
    m_portProxy = (PORT) Proxy.newProxyInstance(port.getClass().getClassLoader(), port.getClass().getInterfaces(), createInvocationHandler(port));
    m_requestContext = new HashMap<>();

    final ITransaction tx = ITransaction.CURRENT.get();
    if (tx != null) {
      tx.registerMember(createTransactionMember(port));
    }
  }

  /**
   * @return associated port, is always the same instance and not <code>null</code>.
   */
  public PORT port() {
    return m_portProxy;
  }

  /**
   * Installs the given listener for this {@link InvocationContext} to be notified once the transaction is rolled back.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> whenRollback(final IRollbackListener listener) {
    Assertions.assertNull(m_rollbackListener, "RollbackListener already installed");
    m_rollbackListener = listener;
    return this;
  }

  /**
   * Installs the given listener for this {@link InvocationContext} to be notified once the transaction is committed.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> whenCommit(final ICommitListener listener) {
    Assertions.assertNull(m_commitListener, "CommitListener already installed");
    m_commitListener = listener;
    return this;
  }

  /**
   * Installs the given {@link InvocationHandler} for this {@link InvocationContext} to be invoked for every webservice
   * request.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> whenInvoke(final InvocationHandler invocationHandler) {
    Assertions.assertNull(m_invocationHandler, "InvocationHandler already installed");
    m_invocationHandler = invocationHandler;
    return this;
  }

  /**
   * Sets the URL of the webservice endpoint for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> endpointUrl(final String endpointUrl) {
    m_requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
    return this;
  }

  /**
   * Sets the username used by authentication handler for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> username(final String username) {
    m_requestContext.put(InvocationContext.PROP_USERNAME, username);
    return this;
  }

  /**
   * Sets the password used by authentication handler for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> password(final String password) {
    m_requestContext.put(InvocationContext.PROP_PASSWORD, password);
    return this;
  }

  /**
   * Sets the connect timeout for this {@link InvocationContext} to a specified timeout, in seconds. If the timeout
   * expires before the connection can be established, the request is aborted. Use <code>null</code> to specify
   * an infinite timeout.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> connectTimeout(final Integer connectTimeout) {
    m_implementorSpecifics.setSocketConnectTimeout(m_requestContext, (int) TimeUnit.SECONDS.toMillis(NumberUtility.nvl(connectTimeout, 0)));
    return this;
  }

  /**
   * Sets the read timeout for this {@link InvocationContext} to a specified timeout, in seconds. If the timeout
   * expires before there is data available for read, the request is aborted. Use <code>null</code> to specify an
   * infinite timeout.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> readTimeout(final Integer readTimeout) {
    m_implementorSpecifics.setSocketReadTimeout(m_requestContext, (int) TimeUnit.SECONDS.toMillis(NumberUtility.nvl(readTimeout, 0)));
    return this;
  }

  /**
   * Sets a context property for this {@link InvocationContext} to be used in JAX-WS handlers.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> contextProperty(final String key, final Object value) {
    m_requestContext.put(key, value);
    return this;
  }

  /**
   * Sets a HTTP request header for this {@link InvocationContext} to be sent to the endpoint.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> httpRequestHeader(final String key, final String value) {
    m_implementorSpecifics.setHttpRequestHeader(m_requestContext, key, value);
    return this;
  }

  /**
   * Returns a HTTP response header of the previous webservice request, or <code>null</code> if not available.
   */
  public List<String> httpResponseHeader(final String key) {
    final Map<String, Object> responseContext = ((BindingProvider) m_portProxy).getResponseContext();
    return (responseContext != null ? m_implementorSpecifics.getHttpResponseHeader(responseContext, key) : null);
  }

  /**
   * Returns the HTTP status code of the previous webservice request, or <code>-1</code> if not available yet. See the
   * constants on {@link HttpServletResponse} for valid response codes.
   */
  public int httpStatusCode() {
    final Map<String, Object> responseContext = ((BindingProvider) m_portProxy).getResponseContext();
    return (responseContext != null ? m_implementorSpecifics.getHttpStatusCode(responseContext).intValue() : -1);
  }

  /**
   * Method invoked to apply the given 'requestContext' to the port.
   */
  @Internal
  protected void applyRequestContext(final PORT port, final Map<String, Object> requestContext) {
    ((BindingProvider) port).getRequestContext().putAll(requestContext);
  }

  /**
   * Factory method to create the {@link InvocationHandler} to invoke the port operation.
   */
  @Internal
  protected InvocationHandler createInvocationHandler(final PORT port) {
    return new P_InvocationHandler(port);
  }

  /**
   * Factory method to create the {@link ITransactionMember} for this {@link InvocationContext}.
   */
  @Internal
  protected ITransactionMember createTransactionMember(final PORT port) {
    return new P_TxMember();
  }

  @Internal
  protected class P_InvocationHandler implements InvocationHandler {

    private final PORT m_port;

    public P_InvocationHandler(final PORT port) {
      m_port = port;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
      if (method.isSynthetic() || method.getAnnotation(WebMethod.class) == null) {
        return method.invoke(m_port, args); // not a webservice method.
      }
      else {
        Assertions.assertNotNullOrEmpty((String) m_requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY), "Endpoint URL must not be null or empty");
        applyRequestContext(m_port, m_requestContext);
        return invokeCancellableWebMethod(m_port, method, args);
      }
    }

    /**
     * Invokes the webservice method in a separate, blocking job to support cancellation of the request. Thereby, the
     * job is run in the calling {@link RunContext} on behalf of the current transaction.
     */
    @Internal
    protected Object invokeCancellableWebMethod(final Object port, final Method method, final Object[] args) throws Throwable {
      final Holder<Object> wsResult = new Holder<>();
      final Holder<Throwable> wsError = new Holder<>();

      final String operation = String.format("client=%s, operation=%s.%s", m_name, method.getDeclaringClass().getSimpleName(), method.getName());

      final ServerRunContext currentRunContext = ServerRunContexts.copyCurrent().transactionScope(TransactionScope.REQUIRED);
      final IFuture<Void> future = Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          try {
            if (m_invocationHandler == null) {
              wsResult.setValue(method.invoke(port, args)); // default: no custom InvocationHandler installed.
            }
            else {
              wsResult.setValue(m_invocationHandler.invoke(port, method, args)); // Custom InvocationHandler installed.
            }
          }
          catch (final InvocationTargetException e) {
            wsError.setValue(e.getCause());
          }
          catch (final Throwable t) {
            wsError.setValue(t);
          }
        }
      }, Jobs.newInput(currentRunContext).name("JAX-WS request [%s]", operation));

      try {
        future.awaitDone(); // wait until completed or interrupted.
      }
      catch (final ProcessingException e) {
        future.cancel(true); // ensure the job to be cancelled once this thread is interrupted.
      }

      // If cancelled, try to close the HTTP connection (if supported by JAX-WS implementor) and throw a CancellationException.
      if (future.isCancelled()) {
        m_implementorSpecifics.closeSocket(port, operation);
        throw new CancellationException(operation);
      }

      // Propagate result or error.
      if (wsError.getValue() != null) {
        throw wsError.getValue();
      }
      else {
        return wsResult.getValue();
      }
    }
  }

  @Internal
  protected class P_TxMember extends AbstractTransactionMember {

    public P_TxMember() {
      super(UUID.randomUUID().toString());
    }

    @Override
    public boolean needsCommit() {
      return true;
    }

    @Override
    public boolean commitPhase1() {
      return (m_commitListener == null || m_commitListener.onCommitPhase1());
    }

    @Override
    public void commitPhase2() {
      if (m_commitListener != null) {
        m_commitListener.onCommitPhase2();
      }
    }

    @Override
    public void rollback() {
      if (m_rollbackListener != null) {
        m_rollbackListener.onRollback();
      }
    }

    @Override
    public void release() {
      // NOOP
    }
  }
}
