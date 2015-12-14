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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * Processor to decorate the thread name of the worker thread during the time of executing a job.
 * <p>
 * Instances of this class are to be added to a {@link CallableChain} to participate in the execution of a
 * {@link Callable}.
 *
 * @since 5.1
 */
public class ThreadNameDecorator<RESULT> implements ICallableDecorator<RESULT> {

  @Override
  public IUndecorator<RESULT> decorate() throws Exception {
    final ThreadInfo currentThreadInfo = ThreadInfo.CURRENT.get();
    if (currentThreadInfo == null) {
      return null;
    }

    final IFuture<?> future = IFuture.CURRENT.get();

    // Update the thread name.
    currentThreadInfo.updateThreadName(future.getJobInput().getThreadName(), buildThreadName(future));

    // Install listener to decorate the thread name upon job state change.
    final IJobListenerRegistration listenerRegistration = future.addListener(
        Jobs.newEventFilterBuilder()
            .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
            .andMatchState(
                JobState.RUNNING,
                JobState.WAITING_FOR_MUTEX,
                JobState.WAITING_FOR_BLOCKING_CONDITION)
            .toFilter(),
        new IJobListener() {

          @Override
          public void changed(final JobEvent event) {
            currentThreadInfo.updateThreadName(future.getJobInput().getThreadName(), buildThreadName(future));
          }
        });

    // Restore to the original thread name.
    return new IUndecorator<RESULT>() {

      @Override
      public void undecorate(final RESULT callableResult, final Throwable callableException) {
        listenerRegistration.dispose();
        currentThreadInfo.reset();
      }
    };
  }

  protected String buildThreadName(final IFuture<?> future) {
    return String.format("(%s) '%s'", future.getState(), future.getJobInput().getName());
  }
}
