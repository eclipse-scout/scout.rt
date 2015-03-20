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
package org.eclipse.scout.rt.platform.job.internal.callable;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.job.JobEventFilters;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of execution with the job name.
 * <p/>
 * This {@link Callable} is a processing object in the language of the design pattern 'chain-of-responsibility'.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 */
public class ThreadNameDecorator<RESULT> implements Callable<RESULT>, Chainable<RESULT> {

  private final Callable<RESULT> m_next;
  private final String m_threadName;
  private final String m_jobName;

  /**
   * Creates a processor to decorate the thread-name of the worker-thread with the job name.
   *
   * @param next
   *          next processor in the chain; must not be <code>null</code>.
   * @param threadName
   *          the name to be used as thread-name.
   * @param jobName
   *          the job's identifier to be appended to the thread-name.
   */
  public ThreadNameDecorator(final Callable<RESULT> next, final String threadName, final String jobName) {
    m_next = Assertions.assertNotNull(next);
    m_threadName = threadName;
    m_jobName = JobInput.N_A.equals(jobName) ? null : jobName;
  }

  @Override
  public RESULT call() throws Exception {
    final ThreadInfo currentThreadInfo = NamedThreadFactory.CURRENT_THREAD_INFO.get();

    // Install job listener to decorate the thread's name once being blocked.
    final IJobListener listener = Jobs.getJobManager().addListener(new IJobListener() {

      @Override
      public void changed(final JobEvent event) {
        switch (event.getType()) {
          case BLOCKED:
            currentThreadInfo.updateState(JobState.Blocked, event.getBlockingCondition().getName());
            break;
          case UNBLOCKED:
            currentThreadInfo.updateState(JobState.Running, null);
            break;
        }
      }
    }, JobEventFilters.allFilter().currentFuture().eventTypes(JobEventType.BLOCKED, JobEventType.UNBLOCKED));

    // Update the name of the thread.
    currentThreadInfo.updateNameAndState(m_threadName, m_jobName, JobState.Running);
    try {
      return m_next.call();
    }
    finally {
      Jobs.getJobManager().removeListener(listener);
      currentThreadInfo.updateNameAndState(null, null, JobState.Idle);
    }
  }

  @Override
  public Callable<RESULT> getNext() {
    return m_next;
  }
}
