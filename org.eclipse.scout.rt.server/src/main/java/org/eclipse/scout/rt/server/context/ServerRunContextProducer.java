/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContextProducer;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.shared.user.UserId;

/**
 * FIXME PBZ SESSION update javadoc
 */
public class ServerRunContextProducer extends RunContextProducer {

  /**
   * Creates a {@link ServerRunContext} for the specified {@link Subject}.
   */
  @Override
  public ServerRunContext produce(final Subject subject) {
    final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent(true)
        .withSubject(subject)
        .withThreadLocal(UserId.CURRENT, BEANS.get(IAccessControlService.class).getUserId(subject))
        .withTransactionScope(TransactionScope.REQUIRES_NEW);
    return serverRunContext;
  }
}
