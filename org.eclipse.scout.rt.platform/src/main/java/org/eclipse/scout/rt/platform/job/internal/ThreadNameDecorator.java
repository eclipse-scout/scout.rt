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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.IInvocationDecorator;
import org.eclipse.scout.rt.platform.chain.InvocationChain;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Processor to decorate the thread-name of the worker-thread during the time of executing a job.
 * <p>
 * Instances of this class are to be added to a {@link InvocationChain} to participate in the execution of a
 * {@link Callable}. #d
 *
 * @since 5.1
 */
public class ThreadNameDecorator<RESULT> implements IInvocationDecorator<RESULT> {

  private final String m_threadName;
  private final String m_jobName;

  public ThreadNameDecorator(final String threadName, final String jobName) {
    m_threadName = threadName;
    m_jobName = jobName;
  }

  @Override
  public IUndecorator<RESULT> decorate() throws Exception {
    final ThreadInfo currentThreadInfo = ThreadInfo.CURRENT.get();
    if (currentThreadInfo == null) {
      return null;
    }

    // Install job listener to decorate the thread name
    final IJobListenerRegistration listenerRegistration = IFuture.CURRENT.get().addListener(
        Jobs.newEventFilterBuilder()
            .andMatchEventType(
                JobEventType.BLOCKED,
                JobEventType.UNBLOCKED,
                JobEventType.RESUMED)
            .toFilter(),
        new IJobListener() {
          @Override
          public void changed(final JobEvent event) {
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
        });

    // Update the name of the thread.
    currentThreadInfo.updateNameAndState(m_threadName, m_jobName, JobState.Running);

    // Set origin thread name and state.
    return new IUndecorator<RESULT>() {

      @Override
      public void undecorate(final RESULT invocationResult, final Throwable invocationException) {
        listenerRegistration.dispose();
        currentThreadInfo.reset();
      }
    };
  }
}
