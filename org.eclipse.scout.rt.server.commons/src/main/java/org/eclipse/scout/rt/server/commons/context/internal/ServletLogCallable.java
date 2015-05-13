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
package org.eclipse.scout.rt.server.commons.context.internal;

import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IChainable;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #HTTP_SESSION_ID}, {@value #HTTP_REQUEST_METHOD},
 * {@value #HTTP_REQUEST_URI}
 */
public class ServletLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String HTTP_SESSION_ID = "http.session.id";
  public static final String HTTP_REQUEST_METHOD = "http.request.method";
  public static final String HTTP_REQUEST_URI = "http.request.uri";

  protected final Callable<RESULT> m_next;
  protected final HttpServletRequest m_request;
  protected final HttpServletResponse m_response;

  public ServletLogCallable(Callable<RESULT> next, HttpServletRequest request, HttpServletResponse response) {
    m_next = next;
    m_request = request;
    m_response = response;
  }

  @Override
  public RESULT call() throws Exception {
    String oldSessionId = MDC.get(HTTP_SESSION_ID);
    String oldRequestMethod = MDC.get(HTTP_REQUEST_METHOD);
    String oldRequestUri = MDC.get(HTTP_REQUEST_URI);
    HttpSession session = m_request.getSession(false);
    try {
      MDC.put(HTTP_SESSION_ID, session != null ? session.getId() : null);
      MDC.put(HTTP_REQUEST_METHOD, m_request.getMethod());
      MDC.put(HTTP_REQUEST_URI, m_request.getRequestURI());
      //
      return m_next.call();
    }
    finally {
      if (oldSessionId != null) {
        MDC.put(HTTP_SESSION_ID, oldSessionId);
      }
      else {
        MDC.remove(HTTP_SESSION_ID);
      }
      if (oldRequestMethod != null) {
        MDC.put(HTTP_REQUEST_METHOD, oldRequestMethod);
      }
      else {
        MDC.remove(HTTP_REQUEST_METHOD);
      }
      if (oldRequestUri != null) {
        MDC.put(HTTP_REQUEST_URI, oldRequestUri);
      }
      else {
        MDC.remove(HTTP_REQUEST_URI);
      }
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
