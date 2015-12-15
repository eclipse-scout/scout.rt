/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.testing.client;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to test code that enters a blocking condition.
 * <p>
 * TOOD [jgu] please move into scout testing module
 *
 * @see IBlockingCondition#waitFor()
 */
public final class BlockingTestUtility {

  private static final Logger LOG = LoggerFactory.getLogger(BlockingTestUtility.class);

  private BlockingTestUtility() {
  }

  /**
   * Helper method to test code which will enter a blocking condition.
   *
   * @param runnableGettingBlocked
   *          {@code IRunnable} that will enter a blocking condition.
   * @param runnableOnceBlocked
   *          {@code IRunnable} to be executed once the 'runnableGettingBlocked' enters a blocking condition.
   * @param closeBlockingFormsOnDone
   *          <code>true</code> to close all blocking Forms once 'runnableOnceBlocked' ends execution.
   * @throws RuntimeException
   *           thrown if the execution of 'runnableGettingBlocked' or 'runnableOnceBlocked' throws an exception.
   */
  @SafeVarargs
  public static void runBlockingAction(final IRunnable runnableGettingBlocked, final IRunnable runnableOnceBlocked, final Class<? extends IForm>... formsToClose) {
    final ClientRunContext runContext = ClientRunContexts.copyCurrent();

    final IJobListenerRegistration listenerRegistration = IFuture.CURRENT.get().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
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
                  for (final Class<? extends IForm> formToClose : formsToClose) {
                    final IForm form = runContext.getDesktop().findForm(formToClose);
                    if (form != null) {
                      LOG.info("closing form '{}' ({}).", form.getTitle(), form.getClass().getSimpleName());
                      form.doClose();
                    }
                  }
                }
              }
            }, ModelJobs.newInput(runContext)
                .withExceptionHandling(JUnitExceptionHandler.class, true)
                .withName("runnable once blocked"));
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
    }
  }
}
