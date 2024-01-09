/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet ensures that {@link HttpServletRequest} and {@link HttpServletResponse} are wrapped and will be
 * invalidated after {@link HttpServlet#service(jakarta.servlet.ServletRequest, jakarta.servlet.ServletResponse)} method
 * has been completed. Any further access to those objects will throw an {@link IllegalStateException}.
 * <p>
 * Some application containers already prevent asynchronous access to released/invalid resources. This servlet enables
 * this behavior for all containers.
 */
public abstract class AbstractHttpServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpServlet.class);

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    wrap(req, resp, super::service);
  }

  protected void wrap(HttpServletRequest req, HttpServletResponse resp, HttpServletConsumer consumer) throws ServletException, IOException {
    P_HttpInvocationHandler requestProxyHandler = new P_HttpInvocationHandler(req);
    P_HttpInvocationHandler responseProxyHandler = new P_HttpInvocationHandler(resp);
    try {
      HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
          new Class[]{HttpServletRequest.class}, requestProxyHandler);
      HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
          new Class[]{HttpServletResponse.class}, responseProxyHandler);
      consumer.service(request, response);
    }
    finally {
      invalidateRequestAndResponse(req, requestProxyHandler, responseProxyHandler);
    }
  }

  /**
   * Call {@link P_HttpInvocationHandler#invalidate()} either immediately or if request started an async operation
   * register a listener to call these methods on completion.
   */
  protected void invalidateRequestAndResponse(HttpServletRequest req, P_HttpInvocationHandler requestProxyHandler, P_HttpInvocationHandler responseProxyHandler) {
    if (req.isAsyncStarted()) {
      invalidateAsync(req, requestProxyHandler, responseProxyHandler);
    }
    else {
      requestProxyHandler.invalidate();
      responseProxyHandler.invalidate();
    }
  }

  protected void invalidateAsync(HttpServletRequest req, P_HttpInvocationHandler requestProxyHandler, P_HttpInvocationHandler responseProxyHandler) {
    AsyncContext asyncContext = req.getAsyncContext();
    try {
      asyncContext.addListener(new AsyncListener() {
        @Override
        public void onComplete(AsyncEvent event) {
          requestProxyHandler.invalidate();
          responseProxyHandler.invalidate();
        }

        @Override
        public void onTimeout(AsyncEvent event) {
          requestProxyHandler.invalidate();
          responseProxyHandler.invalidate();
        }

        @Override
        public void onError(AsyncEvent event) {
          requestProxyHandler.invalidate();
          responseProxyHandler.invalidate();
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
          // nop
        }
      });
      // there is a short time-frame before listener registration were the async request may be completed, therefore it is double-checked after listener registration
      asyncContext.getRequest(); // would trigger an IllegalStateException if the async-context is already completed
    }
    catch (IllegalStateException e) {
      LOG.debug("Error during async-listener registration; invalidating request and response", e);
      // exception may be thrown if async-context has already completed
      requestProxyHandler.invalidate();
      responseProxyHandler.invalidate();
    }
  }

  protected final class P_HttpInvocationHandler implements InvocationHandler {

    private boolean m_valid = true;
    private final Object m_origin;

    public P_HttpInvocationHandler(Object origin) {
      m_origin = origin;
    }

    private void invalidate() {
      m_valid = false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (!m_valid) {
        throw new AlreadyInvalidatedException(method, m_origin);
      }
      return method.invoke(m_origin, args);
    }
  }

  @FunctionalInterface
  protected interface HttpServletConsumer {

    void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
  }
}
