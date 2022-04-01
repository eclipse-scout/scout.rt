/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.rest.client.proxy;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates proxy instances around REST client resources which provide the following features:
 * <ul>
 * <li><b>Exception handling:</b> {@link WebApplicationException}s and {@link javax.ws.rs.ProcessingException}s are
 * transformed by an {@link IRestClientExceptionTransformer} which allows to extract additional service-dependent
 * payload.</li>
 * </ul>
 * <p>
 * Cancellation is expected to be provided by the JAX-RS-implementation and its HTTP communication sub-system,
 * respectively.
 * <p>
 * <b>Note:</b> Some JAX-RS methods are only partially supported or not supported at all. Their use is not recommended
 * (see {@link #isDiscouraged(Method)}).
 */
@ApplicationScoped
public class RestClientProxyFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RestClientProxyFactory.class);

  static final String INVOCATION_SUBMIT_METHOD_NAME = "submit";

  private final LazyValue<Set<Method>> m_invocationCallbackMethods = new LazyValue<>(this::collectDiscouragedMethods);

  public Client createClientProxy(Client client, IRestClientExceptionTransformer exceptionTransformer) {
    assertNotNull(client, "client is required");
    return createProxy(Client.class, new RestProxyInvcationHandler<>(client, exceptionTransformer));
  }

  public WebTarget createWebTargetProxy(WebTarget webTarget, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(WebTarget.class, new RestProxyInvcationHandler<>(webTarget, exceptionTransformer));
  }

  public Invocation.Builder createInvocationBuilderProxy(Invocation.Builder builder, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(Invocation.Builder.class, new RestProxyInvcationHandler<>(builder, exceptionTransformer));
  }

  public Invocation createInvocationProxy(Invocation invocation, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(Invocation.class, new RestProxyInvcationHandler<>(invocation, exceptionTransformer));
  }

  public AsyncInvoker createAsyncInvokerProxy(AsyncInvoker asyncInvoker, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(AsyncInvoker.class, new RestProxyInvcationHandler<>(asyncInvoker, exceptionTransformer));
  }

  public Future<?> createFutureProxy(Future<?> future, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(Future.class, new FutureExceptionTransformerInvocationHandler(future, exceptionTransformer));
  }

  protected <T> T createProxy(Class<T> type, InvocationHandler handler) {
    return type.cast(Proxy.newProxyInstance(
        RestClientProxyFactory.class.getClassLoader(),
        new Class[]{type},
        handler));
  }

  /**
   * @return {@code true} if the given method is not completely supported by this proxy factory. Otherwise
   *         {@code false}.
   */
  protected boolean isDiscouraged(Method method) {
    return m_invocationCallbackMethods.get().contains(method);
  }

  /**
   * Collects all REST client methods that are supported only partially (i.e. async methods are not running within a
   * {@link RunContext} and exceptions are not transformed if an {@link InvocationCallback} is used).
   * <p>
   * <b>Implementation Note:</b>The mapping is computed once, kept in a {@link FinalValue} and used by
   * {@link #isDiscouraged(Method)}.
   */
  protected Set<Method> collectDiscouragedMethods() {
    Set<Method> discouragedMethods = new HashSet<>();

    // collect methods of AsyncInvoker
    Collections.addAll(discouragedMethods, AsyncInvoker.class.getDeclaredMethods());

    // collect methods of Invocation named 'submit'
    for (Method method : Invocation.class.getDeclaredMethods()) {
      if (INVOCATION_SUBMIT_METHOD_NAME.equals(method.getName())) {
        discouragedMethods.add(method);
      }
    }

    return discouragedMethods;
  }

  /**
   * @return returns {@code true} if the given object is part of a REST client proxy created by this factory.
   */
  public boolean isProxy(Object o) {
    if (o == null || !Proxy.isProxyClass(o.getClass())) {
      return false;
    }
    InvocationHandler handler = Proxy.getInvocationHandler(o);
    return handler instanceof RestProxyInvcationHandler;
  }

  /**
   * @return Returns the instance wrapped into a proxy created by this class. Otherwise the given object itself;
   */
  @SuppressWarnings("unchecked")
  public <T> T unwrap(T o) {
    if (!isProxy(o)) {
      return o;
    }
    RestProxyInvcationHandler handler = (RestProxyInvcationHandler) Proxy.getInvocationHandler(o);
    return (T) handler.unwrap();
  }

  protected class RestProxyInvcationHandler<T> implements InvocationHandler {

    private final T m_proxiedObject;
    private final IRestClientExceptionTransformer m_exceptionTransformer;

    public RestProxyInvcationHandler(T proxiedObject, IRestClientExceptionTransformer exceptionTransformer) {
      m_proxiedObject = assertNotNull(proxiedObject, "proxiedObject is required");
      m_exceptionTransformer = IRestClientExceptionTransformer.identityIfNull(exceptionTransformer);
    }

    public T unwrap() {
      return m_proxiedObject;
    }

    /**
     * Returns a REST client exception transformer. Never {@code null}.
     */
    protected IRestClientExceptionTransformer getExceptionTransformer() {
      return m_exceptionTransformer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      warnDiscouragedMethodUsage(method);
      try {
        Object result = method.invoke(unwrap(), args);
        return proxyResult(proxy, result);
      }
      catch (Exception e) {
        Throwable t = unwrapException(e);

        // check whether invocation has been cancelled
        final RunMonitor runMonitor = RunMonitor.CURRENT.get();
        if (runMonitor != null && runMonitor.isCancelled()) {
          ThreadInterruptedError tie = new ThreadInterruptedError("Interrupted while invoking REST service");
          tie.addSuppressed(t);
          throw tie;
        }

        throw transformException(t);
      }
    }

    protected void warnDiscouragedMethodUsage(Method method) {
      if (isDiscouraged(method)) {
        LOG.warn("Discouraged method invocation (e.g. running outside a RunContext or exception transformation not available)");
      }
    }

    /**
     * Proxies JAX-RS invocation related objects.
     */
    protected Object proxyResult(Object proxy, Object result) {
      if (result == unwrap()) {
        return proxy;
      }
      if (result instanceof Client) {
        return createClientProxy((Client) result, getExceptionTransformer());
      }
      if (result instanceof WebTarget) {
        return createWebTargetProxy((WebTarget) result, getExceptionTransformer());
      }
      if (result instanceof Invocation.Builder) {
        return createInvocationBuilderProxy((Invocation.Builder) result, getExceptionTransformer());
      }
      if (result instanceof Invocation) {
        return createInvocationProxy((Invocation) result, getExceptionTransformer());
      }
      if (result instanceof Future<?>) {
        return createFutureProxy((Future<?>) result, getExceptionTransformer());
      }
      if (result instanceof AsyncInvoker) {
        return createAsyncInvokerProxy((AsyncInvoker) result, getExceptionTransformer());
      }
      if (result instanceof Response) {
        // check status
        Response response = (Response) result;
        WebApplicationException webAppException = convertToWebAppException(response);
        if (webAppException == null) {
          return response;
        }
        throw getExceptionTransformer().transform(webAppException, response);
      }
      return result;
    }

    protected Throwable transformException(Throwable t) {
      if (t instanceof WebApplicationException) {
        WebApplicationException e = (WebApplicationException) t;
        return getExceptionTransformer().transform(e, e.getResponse());
      }

      if (t instanceof ResponseProcessingException) {
        ResponseProcessingException e = (ResponseProcessingException) t;
        return getExceptionTransformer().transform(e, e.getResponse());
      }

      if (t instanceof javax.ws.rs.ProcessingException) {
        javax.ws.rs.ProcessingException e = (javax.ws.rs.ProcessingException) t;
        return getExceptionTransformer().transform(e, null);
      }

      return t;
    }

    /**
     * Unwraps {@link UndeclaredThrowableException} and {@link InvocationTargetException}.
     * <p>
     * <b>Note:</b> {@link ExecutionException}s are not unwrapped by intention. See
     * {@link FutureExceptionTransformerInvocationHandler}.
     */
    protected Throwable unwrapException(Exception ex) {
      Throwable current = ex;
      while (current.getCause() != null
          && (current instanceof UndeclaredThrowableException || current instanceof InvocationTargetException)) {
        current = current.getCause();
      }
      return current;
    }
  }

  protected class FutureExceptionTransformerInvocationHandler extends RestProxyInvcationHandler<Future> {
    public FutureExceptionTransformerInvocationHandler(Future future, IRestClientExceptionTransformer exceptionTransformer) {
      super(future, exceptionTransformer);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        return super.invoke(proxy, method, args);
      }
      catch (ExecutionException e) {
        Throwable transformedException = transformException(e.getCause());
        if (transformedException == e.getCause()) {
          throw e;
        }
        throw new ExecutionException(transformedException);
      }
    }
  }

  protected WebApplicationException convertToWebAppException(Response response) {
    if (response.getStatusInfo().getFamily() == Status.Family.SUCCESSFUL) {
      return null;
    }

    // Buffer and close input stream to release any resources (i.e. memory and connections)
    response.bufferEntity();

    final Response.Status status = Response.Status.fromStatusCode(response.getStatus());
    if (status != null) {
      switch (status) {
        case BAD_REQUEST:
          return new BadRequestException(response);
        case UNAUTHORIZED:
          return new NotAuthorizedException(response);
        case FORBIDDEN:
          return new ForbiddenException(response);
        case NOT_FOUND:
          return new NotFoundException(response);
        case METHOD_NOT_ALLOWED:
          return new NotAllowedException(response);
        case NOT_ACCEPTABLE:
          return new NotAcceptableException(response);
        case UNSUPPORTED_MEDIA_TYPE:
          return new NotSupportedException(response);
        case INTERNAL_SERVER_ERROR:
          return new InternalServerErrorException(response);
        case SERVICE_UNAVAILABLE:
          return new ServiceUnavailableException(response);
      }
    }

    switch (response.getStatusInfo().getFamily()) {
      case REDIRECTION:
        return new RedirectionException(response);
      case CLIENT_ERROR:
        return new ClientErrorException(response);
      case SERVER_ERROR:
        return new ServerErrorException(response);
      default:
        return new WebApplicationException(response);
    }
  }
}
