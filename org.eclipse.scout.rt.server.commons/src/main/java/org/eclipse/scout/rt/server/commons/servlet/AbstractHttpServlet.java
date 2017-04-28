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
 * <h3>{@link AbstractHttpServlet}</h3>
 * <p>
 * This servlet ensures the {@link HttpServletRequest} and {@link HttpServletResponse} are wrapped and will be
 * invalidated after {@link HttpServlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)} method
 * completed. Several app containers does so by default to ensure no asynchronous access to released requests and
 * responses.
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

    private boolean m_invalidated = false;
    private Object m_origin;

    public P_HttpInvocationHandler(Object origin) {
      m_origin = origin;

    }

    private void invalidate() {
      m_invalidated = true;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (m_invalidated) {
        throw new PlatformException("Access to servlet request/response is not allowed after servlet request is completet.");
      }
      return method.invoke(m_origin, args);
    }
  }
}
