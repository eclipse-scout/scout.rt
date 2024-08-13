/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.testing.platform.runner.statement;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Statement to assert no running jobs after test execution to prevent job interferences among test classes using a
 * shared platform. Jobs marked with {@link JobInput#EXECUTION_HINT_TESTING_DO_NOT_WAIT_FOR_THIS_JOB} are ignored.
 *
 * @since 5.2
 */
public class AssertNoRunningJobsStatement extends Statement {
  private static final Logger LOG = LoggerFactory.getLogger(AssertNoRunningJobsStatement.class);
  private static final long AWAIT_DONE_TIMEOUT_NANOS = TimeUnit.MINUTES.toNanos(1);
  private static final long REPORT_THRESHOLD_NANOS = TimeUnit.MILLISECONDS.toNanos(500);

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
      assertNoRunningJobs(scheduledFutures);
    }
  }

  /**
   * Asserts that all jobs accepted by the given filter are in 'done' state.
   */
  private void assertNoRunningJobs(final Set<IFuture<?>> futures) {
    try {
      //noinspection ResultOfMethodCallIgnored
      Thread.interrupted(); // clear the thread's interrupted status, in case the JUnit test interrupted the executing thread.
      removeAndAwaitDone(futures);
    }
    catch (final TimedOutError e) { // NOSONAR
      final List<String> runningJobs = futures.stream()
          .filter(f -> !f.isDone()) // because of the TimedOutError there could still be done jobs in the list
          .map(f -> f.getJobInput().getName())
          .collect(Collectors.toList());
      if (!runningJobs.isEmpty()) {
        fail(String.format("Test failed because some jobs did not complete yet. [context=%s, jobs=%s]", m_context, runningJobs));
      }
    }
  }

  /**
   * Waits until the given futures are done and removes them.
   */
  private void removeAndAwaitDone(Set<IFuture<?>> futures) {
    // implementation note: we do not use IJobManager.awaitDone because we want to report jobs running longer than REPORT_THRESHOLD_NANOS.
    long nanosWaited = 0;
    boolean reported = false;
    while (nanosWaited < AWAIT_DONE_TIMEOUT_NANOS) {
      // remove all done jobs
      futures.removeIf(IFuture::isDone);
      if (futures.isEmpty()) {
        return;
      }

      final IFuture<?> next = futures.iterator().next();
      final long startNanos = System.nanoTime();
      next.awaitDone(AWAIT_DONE_TIMEOUT_NANOS - nanosWaited, TimeUnit.NANOSECONDS);
      nanosWaited += System.nanoTime() - startNanos;

      if (!reported && nanosWaited > REPORT_THRESHOLD_NANOS) {
        LOG.warn("The job '{}' did not complete within {}ms [took {}ms].\n"
            + "Hint: in case of deferred background operations that do not impact the test execution, consider using JobInput.EXECUTION_HINT_TESTING_DO_NOT_WAIT_FOR_THIS_JOB",
            next.getJobInput().getName(), TimeUnit.NANOSECONDS.toMillis(REPORT_THRESHOLD_NANOS), TimeUnit.NANOSECONDS.toMillis(nanosWaited));
        reported = true;
      }
    }
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
        final IFuture<?> future = event.getData().getFuture();
        // ignore specifically marked jobs
        if (future.containsExecutionHint(JobInput.EXECUTION_HINT_TESTING_DO_NOT_WAIT_FOR_THIS_JOB)) {
          return;
        }
        m_scheduledFutures.put(event.getData().getFuture(), PRESENT);
      }
    }

    /**
     * @return Returns <code>true</code> if the initial statement was not executed by a job and the current thread is
     *         the initial thread.
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
