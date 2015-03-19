package org.eclipse.scout.rt.testing.client.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a job.
 *
 * @since5.1
 */
public class ModelRunNowStatement extends Statement {

  protected final Statement m_next;

  /**
   * Creates a statement to run the following statements within a job.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param jobInput
   *          {@link JobInput} to be given to the 'runNow' call.
   */
  public ModelRunNowStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    Assertions.assertFalse(ModelJobs.isModelJob(), "already running within a model context");
    final Holder<Throwable> throwable = new Holder<>();

    IRunnable runnable = new IRunnable() {
      @Override
      public void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Throwable t) {
          throwable.setValue(t);
        }
      }
    };
    ModelJobs.schedule(runnable).awaitDone();

    if (throwable.getValue() != null) {
      throw throwable.getValue();
    }
  }
}
