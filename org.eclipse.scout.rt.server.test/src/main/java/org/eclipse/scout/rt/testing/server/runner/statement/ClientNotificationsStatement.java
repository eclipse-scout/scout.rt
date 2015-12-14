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
package org.eclipse.scout.rt.testing.server.runner.statement;

import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationCollector;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.eclipse.scout.rt.testing.server.runner.RunWithClientNotifications;
import org.junit.runners.model.Statement;

/**
 * Statement to have client notification support.
 * <p>
 * This statement requires to run in a new transaction.
 */
public class ClientNotificationsStatement extends Statement {

  private final Statement m_next;
  private final String m_clientNodeId;
  private final ClientNotificationCollector m_collector;

  public ClientNotificationsStatement(final Statement next, final RunWithClientNotifications annotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_clientNodeId = (annotation != null ? annotation.clientNodeId() : null);
    m_collector = new ClientNotificationCollector();
  }

  @Override
  public void evaluate() throws Throwable {
    final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);

    ServerRunContexts.copyCurrent()
        .withClientNotificationCollector(m_collector)
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withClientNodeId(m_clientNodeId)
        .run(invoker);

    invoker.throwOnError();
  }
}
