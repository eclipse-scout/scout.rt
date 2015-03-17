/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.testing.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.rt.client.job.IModelJobManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.testing.shared.WaitCondition;

/**
 *
 */
public abstract class AbstractGuiMock implements IGuiMock {

  private IClientSession m_session;

  protected AbstractGuiMock(IClientSession session) {
    setClientSession(session);
  }

  @Override
  public <T> T invokeScoutAndWait(final WaitCondition<T> r, long startTimeout, long runTimeout) throws Throwable {
    final long deadLine = startTimeout > 0 ? System.nanoTime() + startTimeout : -1;
    final AtomicReference<T> ret = new AtomicReference<T>(null);
    final AtomicReference<Throwable> throwables = new AtomicReference<Throwable>(null);

    IClientSession clientSession = getClientSession();
    IFuture<Void> future = OBJ.get(IModelJobManager.class).schedule(new IRunnable() {
      @Override
      public void run() throws Exception {
        if (deadLine < 0 || deadLine > System.nanoTime()) {
          try {
            ret.set(r.run());
          }
          catch (Throwable e) {
            throwables.set(e);
          }
        }
      }
    }, ClientJobInput.defaults().session(clientSession).name(r.toString()));

    future.get(runTimeout, TimeUnit.MILLISECONDS);
    Throwable t = throwables.get();
    if (t != null) {
      throw t;
    }

    waitForIdle(); // give the UI time to process all events that have been scheduled by the scout model runnable
    return ret.get();
  }

  @Override
  public IClientSession getClientSession() {
    return m_session;
  }

  public void setClientSession(IClientSession session) {
    m_session = session;
  }
}
