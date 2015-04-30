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
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IChainable;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of execution with the job name.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @since 5.1
 * @see <i>design pattern: chain of responsibility</i>
 */
public class ThreadNameDecorator<RESULT> implements Callable<RESULT>, IChainable<Callable<RESULT>> {

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
    m_jobName = jobName;
  }

  @Override
  public RESULT call() throws Exception {
    final ThreadInfo currentThreadInfo = ThreadInfo.CURRENT.get();

    final AtomicLong lastEventTime = new AtomicLong(System.nanoTime());

    // Install job listener to decorate the thread's name once being blocked.
    final IJobListener listener = Jobs.getJobManager().addListener(Jobs.newEventFilter().currentFuture().eventTypes(JobEventType.BLOCKED, JobEventType.UNBLOCKED, JobEventType.RESUMED), new IJobListener() {

      @Override
      public void changed(final JobEvent event) {
        synchronized (lastEventTime) { // synchronize event handling because being fired asynchronously.
          if (event.getNanoTime() < lastEventTime.get()) {
            return; // this event is out-of-date
          }
          lastEventTime.set(event.getNanoTime());

          switch (event.getType()) {
            case BLOCKED:
              currentThreadInfo.updateState(JobState.Blocked, event.getBlockingCondition().getName());
              break;
            case UNBLOCKED:
              currentThreadInfo.updateState(JobState.Resuming, event.getBlockingCondition().getName());
              break;
            case RESUMED:
              currentThreadInfo.updateState(JobState.Running, null);
              break;
          }
        }
      }
    });

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
