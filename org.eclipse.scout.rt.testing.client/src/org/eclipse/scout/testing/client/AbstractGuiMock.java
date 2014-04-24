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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
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
    final long deadLine = startTimeout > 0 ? System.currentTimeMillis() + startTimeout : -1;
    final AtomicReference<T> ret = new AtomicReference<T>(null);
    final AtomicReference<Throwable> throwables = new AtomicReference<Throwable>(null);

    ClientSyncJob eclipseJob = new ClientSyncJob(r.toString(), getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) {
        if (deadLine < 0 || deadLine > System.currentTimeMillis()) {
          try {
            ret.set(r.run());
          }
          catch (Throwable e) {
            throwables.set(e);
          }
        }
      }
    };
    eclipseJob.schedule();

    try {
      eclipseJob.join(runTimeout);
      Throwable t = throwables.get();
      if (t != null) {
        throw t;
      }

      waitForIdle(); // give the UI time to process all events that have been scheduled by the scout model runnable
      return ret.get();
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public IClientSession getClientSession() {
    return m_session;
  }

  public void setClientSession(IClientSession session) {
    m_session = session;
  }
}
