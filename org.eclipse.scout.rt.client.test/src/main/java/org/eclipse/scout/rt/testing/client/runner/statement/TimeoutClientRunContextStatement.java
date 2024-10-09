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
package org.eclipse.scout.rt.testing.client.runner.statement;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.statement.AbstractTimeoutRunContextStatement;
import org.junit.Test;
import org.junit.runners.model.Statement;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). The given next statement is executed in a new model job. Hence this statement cannot be
 * evaluated within a model job.
 *
 * @see Test#timeout()
 * @since 5.1
 */
public class TimeoutClientRunContextStatement extends AbstractTimeoutRunContextStatement {

  public TimeoutClientRunContextStatement(final Statement next, final long timeoutMillis) {
    super(next, timeoutMillis);
  }

  @Override
  protected IFuture<Void> createFuture(IRunnable runnable) {
    return ModelJobs.schedule(runnable, ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("Running test with support for JUnit timeout"));
  }
}
