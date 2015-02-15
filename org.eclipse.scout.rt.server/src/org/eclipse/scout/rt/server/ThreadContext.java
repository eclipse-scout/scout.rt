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
import org.eclipse.scout.rt.server.commons.servletfilter.IServlet;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;

/**
 * This singleton is a container for objects associated with the
 * current Thread These objects are not accessible outside the Thread
 * This eliminates the need of creating special event dispatching
 * threads to run job queues, handle Thread-based session values etc.
 */
@Deprecated
public final class ThreadContext {

  private ThreadContext() {
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ServiceTunnelServlet#CURRENT_HTTP_SERVLET_REQUEST} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static HttpServletRequest getHttpServletRequest() {
    return IServlet.CURRENT_HTTP_SERVLET_REQUEST.get();
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ServiceTunnelServlet#CURRENT_HTTP_SERVLET_RESPONSE} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static HttpServletResponse getHttpServletResponse() {
    return IServlet.CURRENT_HTTP_SERVLET_RESPONSE.get();
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ISession#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static IServerSession getServerSession() {
    return (IServerSession) (ISession.CURRENT.get() instanceof IServerSession ? ISession.CURRENT.get() : null);
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ITransaction#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static ITransaction getTransaction() {
    return ITransaction.CURRENT.get();
  }

  // TODO [dwi]: Remove me because ThreadLocals are set/restored in the ServerJob itself.
  public static Map<Class, Object> backup() {
    Map<Class, Object> copyMap = new HashMap<Class, Object>(4);
    copyMap.put(HttpServletRequest.class, ThreadContext.getHttpServletRequest());
    copyMap.put(HttpServletResponse.class, ThreadContext.getHttpServletResponse());
    copyMap.put(IServerSession.class, ThreadContext.getServerSession());
    copyMap.put(ITransaction.class, ThreadContext.getTransaction());
    return copyMap;
  }

  // TODO [dwi]: Remove me because ThreadLocals are set/restored in the ServerJob itself.
  public static void restore(Map<Class, Object> map) {
    Map<Class, Object> copyMap = CollectionUtility.copyMap(map);
    ThreadContext.putHttpServletRequest((HttpServletRequest) copyMap.remove(HttpServletRequest.class));
    ThreadContext.putHttpServletResponse((HttpServletResponse) copyMap.remove(HttpServletResponse.class));
    ThreadContext.putServerSession((IServerSession) copyMap.remove(IServerSession.class));
    ThreadContext.putTransaction((ITransaction) copyMap.remove(ITransaction.class));
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ServiceTunnelServlet#CURRENT_HTTP_SERVLET_REQUEST} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void putHttpServletRequest(HttpServletRequest value) {
    IServlet.CURRENT_HTTP_SERVLET_REQUEST.set(value);
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ServiceTunnelServlet#CURRENT_HTTP_SERVLET_RESPONSE} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void putHttpServletResponse(HttpServletResponse value) {
    IServlet.CURRENT_HTTP_SERVLET_RESPONSE.set(value);
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ISession#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void putServerSession(IServerSession value) {
    ISession.CURRENT.set(value);
  }

  /**
   * @deprecated {@link ThreadLocal Thread-Locals} are defined directly on the representing objects; use
   *             {@link ITransaction#CURRENT} instead; will be removed in 5.2.0;
   */
  @Deprecated
  public static void putTransaction(ITransaction value) {
    ITransaction.CURRENT.set(value);
  }
}
