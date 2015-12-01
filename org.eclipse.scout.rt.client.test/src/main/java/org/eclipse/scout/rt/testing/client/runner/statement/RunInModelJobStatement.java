package org.eclipse.scout.rt.testing.client.runner.statement;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ThrowableTranslator;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements as model job.
 *
 * @since 5.1
 */
public class RunInModelJobStatement extends Statement {

  protected final Statement m_next;

  public RunInModelJobStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    if (ModelJobs.isModelThread()) {
      m_next.evaluate();
    }
    else {
      JobInput input = ModelJobs.newInput(ClientRunContexts.copyCurrent()).withName("JUnit model job");
      ModelJobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          try {
            m_next.evaluate();
          }
          catch (final Exception | Error e) {
            throw e;
          }
          catch (final Throwable t) {
            throw new Error(t);
          }
        }
      }, input).awaitDoneAndGet(BEANS.get(ThrowableTranslator.class));
    }
  }
}
