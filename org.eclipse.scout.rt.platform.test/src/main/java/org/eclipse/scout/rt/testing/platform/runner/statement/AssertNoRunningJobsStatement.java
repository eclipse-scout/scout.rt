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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
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

  private final Statement m_next;
  private final String m_context;

  public AssertNoRunningJobsStatement(final Statement next, String context) {
    m_context = context;
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final ScheduledDescendantJobListener jobListener = new ScheduledDescendantJobListener();

    IJobListenerRegistration reg = null;
    try {
      reg = Jobs.getJobManager().addListener(jobListener);

      // Continue the chain.
      m_next.evaluate();
    }
    finally {
      if (reg != null) {
        reg.dispose();
      }
    }

    Set<IFuture<?>> scheduledFutures = jobListener.getScheduledFutures();
    if (!scheduledFutures.isEmpty()) {
      IFilter<IFuture<?>> filter = Jobs.newFutureFilterBuilder().andMatchFuture(scheduledFutures).toFilter();
      assertNoRunningJobs(filter);
    }
  }

  /**
   * Asserts that all jobs accepted by the given filter are in 'done' state.
   */
  private void assertNoRunningJobs(final IFilter<IFuture<?>> jobFilter) {
    if (!Jobs.getJobManager().awaitDone(jobFilter, AWAIT_DONE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
      final List<String> runningJobs = findJobNames(jobFilter);
      if (!runningJobs.isEmpty()) {
        fail(String.format("Test failed because some jobs did not complete yet. [context=%s, jobs=%s]", m_context, runningJobs));
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

  /**
   * Job listener that keeps track of all descendant jobs scheduled by the job or thread which is executing the test.
   */
  public static class ScheduledDescendantJobListener implements IJobListener {

    private static final Object PRESENT = new Object();

    private final Thread m_initialThread;
    private final IFuture<?> m_initialJobFuture;
    private final ConcurrentHashMap<IFuture<?>, Object> m_scheduledFutures;

    public ScheduledDescendantJobListener() {
      m_scheduledFutures = new ConcurrentHashMap<>();

      m_initialJobFuture = IFuture.CURRENT.get();
      m_initialThread = m_initialJobFuture == null ? Thread.currentThread() : null;
    }

    @org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement //FIXME sle remove when https://github.com/mojohaus/animal-sniffer/issues/8 is solved
    public Set<IFuture<?>> getScheduledFutures() {
      return m_scheduledFutures.keySet();
    }

    @Override
    public void changed(JobEvent event) {
      if (event.getType() != JobEventType.SCHEDULED) {
        return;
      }

      if (isScheduledByInitialThread() || isScheduledByInitialJob() || isScheduledByDescendantJob()) {
        IFuture<?> future = event.getFuture();
        if (future == null) {
          throw new IllegalStateException("event.getFuture() is not expected to be null");
        }
        m_scheduledFutures.put(future, PRESENT);
      }
    }

    /**
     * @return Returns <code>true</code> if the the initial statement was not executed by a job and the current thread
     *         is the initial thread.
     */
    private boolean isScheduledByInitialThread() {
      return m_initialThread != null && m_initialThread == Thread.currentThread();
    }

    /**
     * @return Returns <code>true</code> if the initial statement was executed by a job and the currently running job is
     *         the initial job.
     */
    private boolean isScheduledByInitialJob() {
      return m_initialJobFuture != null && m_initialJobFuture == IFuture.CURRENT.get();
    }

    /**
     * @return Returns <code>true</code> if the currently running job has been directly or indirectly scheduled by the
     *         initial thread or the initial job.
     */
    private boolean isScheduledByDescendantJob() {
      IFuture<?> future = IFuture.CURRENT.get();
      return future != null && m_scheduledFutures.containsKey(future);
    }
  }
}
