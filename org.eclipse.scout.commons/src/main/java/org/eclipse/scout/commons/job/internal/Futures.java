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
package org.eclipse.scout.commons.job.internal;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.Executables.CallableWithJobInput;
import org.eclipse.scout.commons.job.internal.Executables.RunnableWithJobInput;
import org.eclipse.scout.commons.job.internal.callable.ExceptionTranslator;

/**
 * Factory and utility methods to create future objects.
 *
 * @since 5.1
 */
@Internal
public class Futures {

  private Futures() {
    // private constructor for utility classes.
  }

  /**
   * Creates a {@link JobFuture} that delegates to the given Future and contains the {@link IFuture}.
   */
  public static <RESULT> JobFuture<RESULT> jobFuture(final RunnableScheduledFuture<RESULT> delegate, final RunnableWithJobInput runnable, final IProgressMonitorProvider progressMonitorProvider) {
    return Futures.jobFuture(delegate, runnable.getInput(), progressMonitorProvider);
  }

  /**
   * Creates a {@link JobFuture} that delegates to the given Future and contains the {@link IFuture}.
   */
  public static <RESULT> JobFuture<RESULT> jobFuture(final RunnableScheduledFuture<RESULT> delegate, final CallableWithJobInput callable, final IProgressMonitorProvider progressMonitorProvider) {
    return Futures.jobFuture(delegate, callable.getInput(), progressMonitorProvider);
  }

  /**
   * Creates a {@link JobFuture} that delegates to the given Future and contains the {@link IFuture}.
   */
  public static <RESULT> JobFuture<RESULT> jobFuture(final RunnableScheduledFuture<RESULT> delegate, final IJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    return new JobFuture<>(delegate, input, progressMonitorProvider);
  }

  /**
   * Creates a 'runNow'-style {@link JobFuture} that delegates to the given Future and contains the {@link IFuture}.
   */
  public static <RESULT> JobFuture<RESULT> runNowJobFuture(final IJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    return Futures.jobFuture(new JobRunNowFuture<RESULT>(), input, progressMonitorProvider);
  }

  /**
   * Creates a {@link IFuture} that delegates to the given {@link Future}.
   */
  public static <RESULT> IFuture<RESULT> iFuture(final Future<RESULT> delegate, final IJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    Assertions.assertNotNull(delegate);
    Assertions.assertNotNull(input);

    final AtomicReference<IProgressMonitor> progressMonitor = new AtomicReference<>();

    return new IFuture<RESULT>() {

      public IFuture<RESULT> init() {
        progressMonitor.set(progressMonitorProvider.create(this));
        return this;
      }

      @Override
      public IJobInput getJobInput() {
        return input;
      }

      @Override
      public IProgressMonitor getProgressMonitor() {
        return progressMonitor.get();
      }

      @Override
      public Future<?> getDelegate() {
        return delegate;
      }

      // === Delegate methods to Future ===

      @Override
      public boolean cancel(final boolean interruptIfRunning) {
        return delegate.cancel(interruptIfRunning);
      }

      @Override
      public boolean isCancelled() {
        return delegate.isCancelled();
      }

      @Override
      public boolean isDone() {
        return delegate.isDone();
      }

      @Override
      public RESULT get() throws ProcessingException, JobExecutionException {
        try {
          return delegate.get();
        }
        catch (final ExecutionException e) {
          throw ExceptionTranslator.translate(e.getCause());
        }
        catch (final CancellationException e) {
          throw ExceptionTranslator.translateCancellationException(e, input.getIdentifier());
        }
        catch (final InterruptedException e) {
          throw ExceptionTranslator.translateInterruptedException(e, input.getIdentifier());
        }
        catch (final RuntimeException e) {
          throw ExceptionTranslator.translate(e);
        }
      }

      @Override
      public RESULT get(final long timeout, final TimeUnit unit) throws ProcessingException, JobExecutionException {
        try {
          return delegate.get(timeout, unit);
        }
        catch (final ExecutionException e) {
          throw ExceptionTranslator.translate(e.getCause());
        }
        catch (final CancellationException e) {
          throw ExceptionTranslator.translateCancellationException(e, input.getIdentifier());
        }
        catch (final InterruptedException e) {
          throw ExceptionTranslator.translateInterruptedException(e, input.getIdentifier());
        }
        catch (final TimeoutException e) {
          throw ExceptionTranslator.translateTimeoutException(e, timeout, unit, input.getIdentifier());
        }
        catch (final RuntimeException e) {
          throw ExceptionTranslator.translate(e);
        }
      }
    }.init();
  }

