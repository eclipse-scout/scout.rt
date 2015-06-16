package org.eclipse.scout.rt.testing.client.runner.statement;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/**
 * Statement for executing tests with a timeout (i.e. the annotated test method is expected to complete within the
 * specified amount of time). The given next statement is executed in a new model job. Hence this statement cannot be
 * evaluated within a model job.
 *
 * @see Test#timeout()
 * @since 5.1
 */
public class TimeoutClientRunContextStatement extends Statement {

  protected final Statement m_next;
  private final long m_timeoutMillis;

  public TimeoutClientRunContextStatement(final Statement next, final long timeoutMillis) {
    m_timeoutMillis = timeoutMillis;
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    if (ModelJobs.isModelThread()) {
      throw new IllegalStateException("Already running in a model job. but tests with max allowed runtime (i.e. @Test(timeout=...)) "
          + "cannot be nested into a model job. Check your test setup or remove timeout.");
    }
    else {
      IRunnable nestedRunnable = new IRunnable() {
        @Override
        public void run() throws Exception {
          try {
            m_next.evaluate();
          }
          catch (final Exception e) {
            throw new WrappingError(e);
          }
          catch (final Error e) {
            throw e;
          }
          catch (final Throwable t) {
            throw new Error(t);
          }
        }
      };

      IFuture<Void> future = ModelJobs.schedule(nestedRunnable, ModelJobs.newInput(ClientRunContexts.copyCurrent()).name("nested JUnit model job"));
      try {
        if (m_timeoutMillis <= 0) {
          future.awaitDoneAndGet();
        }
        else {
          future.awaitDoneAndGet(m_timeoutMillis, TimeUnit.MILLISECONDS);
        }
      }
      catch (WrappingError e) {
        throw e.getCause();
      }
      catch (ProcessingException e) {
        if (e.isTimeout()) {
          // waiting on the job to complete timed out. Try to cancel the job and translate exception into junit counterpart.
          future.cancel(true);
          throw new TestTimedOutException(m_timeoutMillis, TimeUnit.MILLISECONDS);
        }

        // re-throw any other job exception
        throw e;
      }
    }
  }

  private static class WrappingError extends Error {
    private static final long serialVersionUID = 1L;

    public WrappingError(Throwable paramThrowable) {
      super(paramThrowable);
    }
  }
}
