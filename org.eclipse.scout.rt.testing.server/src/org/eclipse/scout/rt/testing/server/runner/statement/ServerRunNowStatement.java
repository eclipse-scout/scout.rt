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
package org.eclipse.scout.rt.testing.server.runner.statement;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a job.
 *
 * @since5.1
 */
public class ServerRunNowStatement extends Statement {

  protected final Statement m_next;

  /**
   * Creates a statement to run the following statements within a job.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param jobInput
   *          {@link JobInput} to be given to the 'runNow' call.
   */
  public ServerRunNowStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final Holder<Throwable> throwable = new Holder<>();

    ServerJobs.runNow(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Throwable t) {
          throwable.setValue(t);
        }
      }
    });

    if (throwable.getValue() != null) {
      throw throwable.getValue();
    }
  }
}
