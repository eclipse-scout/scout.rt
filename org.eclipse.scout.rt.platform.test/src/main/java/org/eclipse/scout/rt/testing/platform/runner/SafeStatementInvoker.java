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
package org.eclipse.scout.rt.testing.platform.runner;

import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.junit.runners.model.Statement;

/**
 * Runnable which invokes a {@link Statement}, but does not propagate a possible {@link Throwable}.
 * <p>
 * Use {@link #throwOnError()} to propagate a possible {@link Throwable}.
 * <p>
 * This class is necessary, because {@link IRunnable} cannot propagate {@link Throwable}s.
 *
 * @since 5.2
 */
public class SafeStatementInvoker implements IRunnable {

  private volatile Throwable m_throwable;
  private final Statement m_next;

  public SafeStatementInvoker(final Statement next) {
    m_next = next;
  }

  @Override
  public void run() throws Exception {
    try {
      m_next.evaluate();
    }
    catch (final Throwable t) {
      m_throwable = t;
    }
  }

  /**
   * Throws the {@link Throwable} if catched during execution.
   */
  public void throwOnError() throws Throwable {
    if (m_throwable != null) {
      throw m_throwable;
    }
  }
}
