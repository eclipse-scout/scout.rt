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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContextProducer;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;

@Replace
public class ServerSessionRunContextProducer extends ServerRunContextProducer {

  @Override
  public ServerRunContext produce(Subject subject) {
    final ServerSessionRunContext serverRunContext = ServerSessionRunContexts.copyCurrent(true)
        .withSubject(subject)
        .withTransactionScope(TransactionScope.REQUIRES_NEW);

    // ensure that the session belongs to the specified subject
    // use the current set subject as subject of the session, because if the session is not null it must be the current session
    IServerSession session = serverRunContext.getSession();
    if (session == null || ObjectUtility.notEquals(Subject.current(), subject)) {
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
    }

    return serverRunContext;
  }
}
