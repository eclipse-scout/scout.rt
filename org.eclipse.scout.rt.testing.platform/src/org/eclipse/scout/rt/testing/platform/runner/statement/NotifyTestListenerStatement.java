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
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.testing.platform.ITestExecutionListener;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Statement to notify a potential registered {@link ITestExecutionListener} about test-execution.
 * 
 * @since5.1
 */
public class NotifyTestListenerStatement extends Statement {

  private final Statement m_next;
  private final Description m_description;

  /**
   * Creates a statement to notify a potential registered {@link ITestExecutionListener} about test-execution.
   *
   * @param next
   *          next {@link Statement} to be executed.
   * @param description
   *          test-description.
   */
  public NotifyTestListenerStatement(final Statement next, final Description description) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
    m_description = Assertions.assertNotNull(description, "test-description must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    final ITestExecutionListener listener = OBJ.getOptional(ITestExecutionListener.class);
    if (listener != null) {
      listener.beforeTestClass(m_description);
      try {
        m_next.evaluate();
      }
      finally {
        listener.afterTestClass(m_description);
      }
    }
    else {
      m_next.evaluate();
    }
  }
}
