/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * This servlet ensures that {@link HttpServletRequest} and {@link HttpServletResponse} are wrapped and will be
 * invalidated after {@link HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} method has
 * been completed. Any further access to those objects will throw an {@link IllegalStateException}.
 * <p>
 * Some application containers already prevent asynchronous access to released/invalid resources. This servlet enables
 * this behavior for all containers.
 */
public abstract class AbstractHttpServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    P_HttpInvocationHandler requestProxyHandler = new P_HttpInvocationHandler(req);
    P_HttpInvocationHandler responseProxyHandler = new P_HttpInvocationHandler(resp);
    try {
      HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
          new Class[]{HttpServletRequest.class}, requestProxyHandler);
      HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(HttpServletResponse.class.getClassLoader(),
          new Class[]{HttpServletResponse.class}, responseProxyHandler);
      super.service(request, response);
    }
    finally {
      requestProxyHandler.invalidate();
      responseProxyHandler.invalidate();
    }
  }

  private final class P_HttpInvocationHandler implements InvocationHandler {

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
        throw new PlatformException("Access to '{}' is not allowed because {} is no longer valid (request has been completed).", method, (m_origin instanceof HttpServletRequest ? "HTTP servlet request" : "HTTP servlet response"));
      }
      return method.invoke(m_origin, args);
    }
  }
}
