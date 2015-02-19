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
package org.eclipse.scout.jaxws.internal;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;

import org.eclipse.scout.jaxws.session.IServerSessionFactory;
import org.eclipse.scout.rt.server.IServerSession;

/**
 * Helper to maintain the session on the calling context
 */
public final class ContextHelper {
  public static final String PROP_SESSION_CONTEXT = "org.eclipse.scout.jaxws.internal.session";
  public static final String PROP_SESSION_FACTORY_PORT_TYPE = "org.eclipse.scout.jaxws.internal.session.factory.porttype";

  private ContextHelper() {
  }

  /**
   * Returns the session stored on the calling context
   * 
   * @param context
   * @return the cached session or null if not applicable
   */
  public static IServerSession getContextSession(MessageContext context) {
    Object object = context.get(PROP_SESSION_CONTEXT);
    if (object instanceof SessionContextBean) {
      return ((SessionContextBean) object).getSession();
    }
    return null;
  }

  /**
   * Returns the session factory class which was used to create the cached session.
   * 
   * @param context
   * @return the session factory or null if the session is not cached on the calling context yet
   */
  public static Class<? extends IServerSessionFactory> getContextSessionFactoryClass(MessageContext context) {
    Object object = context.get(PROP_SESSION_CONTEXT);
    if (object instanceof SessionContextBean) {
      return ((SessionContextBean) object).getFactoryClass();
    }
    return null;
  }

  /**
   * Returns the session factory configured on the port type
   * 
   * @param context
   * @return the session factory on the port type or null if not applicable
   */
  public static IServerSessionFactory getPortTypeSessionFactory(MessageContext context) {
    Object object = context.get(PROP_SESSION_FACTORY_PORT_TYPE);
    if (object instanceof IServerSessionFactory) {
      return (IServerSessionFactory) object;
    }
    return null;
  }

  /**
   * To set the session and its factory class on the calling context
   * 
   * @param context
   * @param factory
   *          the factory the given session was created by
   * @param session
   *          the created session
   */
  public static void setContextSession(MessageContext context, IServerSessionFactory factory, IServerSession session) {
    if (factory == null || session == null) {
      context.remove(PROP_SESSION_CONTEXT);
    }
    else {
      context.put(PROP_SESSION_CONTEXT, new SessionContextBean(factory.getClass(), session));
      context.setScope(PROP_SESSION_CONTEXT, Scope.APPLICATION); // APPLICATION-SCOPE to be accessible in @{link ScoutInstanceResolver}
    }
  }

  /**
   * To set the session factory configured on the port type for the calling context
   * 
   * @param context
   * @param factory
   *          the factory configured on port type
   */
  public static void setPortTypeSessionFactory(MessageContext context, IServerSessionFactory factory) {
    if (factory == null) {
      context.remove(PROP_SESSION_FACTORY_PORT_TYPE);
    }
    else {
      context.put(PROP_SESSION_FACTORY_PORT_TYPE, factory);
      context.setScope(PROP_SESSION_FACTORY_PORT_TYPE, Scope.HANDLER); // HANDLER-SCOPE as only accessible by handlers
    }
  }
}
