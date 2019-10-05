/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.context;

import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerConfigProperties.ServerSessionCacheExpirationProperty;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

/**
 * Producer for {@link ServerRunContext} objects having a userId based {@link IServerSession} cache that is <i>NOT</i>
 * bound to the HTTP session.
 * <p>
 * The default implementation creates a copy of the current calling {@link ServerRunContext} with transaction scope
 * {@link TransactionScope#REQUIRES_NEW}.
 * <p>
 * <b>Important: </b>If no session is associated yet, it is obtained by {@link ServerSessionProviderWithCache}. This
 * means the session is cached by userId (see {@link IAccessControlService#getUserId(Subject)}) and only removed from
 * the cache if the TTL expires (see {@link ServerSessionCacheExpirationProperty})! The {@link IServerSession} is not
 * bound to the HTTP session and therefore survives the HTTP session timeouts!
 *
 * @since 5.1
 */
public class ServerRunContextProducer extends RunContextProducer {

  /**
   * Creates a {@link ServerRunContext} for the specified {@link Subject}.
   */
  @Override
  public ServerRunContext produce(final Subject subject) {
    final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent(true)
        .withSubject(subject)
        .withTransactionScope(TransactionScope.REQUIRES_NEW);

    // ensure that the session belongs to the specified subject
    // use the current set subject as subject of the session, because if the session is not null it must be the current session
    IServerSession session = serverRunContext.getSession();
    if (session == null || ObjectUtility.notEquals(Subject.getSubject(AccessController.getContext()), subject)) {
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
    }

    return serverRunContext;
  }
}
