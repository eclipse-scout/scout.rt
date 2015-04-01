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
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.junit.runners.model.Statement;

/**
 * Statement to run the following statements within a <code>ServerRunContext</code>.
 *
 * @since 5.1
 */
public class ServerRunContextStatement extends Statement {

  protected final Statement m_next;

  /**
   * Creates a statement to run the following statements within a <code>ServerRunContext</code>.
   *
   * @param next
   *          next {@link Statement} to be executed.
   */
  public ServerRunContextStatement(final Statement next) {
    m_next = Assertions.assertNotNull(next, "next statement must not be null");
  }

  @Override
  public void evaluate() throws Throwable {
    ServerRunContexts.copyCurrent().run(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          m_next.evaluate();
        }
        catch (final Exception e) {
          throw e;
        }
        catch (final Throwable e) {
          throw new Error(e);
        }
      }
    });
  }
}
