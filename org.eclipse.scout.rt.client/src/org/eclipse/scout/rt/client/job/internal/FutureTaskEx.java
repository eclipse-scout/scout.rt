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
package org.eclipse.scout.rt.client.job.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * {@link FutureTask} does not propagate control to the {@link Callable} once the {@link Future} is
 * <code>cancelled</code> or in an invalid state. This class provides a 'before', 'after' and 'reject' hook which are
 * invoked for sure to properly acquire/release the mutex.
 *
 * @See {@link FutureTaskEx#run()}
 * @since 5.1
 */
@Internal
public final class FutureTaskEx<RESULT> extends FutureTask<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(FutureTaskEx.class);

  private final Task<RESULT> m_callable;

  public FutureTaskEx(final Task<RESULT> callable) {
    super(callable);
    m_callable = callable;
  }

  @Override
  public void run() {
    try {
      m_callable.onBefore();
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected exception before delegating control to the FutureTask", e);
    }

    try {
      super.run(); // delegate control to the FutureTask which in turn calls 'Callable#call'.
    }
    finally {
      try {
        m_callable.onAfter();
      }
      catch (final RuntimeException e) {
        LOG.error("Unexpected exception after delegating control to the FutureTask", e);
      }
    }
  }

  /**
   * Invoke this method if this {@link FutureTask} was rejected by the executor; the call is simply delegated to
   * {@link Task#onRejected()}.
   */
  public void reject() {
    m_callable.onRejected();
  }
}
