/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.testing.server.runner.statement;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.clientnotification.TransactionalClientNotificationCollector;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.testing.server.runner.RunWithClientNotifications;
import org.junit.runners.model.Statement;

public class ClientNotificationsStatement extends Statement {

  private final Statement m_next;
  private final String m_clientNodeId;
  private final TransactionalClientNotificationCollector m_collector;

  /**
   * Creates a statement to execute the following statements with client notification thread locals set to simulate a
   * client request.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param annotation
   *          {@link RunWithClientNotifications}-annotation to read nodeId
   */
  public ClientNotificationsStatement(final Statement next, final RunWithClientNotifications annotation) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_clientNodeId = (annotation != null ? annotation.clientNodeId() : null);
    m_collector = new TransactionalClientNotificationCollector();
  }

  @Override
  public void evaluate() throws Throwable {
    ServerRunContexts.copyCurrent()
        .withTransactionalClientNotificationCollector(m_collector)
        .withClientNodeId(m_clientNodeId)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              m_next.evaluate();
            }
            catch (final Exception | Error e) {
              throw e;
            }
            catch (final Throwable e) {
              throw new Error(e);
            }
          }
        }, BEANS.get(ThrowableTranslator.class));
  }
}
