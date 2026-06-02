/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.session.context;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.session.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

/**
 * Producer for {@link ServerSessionRunContext} based on {@link Subject}.
 */
@ApplicationScoped
public class ServerSessionRunContextProducer {

  public ServerSessionRunContext produce(Subject subject) {
    return produce(subject, null);
  }

  public ServerSessionRunContext produce(Subject subject, String sessionId) {
    final ServerSessionRunContext serverRunContext = ServerSessionRunContexts.copyCurrent(true)
        .withSubject(subject)
        .withUser(BEANS.get(IAccessControlService.class).getUser(subject))
        .withTransactionScope(TransactionScope.REQUIRES_NEW);

    // ensure that the session belongs to the specified subject
    // use the current set subject as subject of the session, because if the session is not null it must be the current session
    IServerSession session = serverRunContext.getSession();
    if (session == null || ObjectUtility.notEquals(Subject.current(), subject)) {
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(sessionId, serverRunContext.copy()));
    }

    return serverRunContext;
  }
}
