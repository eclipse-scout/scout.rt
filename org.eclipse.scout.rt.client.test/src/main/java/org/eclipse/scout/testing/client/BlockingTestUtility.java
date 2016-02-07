/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.testing.client;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;

/**
 * Utility class to test code that enters a blocking condition.
 *
 * @see IBlockingCondition#waitFor(String...)
 * @see IBlockingCondition#waitFor(long, java.util.concurrent.TimeUnit, String...)
 */
public final class BlockingTestUtility {

  private BlockingTestUtility() {
  }

  /**
   * Helper method to test code which will enter a blocking condition.
   * <p>
   * If <code>runnableOnceBlocked</code> throws an exception, it is given to {@link JUnitExceptionHandler} to make the
   * JUnit test fail.
   *
   * @param runnableGettingBlocked
   *          {@code IRunnable} that will enter a blocking condition.
   * @param runnableOnceBlocked
   *          {@code IRunnable} to be executed once the 'runnableGettingBlocked' enters a blocking condition.
   */
  public static void runBlockingAction(final IRunnable runnableGettingBlocked, final IRunnable runnableOnceBlocked) {
    final ClientRunContext runContext = ClientRunContexts.copyCurrent();
    final IBlockingCondition onceBlockedDoneCondition = Jobs.newBlockingCondition(true);

    final IRegistrationHandle listenerRegistration = IFuture.CURRENT.get().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .andMatchExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(final JobEvent event) {
            ModelJobs.schedule(new IRunnable() {

              @Override
              public void run() throws Exception {
                try {
                  runnableOnceBlocked.run();
                }
                finally {
                  event.getData().getBlockingCondition().setBlocking(false);
                  onceBlockedDoneCondition.setBlocking(false);
                }
              }
            }, ModelJobs.newInput(runContext)
                .withExceptionHandling(BEANS.get(JUnitExceptionHandler.class), true)
                .withName("JUnit: Handling blocked thread because waiting for a blocking condition"));
          }
        });
    try {
      runnableGettingBlocked.run(); // this action will enter a blocking condition which causes the 'runnableOnceBlocked' to be executed.
    }
    catch (final Exception e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    finally {
      listenerRegistration.dispose();
      // we need to wait until the runnableOnceBlocked is completed.
      // runnableOnceBlocked may, during its execution,  set the original blocking condition to non-blocking but still execute
      // important code afterwards. Therefore, the original blocking condition that starts runnableOnceBlocked is only used
      // to indicate the start of the runnableOnceBlocked, but this method returns only AFTER runnableOnceBlocked completes execution.
      onceBlockedDoneCondition.waitForUninterruptibly(120, TimeUnit.SECONDS);
    }
  }
}
