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
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.slf4j.MDC;

/**
 * Provides the {@link MDC#put(String, String)} properties {@value #HTTP_SESSION_ID}, {@value #HTTP_REQUEST_METHOD},
 * {@value #HTTP_REQUEST_URI} with values from the ongoing HTTP request.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class CurrentHttpServletRequestLogCallable<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {
  public static final String HTTP_SESSION_ID = "http.session.id";
  public static final String HTTP_REQUEST_METHOD = "http.request.method";
  public static final String HTTP_REQUEST_URI = "http.request.uri";

  protected final Callable<RESULT> m_next;

  public CurrentHttpServletRequestLogCallable(final Callable<RESULT> next) {
    m_next = next;
  }

  @Override
  public RESULT call() throws Exception {
    final HttpServletRequest currentServletRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();

    final String oldSessionId = MDC.get(HTTP_SESSION_ID);
    final String oldRequestMethod = MDC.get(HTTP_REQUEST_METHOD);
    final String oldRequestUri = MDC.get(HTTP_REQUEST_URI);
    final HttpSession session = currentServletRequest.getSession(false);
    try {
      MDC.put(HTTP_SESSION_ID, session != null ? session.getId() : null);
      MDC.put(HTTP_REQUEST_METHOD, currentServletRequest.getMethod());
      MDC.put(HTTP_REQUEST_URI, currentServletRequest.getRequestURI());
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
