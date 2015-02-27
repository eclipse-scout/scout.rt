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
package org.eclipse.scout.rt.testing.commons;

/**
 * Callable to catch uncaught exceptions and offers functionality to re-throw them.
 */
public abstract class UncaughtExceptionRunnable implements Runnable {

  private volatile Throwable m_uncaughtException;

  @Override
  public final void run() {
    try {
      runSafe();
    }
    catch (final Throwable e) {
      m_uncaughtException = e;
      onUncaughtException(e);
    }
  }

  /**
   * Throws an uncaught exception; has no effect if there is no exception.
   */
  public void throwOnError() throws Throwable {
    if (m_uncaughtException != null) {
      throw m_uncaughtException;
    }
  }

  /**
   * Execute your code.
   */
  protected abstract void runSafe() throws Exception;

  /**
   * Method invoked in case of an uncaught exception.
   */
  protected void onUncaughtException(final Throwable t) {
  }

}
