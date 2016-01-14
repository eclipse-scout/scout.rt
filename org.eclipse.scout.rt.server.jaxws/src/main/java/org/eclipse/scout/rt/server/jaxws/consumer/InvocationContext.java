/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import javax.jws.WebMethod;
import javax.mail.MessageContext;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedRuntimeException;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.jaxws.implementor.JaxWsImplementorSpecifics;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * This context represents a webservice Port to interact with a webservice endpoint, and is associated with a dedicated
 * Port, meaning that it can be used concurrently among other Ports. However, this context itself is not threadsafe and
 * therefore not to be used for concurrent webservice requests. That is due to a restriction of the JAX-WS API which
 * does not require the Port to be threadsafe. If a webservice is to be consumed concurrently, use different
 * {@link InvocationContext} instances instead.
 * <p>
 * Request properties are inherited from {@link AbstractJaxWsClient}, and can be overwritten for the scope of this
 * context. That is useful if having a port with some operations require some different properties set, e.g. another
 * read-timeout to transfer big data. Also, if associated with a transaction, respective commit or rollback listeners
 * are called upon leaving the transaction boundary, e.g. to implement a <code>2-phase-commit-protocol (2PC)</code> for
 * the webservice operations invoked.
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
  protected final PORT m_port;
  protected final PORT m_portProxy;

  @SuppressWarnings("unchecked")
  public InvocationContext(final PORT port, final String name) {
    m_name = name;
    m_implementorSpecifics = BEANS.get(JaxWsImplementorSpecifics.class);
    m_port = port;
    // Create a dynamic Java Proxy to wrap the Port to intercept webservice requests and enable request cancellation.
    m_portProxy = (PORT) Proxy.newProxyInstance(port.getClass().getClassLoader(), port.getClass().getInterfaces(), createInvocationHandler(port));

    final ITransaction tx = ITransaction.CURRENT.get();
    if (tx != null) {
      tx.registerMember(createTransactionMember(port));
    }
  }

  /**
   * @return associated port, is always the same instance and not <code>null</code>.
   */
  public PORT getPort() {
    return m_portProxy;
  }

  /**
   * Returns the context that is used to initialize the message context for request messages. Every subsequent operation
   * uses this very same request context instance, meaning that values are not removed once an operation completed. Of
   * course, values can be overwritten or removed manually.
   */
  public Map<String, Object> getRequestContext() {
    return ((BindingProvider) m_port).getRequestContext();
  }

  /**
   * Returns the context that resulted from processing a response message. The returned context is for the most recently
   * completed operation. Subsequent operations return with another response context. The response context is
   * <code>null</code> until the first operation returns.
   */
  public Map<String, Object> getResponseContext() {
    return ((BindingProvider) m_port).getResponseContext();
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
   * Sets a request context property for this {@link InvocationContext}. That way processing related state can be shared
   * among JAX-WS handlers and the JAX-WS implementor. See {@link MessageContext} property constants for some standard
   * properties.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withRequestContextProperty(final String key, final Object value) {
    getRequestContext().put(key, value);
    return this;
  }

  /**
   * Sets the URL of the webservice endpoint for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withEndpointUrl(final String endpointUrl) {
    getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl);
    return this;
  }

  /**
   * Returns the URL of the webservice endpoint for this {@link InvocationContext}.
   */
  public String getEndpointUrl() {
    return (String) getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
  }

  /**
   * Sets the username used by authentication handler for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withUsername(final String username) {
    if (username != null) {
      getRequestContext().put(InvocationContext.PROP_USERNAME, username);
    }
    else {
      getRequestContext().remove(InvocationContext.PROP_USERNAME);
    }
    return this;
  }

  /**
   * Sets the password used by authentication handler for this {@link InvocationContext}.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withPassword(final String password) {
    if (password != null) {
      getRequestContext().put(InvocationContext.PROP_PASSWORD, password);
    }
    else {
      getRequestContext().remove(InvocationContext.PROP_PASSWORD);
    }
    return this;
  }

  /**
   * Sets the connect timeout for this {@link InvocationContext} to a specified timeout, in milliseconds. If the timeout
   * expires before the connection can be established, the request is aborted. Use <code>null</code> to specify an
   * infinite timeout.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withConnectTimeout(final Integer connectTimeout) {
    m_implementorSpecifics.setSocketConnectTimeout(getRequestContext(), (int) NumberUtility.nvl(connectTimeout, 0));
    return this;
  }

  /**
   * Sets the read timeout for this {@link InvocationContext} to a specified timeout, in milliseconds. If the timeout
   * expires before there is data available for read, the request is aborted. Use <code>null</code> to specify an
   * infinite timeout.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withReadTimeout(final Integer readTimeout) {
    m_implementorSpecifics.setSocketReadTimeout(getRequestContext(), (int) NumberUtility.nvl(readTimeout, 0));
    return this;
  }

  /**
   * Sets a HTTP request header with the given name and value for this {@link InvocationContext} to be sent to the
   * endpoint.
   *
   * @return <code>this</code> in order to support for method chaining.
   */
  public InvocationContext<PORT> withHttpRequestHeader(final String name, final String value) {
    m_implementorSpecifics.setHttpRequestHeader(getRequestContext(), name, value);
    return this;
  }

  /**
   * Returns the requested HTTP response header for the most recently completed operation, or <code>null</code> if not
   * found or the operation did not complete yet.
   */
  public List<String> getHttpResponseHeader(final String key) {
    final Map<String, Object> responseContext = getResponseContext();
    return (responseContext != null ? m_implementorSpecifics.getHttpResponseHeader(responseContext, key) : null);
  }

  /**
   * Returns the HTTP status code for the most recently completed operation, or <code>-1</code> if not available yet.
   * See the constants on {@link HttpServletResponse} for valid response codes.
   */
  public int getHttpStatusCode() {
    final Map<String, Object> responseContext = getResponseContext();
    return (responseContext != null ? m_implementorSpecifics.getHttpResponseCode(responseContext).intValue() : -1);
  }

  /**
   * Returns the value for the given context property of processing the most recent completed operation, or
   * <code>null</code> if not found, or the request did not complete yet.
   * <p>
   * Typically, the context contains some processing related information, either put by a JAX-WS handler or the JAX-WS
   * implementor. See {@link MessageContext} property constants for some standard properties.
   */
  public Object getResponseContextProperty(String key) {
    final Map<String, Object> responseContext = getResponseContext();
    return responseContext != null ? responseContext.get(key) : null;
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
        Assertions.assertNotNullOrEmpty(getEndpointUrl(), "Endpoint URL must not be null or empty");
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

      // Reset the HTTP response code.
      Map<String, Object> responseContext = getResponseContext();
      if (responseContext != null) {
        m_implementorSpecifics.clearHttpResponseCode(getResponseContext());
      }

      final String operation = String.format("client=%s, operation=%s.%s", m_name, method.getDeclaringClass().getSimpleName(), method.getName());
      final ServerRunContext currentRunContext = ServerRunContexts.copyCurrent().withTransactionScope(TransactionScope.REQUIRED);

      // Invoke the web method in a separate, blocking job to allow cancellation.
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
      }, Jobs.newInput()
          .withRunContext(currentRunContext)
          .withName("Handling JAX-WS request [{}]", operation));

      try {
        future.awaitDone(); // wait until completed or interrupted.
      }
      catch (final InterruptedRuntimeException e) {
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
