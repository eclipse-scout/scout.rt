/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.context;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.shared.user.UserId;

/**
 * Producer for {@link RunContext} objects.
 * <p>
 * The default implementation creates a copy of the current calling {@link RunContext}.
 *
 * @since 5.1
 */
@ApplicationScoped
public class ServerRunContextProducer extends RunContextProducer {

  /**
   * Creates a {@link ServerRunContext} for the specified {@link Subject}.
   */
  @Override
  public ServerRunContext produce(final Subject subject) {
    return ServerRunContexts.copyCurrent(true)
        .withSubject(subject)
        .withThreadLocal(UserId.CURRENT, BEANS.get(IAccessControlService.class).getUserId(subject))
        .withTransactionScope(TransactionScope.REQUIRES_NEW);
  }
}
