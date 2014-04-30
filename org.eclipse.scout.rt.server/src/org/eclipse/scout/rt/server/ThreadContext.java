/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.transaction.ITransaction;

/**
 * This singleton is a container for objects associated with the
 * current Thread These objects are not accessible outside the Thread
 * This eliminates the need of creating special event dispatching
 * threads to run job queues, handle Thread-based session values etc.
 */
public final class ThreadContext {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ThreadContext.class);
  private static final ThreadLocal<HttpServletRequest> HTTP_SERVLET_REQUEST = new ThreadLocal<HttpServletRequest>();
  private static final ThreadLocal<HttpServletResponse> HTTP_SERVLET_RESPONSE = new ThreadLocal<HttpServletResponse>();
  private static final ThreadLocal<IServerSession> SERVER_SESSION = new ThreadLocal<IServerSession>();
  private static final ThreadLocal<ITransaction> TRANSACTION = new ThreadLocal<ITransaction>();

  private ThreadContext() {
  }

  public static HttpServletRequest getHttpServletRequest() {
    return HTTP_SERVLET_REQUEST.get();
  }

  public static HttpServletResponse getHttpServletResponse() {
    return HTTP_SERVLET_RESPONSE.get();
  }

  public static IServerSession getServerSession() {
    return SERVER_SESSION.get();
  }

  public static ITransaction getTransaction() {
    return TRANSACTION.get();
  }

  public static Map<Class, Object> backup() {
    Map<Class, Object> copyMap = new HashMap<Class, Object>(4);
    copyMap.put(HttpServletRequest.class, ThreadContext.getHttpServletRequest());
    copyMap.put(HttpServletResponse.class, ThreadContext.getHttpServletResponse());
    copyMap.put(IServerSession.class, ThreadContext.getServerSession());
    copyMap.put(ITransaction.class, ThreadContext.getTransaction());
    return copyMap;
  }

  public static void restore(Map<Class, Object> map) {
    Map<Class, Object> copyMap = CollectionUtility.copyMap(map);
    ThreadContext.putHttpServletRequest((HttpServletRequest) copyMap.remove(HttpServletRequest.class));
    ThreadContext.putHttpServletResponse((HttpServletResponse) copyMap.remove(HttpServletResponse.class));
    ThreadContext.putServerSession((IServerSession) copyMap.remove(IServerSession.class));
    ThreadContext.putTransaction((ITransaction) copyMap.remove(ITransaction.class));
  }

  public static void putHttpServletRequest(HttpServletRequest value) {
    HTTP_SERVLET_REQUEST.set(value);
  }

  public static void putHttpServletResponse(HttpServletResponse value) {
    HTTP_SERVLET_RESPONSE.set(value);
  }

  public static void putServerSession(IServerSession value) {
    SERVER_SESSION.set(value);
  }

  public static void putTransaction(ITransaction value) {
    TRANSACTION.set(value);
  }

}
