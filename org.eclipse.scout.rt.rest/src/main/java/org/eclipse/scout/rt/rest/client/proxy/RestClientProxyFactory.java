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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates proxy instances around REST client resources which provide the following features:
 * <ul>
 * <li><b>Cancellation:</b> Synchronously invoked REST services cannot be cancelled. Proxies created by this factory
 * perform synchronous invocations asynchronously, allowing to cancel the blocking client-side execution. <b>Note:</b>
 * the server-side invocation cannot be cancelled in general.</li>
 * <li><b>Exception handling:</b> {@link WebApplicationException}s and {@link javax.ws.rs.ProcessingException}s are
 * transformed by an {@link IRestClientExceptionTransformer} which allows to extract additional service-dependent
 * payload.</li>
 * </ul>
 */
@ApplicationScoped
public class RestClientProxyFactory {

  private static final Logger LOG = LoggerFactory.getLogger(RestClientProxyFactory.class);

  static final String INVOCATION_SUBMIT_METHOD_NAME = "submit";
  static final String INVOCATION_INVOKE_METHOD_NAME = "invoke";

  private final LazyValue<Map<Method, Method>> m_syncToAsyncMethods = new LazyValue<>(this::collectSyncToAsyncMethodMappings);
  private final LazyValue<Set<Method>> m_invocationCallbackMethods = new LazyValue<>(this::collectInvocationCallbackMethods);

  public Client createClientProxy(Client client, IRestClientExceptionTransformer exceptionTransformer) {
    assertNotNull(client, "client is required");
    return createProxy(Client.class, new RestProxyInvcationHandler<>(client, exceptionTransformer));
  }

  public WebTarget createWebTargetProxy(WebTarget webTarget, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(WebTarget.class, new RestProxyInvcationHandler<>(webTarget, exceptionTransformer));
  }

  public Invocation.Builder createInvocationBuilderProxy(Invocation.Builder builder, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(Invocation.Builder.class, new AsyncInvocationBuilderInvocationHandler(builder, exceptionTransformer));
  }

  public Invocation createInvocationProxy(Invocation invocation, IRestClientExceptionTransformer exceptionTransformer) {
    return createProxy(Invocation.class, new AsyncInvocationInvocationHandler(invocation, exceptionTransformer));
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
   * Resolves the corresponding async method declared in {@link AsyncInvoker} for the given one declared in
   * {@link SyncInvoker}.
   *
   * @return Returns the the corresponding method of {@link AsyncInvoker} for the requested {@link SyncInvoker} method
   *         or {@code null} in any other case.
   */
  protected Method resolveAsyncMethod(Method syncMethod) {
    return m_syncToAsyncMethods.get().get(syncMethod);
  }

  /**
   * @return {@code true} if the given method makes use of an {@link InvocationCallback} which is currently not
   *         completely covered by cancellation and exception transformation. Otherwise {@code false}.
   */
  protected boolean isUsingInvocationCallback(Method method) {
    return m_invocationCallbackMethods.get().contains(method);
  }

  /**
   * Creates the mapping of all {@link SyncInvoker} methods to their corresponding {@link AsyncInvoker} methods.
   * <p>
   * <b>Implementation Note:</b>The mapping is computed once, kept in a {@link FinalValue} and used by
   * {@link #resolveAsyncMethod(Method)}.
   */
  protected Map<Method, Method> collectSyncToAsyncMethodMappings() {
    Map<Method, Method> syncToAsyncMethods = new HashMap<>();

    // map SyncInvoker to AsyncInvoker methods
    for (Method syncMethod : SyncInvoker.class.getDeclaredMethods()) {
      getMethod(AsyncInvoker.class, syncMethod.getName(), syncMethod.getParameterTypes())
          .ifPresent(asyncMethod -> syncToAsyncMethods.put(syncMethod, asyncMethod));
    }

    // map sync methods on Invocation to corresponding async method
    for (Method method : Invocation.class.getMethods()) {
      if (INVOCATION_INVOKE_METHOD_NAME.equals(method.getName())) {
        getMethod(Invocation.class, INVOCATION_SUBMIT_METHOD_NAME, method.getParameterTypes())
            .ifPresent(asyncMethod -> syncToAsyncMethods.put(method, asyncMethod));
      }
    }
    return syncToAsyncMethods;
  }

  /**
   * Collects all REST client methods that are using an {@link InvocationCallback} parameter, which is currently not
   * completely supported by this factory (exceptions are not transformed).
   * <p>
   * <b>Implementation Note:</b>The mapping is computed once, kept in a {@link FinalValue} and used by
   * {@link #isUsingInvocationCallback(Method)}.
   */
  protected Set<Method> collectInvocationCallbackMethods() {
    Set<Method> discouragedMethods = new HashSet<>();

    // collect methods of AsyncInvoker which last parameter is a InvocationCallback
    for (Method method : AsyncInvoker.class.getDeclaredMethods()) {
      Class<?>[] paramTypes = method.getParameterTypes();
      if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1] == InvocationCallback.class) {
        discouragedMethods.add(method);
      }
    }

    // add Invocation.submit(InvocationCallback)
    getMethod(Invocation.class, INVOCATION_SUBMIT_METHOD_NAME, InvocationCallback.class).ifPresent(discouragedMethods::add);

