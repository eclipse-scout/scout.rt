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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.filter.AlwaysFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.visitor.CollectorVisitor;
import org.junit.runners.model.Statement;

/**
 * Statement to assert no running jobs after test execution to prevents job interferences among test classes using a
 * shared platform.
 *
 * @since 5.2
 */
public class AssertNoRunningJobsStatement extends Statement {

  private static final long AWAIT_DONE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(15);
  private static final IFilter<IFuture<?>> ALL_FUTURES_FILTER = new AlwaysFilter<IFuture<?>>();

  private final Statement m_next;

  public AssertNoRunningJobsStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    // Filter to match all jobs, which are started from now on.
    final IFilter<IFuture<?>> runningJobsFilter = Jobs.newFutureFilterBuilder()
        .andMatchNotFuture(findFutures(ALL_FUTURES_FILTER))
        .toFilter();

    // Continue the chain.
    m_next.evaluate();

    // Assert to have no running jobs.
    assertNoRunningJobs(runningJobsFilter);
  }

  /**
   * Asserts that all jobs accepted by the given filter are in 'done' state.
   */
  private void assertNoRunningJobs(final IFilter<IFuture<?>> jobFilter) {
    if (!Jobs.getJobManager().awaitDone(jobFilter, AWAIT_DONE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      final List<String> runningJobs = findJobNames(jobFilter);
      if (!runningJobs.isEmpty()) {
        fail(String.format("Test failed because some jobs did not complete yet. [jobs=%s]", runningJobs));
      }
    }
  }

  /**
   * Finds running futures which comply with the given filter.
   */
  private List<IFuture<?>> findFutures(final IFilter<IFuture<?>> filter) {
    final CollectorVisitor<IFuture<?>> futureCollector = new CollectorVisitor<>();
    Jobs.getJobManager().visit(filter, futureCollector);
    return futureCollector.getElements();
  }

  /**
   * Finds running job names which comply with the given filter.
   */
  private List<String> findJobNames(final IFilter<IFuture<?>> filter) {
    final List<String> jobs = new ArrayList<>();
    for (final IFuture<?> future : findFutures(filter)) {
      jobs.add(future.getJobInput().getName());
    }
    return jobs;
  }
}
