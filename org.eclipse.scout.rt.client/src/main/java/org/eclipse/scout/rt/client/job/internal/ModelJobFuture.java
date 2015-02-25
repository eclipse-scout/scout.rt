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
import java.util.concurrent.RunnableFuture;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ClientJobInput;

/**
 * {@link RunnableFuture} with a {@link ClientJobInput} associated.
 * <p/>
 * This Future notifies the given Callable upon changing execution state. That is because {@link FutureTask} does not
 * propagate control to the {@link Callable} once the {@link Future} is <code>cancelled</code> or in an invalid state.
 * This class provides a 'before', 'after' and 'reject' hook which are invoked for sure to properly acquire/release the
 * mutex.
 *
 * @see RunnableFuture
 * @see ModelJobManager
 * @since 5.1
 */
public class ModelJobFuture<RESULT> extends FutureTask<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelJobFuture.class);

  private final Task<RESULT> m_callable;
  private final ClientJobInput m_input;

  public ModelJobFuture(final Task<RESULT> callable, final ClientJobInput input) {
    super(callable);
    m_callable = callable;
    m_input = input;
  }

  @Override
  public final void run() {
    try {
      m_callable.beforeExecute(m_callable.getFuture());
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected exception before delegating control to the FutureTask", e);
    }

    try {
      super.run(); // delegate control to the FutureTask which in turn calls 'Callable#call'.
    }
    finally {
      try {
        m_callable.afterExecute(m_callable.getFuture());
      }
      catch (final RuntimeException e) {
        LOG.error("Unexpected exception after delegating control to the FutureTask", e);
      }
    }
  }

  /**
   * Invoke this method if the job belonging to this Future was rejected by the executor.
   */
  public final void reject() {
    m_callable.rejected(m_callable.getFuture());
  }

  public ClientJobInput getInput() {
    return m_input;
  }
}