    return discouragedMethods;
  }

  /**
   * Returns a public method of the given class, that matches the given name and parameter types.
   */
  protected Optional<Method> getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
    try {
      return Optional.of(clazz.getMethod(name, parameterTypes));
    }
    catch (NoSuchMethodException | SecurityException e) {
      LOG.warn("Could not find method {}.{}({})", clazz, name, parameterTypes, e);
    }
    return Optional.empty();
  }

  /**
   * @return returns {@code true} if the given object is part of a REST client proxy created by this factory.
   */
  public boolean isProxy(Object o) {
    if (o == null || !Proxy.isProxyClass(o.getClass())) {
      return false;
    }
    InvocationHandler handler = Proxy.getInvocationHandler(o);
    return RestProxyInvcationHandler.class.isInstance(handler);
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
      if (isUsingInvocationCallback(method)) {
        LOG.warn("Discuraged method invocation: Exceptions handed over to InvocationCallback.failed() are not transformed by this REST client proxy");
      }
      Object result = method.invoke(unwrap(), args);
      return proxyResult(proxy, result);
    }

    protected Object proxyResult(Object proxy, Object result) {
      // proxy JAX-RS invocation related objects
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

    protected Object invokeRestService(Supplier<Object> asyncObjectSupplier, Method method, Object[] args) {
      try {
        final Future<?> future = findAndInvokeAsyncMethod(asyncObjectSupplier, method, args);
        if (future != null) {
          return awaitDoneAndGet(future);
        }

        // fall back to sync invocation
        return invokeSyncMethod(method, args);
      }
      catch (WebApplicationException e) {
        throw getExceptionTransformer().transform(e, e.getResponse());
      }
      catch (ResponseProcessingException e) {
        throw getExceptionTransformer().transform(e, e.getResponse());
      }
      catch (javax.ws.rs.ProcessingException e) {
        throw getExceptionTransformer().transform(e, null);
      }
    }

    protected Future<?> findAndInvokeAsyncMethod(Supplier<Object> asyncObjectSupplier, Method syncMethod, Object[] args) {
      try {
        Method asyncMethod = resolveAsyncMethod(syncMethod);
        if (asyncMethod != null) {
          LOG.debug("transforming sync to async REST invocation");
          return (Future<?>) asyncMethod.invoke(asyncObjectSupplier.get(), args);
        }
      }
      catch (Exception e) {
        LOG.warn("converting sync to async method failed. Falling back to sync invocation [method='{}']", syncMethod, e);
      }
      return null;
    }

    protected Object invokeSyncMethod(Method syncMethod, Object[] args) {
      try {
        return syncMethod.invoke(unwrap(), args);
      }
      catch (Exception e) {
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }

    protected Object awaitDoneAndGet(Future<?> future) {
      try {
        return future.get();
      }
      catch (CancellationException e) {
        throw new FutureCancelledError("Async REST invocation has been cancelled", e);
      }
      catch (InterruptedException e) {
        future.cancel(true); // cancel async invocation
        Thread.currentThread().interrupt(); // restore interrupted flag
        throw new ThreadInterruptedError("Interrupted while invoking REST service", e);
      }
      catch (ExecutionException e) {
        // unwrap execution exception and make sure it is an unchecked exception
        throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
      }
    }
  }

  protected class AsyncInvocationBuilderInvocationHandler extends RestProxyInvcationHandler<Invocation.Builder> {

    public AsyncInvocationBuilderInvocationHandler(Invocation.Builder builder, IRestClientExceptionTransformer exceptionTransformer) {
      super(builder, exceptionTransformer);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getDeclaringClass() == SyncInvoker.class) {
        return proxyResult(proxy, invokeRestService(() -> unwrap().async(), method, args));
      }
      return super.invoke(proxy, method, args);
    }
  }

  protected class AsyncInvocationInvocationHandler extends RestProxyInvcationHandler<Invocation> {

    public AsyncInvocationInvocationHandler(Invocation invocation, IRestClientExceptionTransformer exceptionTransformer) {
      super(invocation, exceptionTransformer);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (INVOCATION_INVOKE_METHOD_NAME.equals(method.getName())) {
        return proxyResult(proxy, invokeRestService(this::unwrap, method, args));
      }
      return super.invoke(proxy, method, args);
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
      catch (@SuppressWarnings("squid:S1166") InvocationTargetException ie) {
        Throwable cause = ie.getTargetException();
        if (cause instanceof ExecutionException) {
          cause = ((ExecutionException) cause).getCause();
        }

        BiFunction<RuntimeException, Response, ExecutionException> extractAndTransform = (re, r) -> new ExecutionException(getExceptionTransformer().transform(re, r));
        if (cause instanceof WebApplicationException) {
          WebApplicationException we = (WebApplicationException) cause;
          throw extractAndTransform.apply(we, we.getResponse());
        }
        else if (cause instanceof ResponseProcessingException) {
          ResponseProcessingException rpe = (ResponseProcessingException) cause;
          throw extractAndTransform.apply(rpe, rpe.getResponse());
        }
        else if (cause instanceof javax.ws.rs.ProcessingException) {
          javax.ws.rs.ProcessingException pe = (javax.ws.rs.ProcessingException) cause;
          throw extractAndTransform.apply(pe, null);
        }
        throw ie.getTargetException();
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
