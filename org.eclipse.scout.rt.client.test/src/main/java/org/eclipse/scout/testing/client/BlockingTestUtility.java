/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.testing.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.ToIntFunction;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.messagebox.IMessageBox;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.JUnitExceptionHandler;

/**
 * Utility class to test code that enters a blocking condition.
 *
 * @see IBlockingCondition#waitFor(String...)
 * @see IBlockingCondition#waitFor(long, TimeUnit, String...)
 */
public final class BlockingTestUtility {

  private BlockingTestUtility() {
  }

  /**
   * Helper method to test code which will enter a blocking condition.
   * <p>
   * calls {@link #runBlockingAction(IRunnable, IRunnable, boolean)} with awaitBackgroundJobs=false
   */
  public static void runBlockingAction(final IRunnable runnableGettingBlocked, final IRunnable runnableOnceBlocked) {
    runBlockingAction(runnableGettingBlocked, runnableOnceBlocked, false);
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
   * @param awaitBackgroundJobs
   *          true waits for background jobs running in the same session to complete before runnableOnceBlocked is
   *          called
   */
  public static void runBlockingAction(final IRunnable runnableGettingBlocked, final IRunnable runnableOnceBlocked, final boolean awaitBackgroundJobs) {
    final ClientRunContext runContext = ClientRunContexts.copyCurrent();
    final IBlockingCondition onceBlockedDoneCondition = Jobs.newBlockingCondition(true);

    //remember the list of client jobs before blocking
    final Set<IFuture<?>> jobsBefore = new HashSet<>();
    jobsBefore.addAll(BEANS.get(IJobManager.class).getFutures(cand -> {
      final RunContext candContext = cand.getJobInput().getRunContext();
      return candContext instanceof ClientRunContext && ((ClientRunContext) candContext).getSession() == runContext.getSession();
    }));

    //end JobListener.changed
    final IRegistrationHandle listenerRegistration = IFuture.CURRENT.get().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .andMatchExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .toFilter(), event -> {
          //waitFor was entered

          final IRunnable callRunnableOnceBlocked = () -> {
            try {
              runnableOnceBlocked.run();
            }
            finally {
              event.getData().getBlockingCondition().setBlocking(false);
              onceBlockedDoneCondition.setBlocking(false);
            }
          };
          final JobInput jobInputForRunnableOnceBlocked = ModelJobs.newInput(runContext)
              .withExceptionHandling(BEANS.get(JUnitExceptionHandler.class), true)
              .withName("JUnit: Handling blocked thread because waiting for a blocking condition");

          if (awaitBackgroundJobs) {
            //wait until all background jobs finished
            Jobs.schedule(() -> {
              jobsBefore.add(IFuture.CURRENT.get());
              BEANS.get(IJobManager.class).awaitFinished(f -> {
                RunContext candContext = f.getJobInput().getRunContext();
                return candContext instanceof ClientRunContext && ((ClientRunContext) candContext).getSession() == runContext.getSession() && !jobsBefore.contains(f);
              }, 5, TimeUnit.MINUTES);

              //call runnableOnceBlocked
              ModelJobs.schedule(callRunnableOnceBlocked, jobInputForRunnableOnceBlocked);

            }, Jobs.newInput().withName("wait until background jobs finished"));

          }
          else {
            //call runnableOnceBlocked directly
            ModelJobs.schedule(callRunnableOnceBlocked, jobInputForRunnableOnceBlocked);
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

    // we need to wait until the runnableOnceBlocked is completed.
    // runnableOnceBlocked may, during its execution,  set the original blocking condition to non-blocking but still execute
    // important code afterwards. Therefore, the original blocking condition that starts runnableOnceBlocked is only used
    // to indicate the start of the runnableOnceBlocked, but this method returns only AFTER runnableOnceBlocked completes execution.
    onceBlockedDoneCondition.waitFor(120, TimeUnit.SECONDS);
  }

  /**
   * Used by {@link ClientTestRunner} to detect unexpected blocking conditions (forms)
   * <p>
   * Adds a blocking condition timeout listener on {@link IJobManager} for the current {@link IClientSession}
   *
   * @return the handle for the listener containing the first exception caught. Call
   *         {@link IRegistrationHandle#dispose()} to remove the listener.
   */
  public static IBlockingConditionTimeoutHandle addBlockingConditionTimeoutListener(long timeout, TimeUnit unit) {
    final BlockingConditionTimeoutListener listener = new BlockingConditionTimeoutListener(IClientSession.CURRENT.get(), timeout, unit);
    final IRegistrationHandle handle = BEANS.get(IJobManager.class).addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.WAITING_FOR_BLOCKING_CONDITION)
        .toFilter(), listener);
    return new IBlockingConditionTimeoutHandle() {
      @Override
      public Exception getFirstException() {
        return listener.m_firstException;
      }

      @Override
      public void dispose() {
        handle.dispose();
      }
    };
  }

  /**
   * Runs an action that will be blocked by a message box. All message boxes appearing during the execution of the
   * action are answered by the specified option.
   * <p>
   * If you need a distinguish multiple message boxes and give different answers, use
   * {@link #runBlockingActionWithMessageBoxHandler(IRunnable, ToIntFunction)} instead.
   * </p>
   *
   * @param runnable
   *          The action to be executed that will cause the message box to appear
   * @param messageBoxResult
   *          The return value for the message box. See {@link IMessageBox} for possible values.
   */
  public static void runBlockingActionWithMessageBoxDefaultResult(IRunnable runnable, int messageBoxResult) {
    ClientRunContexts.copyCurrent()
        .withThreadLocal(ClientSessionProviderWithCache.MESSAGE_BOX_HANDLER_STRATEGY, mBox -> messageBoxResult)
        .run(runnable);
  }

  /**
   * Runs an action that will be blocked by a message box. For each message box, the handling function is called to
   * determine the return value.
   * <p>
   * If you need don't need to different return values for different message boxes, you can use
   * {@link #runBlockingActionWithMessageBoxDefaultResult(IRunnable, int)} instead. See {@link IMessageBox} for possible
   * values.
   * </p>
   *
   * @param runnable
   *          The action to be executed that will cause the message box to appear
   * @param messageBoxHandler
   *          The handling function that will be used to determine the return value in the message box.
   */
  public static void runBlockingActionWithMessageBoxHandler(IRunnable runnable, ToIntFunction<IMessageBox> messageBoxHandler) {
    ClientRunContexts.copyCurrent()
        .withThreadLocal(ClientSessionProviderWithCache.MESSAGE_BOX_HANDLER_STRATEGY, messageBoxHandler)
        .run(runnable);
  }

  public interface IBlockingConditionTimeoutHandle extends IRegistrationHandle {
    Exception getFirstException();
  }

  private static class BlockingConditionTimeoutListener implements IJobListener {
    private final ISession m_session;
    private final long m_timeout;
    private final TimeUnit m_unit;
    private Exception m_firstException;

    BlockingConditionTimeoutListener(ISession session, long timeout, TimeUnit unit) {
      m_session = session;
      m_timeout = timeout;
      m_unit = unit;
    }

    @Override
    public void changed(final JobEvent event) {
      final IFuture<?> future = event.getData().getFuture();
      if (future == null) {
        return;
      }
      final IBlockingCondition blockingCondition = event.getData().getBlockingCondition();
      if (blockingCondition == null) {
        return;
      }
      RunContext runContext = event.getData().getFuture().getJobInput().getRunContext();
      if (!(runContext instanceof ClientRunContext)) {
        return;
      }
      ClientRunContext clientRunContext = (ClientRunContext) runContext;
      if (clientRunContext.getSession() != m_session) {
        return;
      }
      final Exception callerException = new PlatformException(
          "Testing detected a BlockingCondition that was not released after {} {}. Auto-unlocking the condition. Please check the test code and ensure that especially all forms are handled and closed correctly",
          m_timeout, m_unit);

      Jobs.schedule(() -> {
        try {
          blockingCondition.waitFor(m_timeout, m_unit);
        }
        catch (TimedOutError ex) { // NOSONAR
          //cancel future and unlock
          if (m_firstException == null) {
            m_firstException = callerException;
          }
          future.cancel(true);
          blockingCondition.setBlocking(false);
        }
      }, Jobs.newInput());
    }
  }

}
