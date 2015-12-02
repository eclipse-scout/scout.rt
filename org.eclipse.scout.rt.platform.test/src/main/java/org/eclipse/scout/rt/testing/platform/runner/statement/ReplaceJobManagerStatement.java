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

import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.testing.platform.job.JobTestUtil;
import org.junit.runners.model.Statement;

/**
 * Statement to replace the current {@link JobManager} with a new dedicated instance of {@link JUnitJobManager} during
 * the execution of the subsequent statements.
 *
 * @since 5.2
 */
public class ReplaceJobManagerStatement extends Statement {

  protected final Statement m_next;

  public ReplaceJobManagerStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final IBean<IJobManager> jobManagerBean = JobTestUtil.replaceCurrentJobManager(new JUnitJobManager());
    try {
      m_next.evaluate();
    }
    finally {
      JobTestUtil.unregisterAndShutdownJobManager(jobManagerBean);
    }
  }

  /**
   * This job manager replaces {@link JobManager} and a new dedicated instance is registered in the bean manager for
   * every test class. This prevents job interferences among test classes using a shared platform.
   */
  @IgnoreBean
  @Replace
  public static class JUnitJobManager extends JobManager {
  }
}
