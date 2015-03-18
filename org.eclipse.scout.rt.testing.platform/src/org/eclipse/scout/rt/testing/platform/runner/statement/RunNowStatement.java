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
package org.eclipse.scout.rt.testing.platform.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a job.
 *
 * @since5.1
 */
public class RunNowStatement extends Statement {

  protected final Statement m_next;
  private final JobInput<?> m_jobInput;

  /**
   * Creates a statement to run the following statements within a job.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param jobInput
   *          {@link JobInput} to be given to the 'runNow' call.
   */
  public RunNowStatement(final Statement next, final JobInput<?> jobInput) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_jobInput = Assertions.assertNotNull(jobInput, "job input must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final Holder<Throwable> throwable = new Holder<>();

    Jobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Throwable t) {
          throwable.setValue(t);
        }
      }
    }, m_jobInput);

    if (throwable.getValue() != null) {
      throw throwable.getValue();
    }
  }
}