  // === Future classes ===

  /**
   * Delegator for {@link RunnableScheduledFuture}.
   */
  protected static class RunnableScheduledFutureDelegate<RESULT> implements RunnableScheduledFuture<RESULT> {

    private final RunnableScheduledFuture<RESULT> m_delegate;

    private RunnableScheduledFutureDelegate() {
      this(null);
    }

    protected RunnableScheduledFutureDelegate(final RunnableScheduledFuture<RESULT> delegate) {
      m_delegate = delegate;
    }

    protected RunnableScheduledFuture<RESULT> getDelegate() {
      return m_delegate;
    }

    @Override
    public long getDelay(final TimeUnit unit) {
      throwIfNullDelegate();
      return m_delegate.getDelay(unit);
    }

    @Override
    public void run() {
      throwIfNullDelegate();
      m_delegate.run();
    }

    @Override
    public boolean isPeriodic() {
      throwIfNullDelegate();
      return m_delegate.isPeriodic();
    }

    @Override
    public boolean cancel(final boolean interruptIfRunning) {
      throwIfNullDelegate();
      return m_delegate.cancel(interruptIfRunning);

    }

    @Override
    public int compareTo(final Delayed o) {
      throwIfNullDelegate();
      return m_delegate.compareTo(o);

    }

    @Override
    public boolean isCancelled() {
      throwIfNullDelegate();
      return m_delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
      throwIfNullDelegate();
      return m_delegate.isDone();
    }

    @Override
    public RESULT get() throws InterruptedException, ExecutionException {
      throwIfNullDelegate();
      return m_delegate.get();
    }

    @Override
    public RESULT get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
      throwIfNullDelegate();
      return m_delegate.get(timeout, unit);
    }

    private void throwIfNullDelegate() {
      if (m_delegate == null) {
        throw new UnsupportedOperationException();
      }
    }
  }

  /**
   * Delegator for {@link RunnableScheduledFuture} with a {@link IFuture}.
   */
  public static class JobFuture<RESULT> extends RunnableScheduledFutureDelegate<RESULT> {

    private final IFuture<RESULT> m_future;

    /**
     * Creates a {@link JobFuture} that delegates to the given Future.
     */
    private JobFuture(final RunnableScheduledFuture<RESULT> delegate, final IJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
      super(delegate);
      m_future = Futures.iFuture(this, input, progressMonitorProvider);
    }

    /**
     * Creates a {@link JobFuture} which delegates to the given {@link JobFuture}; used to extend the {@link JobFuture}
     * class.
     */
    protected JobFuture(final JobFuture<RESULT> delegate) {
      this(delegate, delegate.getFuture().getJobInput(), new IProgressMonitorProvider() {

        @Override
        public <R> IProgressMonitor create(final IFuture<R> future) {
          return delegate.getFuture().getProgressMonitor();
        }
      });
    }

    public IFuture<RESULT> getFuture() {
      return m_future;
    }
  }

  /**
   * Future that represents a Future used for 'runNow'-style execution.
   */
  private static class JobRunNowFuture<RESULT> extends RunnableScheduledFutureDelegate<RESULT> {

    private final Thread m_workerThread;
    private boolean m_cancelled;

    private JobRunNowFuture() {
      m_workerThread = Thread.currentThread();
    }

    @Override
    public boolean cancel(final boolean interruptIfRunning) {
      if (m_cancelled) {
        return false;
      }

      if (interruptIfRunning) {
        m_workerThread.interrupt();
      }

      return m_cancelled = true;
    }

    @Override
    public boolean isCancelled() {
      return m_cancelled;
    }

    @Override
    public boolean isDone() {
      return false;
    }
  }
}
