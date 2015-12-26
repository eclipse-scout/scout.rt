/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.context;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Producer for {@link ServerRunContext} objects.
 * <p>
 * The default implementation creates a copy of the current calling {@link ServerRunContext} with transaction scope
 * {@link TransactionScope#REQUIRES_NEW}. If no session is associated yet, it is obtained by
 * {@link ServerSessionProviderWithCache}.
 *
 * @since 5.1
 */
public class ServerRunContextProducer extends RunContextProducer {

  /**
   * Produces a {@link ServerRunContext} for the given {@link Subject}.
   */
  @Override
  public ServerRunContext produce(final Subject subject) {
    final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
        .withSubject(subject)
        .withTransactionScope(TransactionScope.REQUIRES_NEW);

    // Ensure a session to be associated with belongs to the current subject.
    if (serverRunContext.getSession() == null || CompareUtility.notEquals(serverRunContext.getSubject(), subject)) {
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
    }

    return serverRunContext;
  }
}
