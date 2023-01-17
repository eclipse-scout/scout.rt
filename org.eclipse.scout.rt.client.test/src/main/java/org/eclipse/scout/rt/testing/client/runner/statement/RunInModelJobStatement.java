/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.client.runner.statement;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.platform.runner.SafeStatementInvoker;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements as model job.
 *
 * @since 5.1
 */
public class RunInModelJobStatement extends Statement {

  protected final Statement m_next;

  public RunInModelJobStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    if (ModelJobs.isModelThread()) {
      m_next.evaluate();
    }
    else {
      final SafeStatementInvoker invoker = new SafeStatementInvoker(m_next);
      ModelJobs.schedule(invoker, ModelJobs.newInput(ClientRunContexts.copyCurrent())
          .withName("Running JUnit test in model job"))
          .awaitDone();
      invoker.throwOnError();
    }
  }
}
