/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.junit.Test;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.model.Statement;

/**
 * JUnit runs tests with a timeout in a separate thread. This statement is a replace for {@link FailOnTimeout}, and
 * provides the current {@link RunContext} to the executing thread.
 * <p>
 * Note: The thread runs with a new transaction, because a transaction is only allowed to be accessed by the same
 * thread.
 *
 * @see Test#timeout()
 * @see FailOnTimeout
 * @since 5.1
 */
public class TimeoutRunContextStatement extends AbstractTimeoutRunContextStatement {

  public TimeoutRunContextStatement(final Statement next, final long timeoutMillis) {
    super(next, timeoutMillis);
  }

  @Override
  protected IFuture<Void> createFuture(IRunnable runnable) {
    RunMonitor newRunMonitor = BEANS.get(RunMonitor.class);
    return Jobs.schedule(runnable, Jobs.newInput()
        .withRunContext(RunContext.CURRENT.get().copy()
            .withRunMonitor(newRunMonitor)
            .withParentRunMonitor(RunMonitor.CURRENT.get())
            .withTransactionScope(TransactionScope.REQUIRES_NEW)) // Run in new TX, because the same TX is not allowed to be used by multiple threads.
        .withName("Running test with support for JUnit timeout"));
  }
}
