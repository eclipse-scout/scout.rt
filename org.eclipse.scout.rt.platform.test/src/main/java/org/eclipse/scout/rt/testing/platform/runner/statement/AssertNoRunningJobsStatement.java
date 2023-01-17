/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.fail;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.junit.runners.model.Statement;

/**
 * Statement to assert no running jobs after test execution to prevent job interferences among test classes using a
 * shared platform.
 *
 * @since 5.2
 */
public class AssertNoRunningJobsStatement extends Statement {

  private static final long AWAIT_DONE_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(1);

  private final Statement m_next;
  private final String m_context;

  public AssertNoRunningJobsStatement(final Statement next, final String context) {
    m_context = context;
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final ScheduledDescendantJobListener jobListener = new ScheduledDescendantJobListener();

    final IRegistrationHandle listenerRegistration = Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.SCHEDULED)
        .toFilter(), jobListener);
    try {
      // Continue the chain.
      m_next.evaluate();
    }
    finally {
      listenerRegistration.dispose();
    }

    final Set<IFuture<?>> scheduledFutures = jobListener.getScheduledFutures();
    if (!scheduledFutures.isEmpty()) {
      assertNoRunningJobs(Jobs.newFutureFilterBuilder()
          .andMatchFuture(scheduledFutures)
          .toFilter());
    }
  }

  /**
   * Asserts that all jobs accepted by the given filter are in 'done' state.
   */
  private void assertNoRunningJobs(final Predicate<IFuture<?>> jobFilter) {
    try {
      Thread.interrupted(); // clear the thread's interrupted status, in case the JUnit test interrupted the executing thread.
      Jobs.getJobManager().awaitDone(jobFilter, AWAIT_DONE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }
    catch (final TimedOutError e) { // NOSONAR
      final List<String> runningJobs = findJobNames(jobFilter);
      if (!runningJobs.isEmpty()) {
        fail(String.format("Test failed because some jobs did not complete yet. [context=%s, jobs=%s]", m_context, runningJobs));
      }
    }
  }

  /**
   * Finds running job names which comply with the given filter.
   */
  private List<String> findJobNames(final Predicate<IFuture<?>> filter) {
    final List<String> jobs = new ArrayList<>();
    for (final IFuture<?> future : Jobs.getJobManager().getFutures(filter)) {
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
    private final ConcurrentMap<IFuture<?>, Object> m_scheduledFutures;

    public ScheduledDescendantJobListener() {
      m_scheduledFutures = new ConcurrentHashMap<>();

      m_initialJobFuture = IFuture.CURRENT.get();
      m_initialThread = m_initialJobFuture == null ? Thread.currentThread() : null;
    }

    public Set<IFuture<?>> getScheduledFutures() {
      return m_scheduledFutures.keySet();
    }

    @Override
    public void changed(final JobEvent event) {
      if (isScheduledByInitialThread() || isScheduledByInitialJob() || isScheduledByDescendantJob()) {
        m_scheduledFutures.put(event.getData().getFuture(), PRESENT);
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
      final IFuture<?> future = IFuture.CURRENT.get();
      return future != null && m_scheduledFutures.containsKey(future);
    }
  }
}
