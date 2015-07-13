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
package org.eclipse.scout.rt.client.busy;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.job.ClientJobs;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.service.AbstractService;

/**
 * The busy manager is the primary place to register/unregister {@link IBusyHandler} per {@link IClientSession}
 * <p>
 * This service is registered by default with priority -1000
 *
 * @author imo
 * @since 3.8
 */

public class BusyManagerService extends AbstractService implements IBusyManagerService {
  private static final String HANDLER_CLIENT_SESSION_KEY = IBusyHandler.class.getName();

  private final IJobListener m_jobChangeListener;

  public BusyManagerService() {
    m_jobChangeListener = new P_JobChangeListener();
  }

  @Override
  public void initializeService() {
    super.initializeService();
    Jobs.getJobManager().addListener(Jobs.newEventFilter(), m_jobChangeListener);
  }

  @Override
  public void disposeServices() {
    try {
      Jobs.getJobManager().removeListener(m_jobChangeListener);
    }
    finally {
      super.disposeServices();
    }
  }

  private IBusyHandler getHandlerInternal(IFuture<?> future) {
    if (ClientJobs.isClientJob(future) || ModelJobs.isModelJob(future)) {
      return getHandler(((ClientRunContext) future.getJobInput().runContext()).getSession());
    }
    else {
      return null;
    }
  }

  @Override
  public IBusyHandler getHandler(IClientSession session) {
    if (session != null) {
      return (IBusyHandler) session.getData(HANDLER_CLIENT_SESSION_KEY);
    }
    return null;
  }

  @Override
  public synchronized void register(IClientSession session, IBusyHandler handler) {
    if (session == null || handler == null) {
      return;
    }
    handler.setEnabled(true);
    session.setData(HANDLER_CLIENT_SESSION_KEY, handler);
  }

  @Override
  public synchronized void unregister(IClientSession session) {
    if (session == null) {
      return;
    }
    IBusyHandler handler = getHandler(session);
    if (handler != null) {
      handler.setEnabled(false);
    }
    session.setData(HANDLER_CLIENT_SESSION_KEY, null);
  }

  private class P_JobChangeListener implements IJobListener {

    private Map<IFuture, IJobListener> m_listeners = new HashMap<>();

    private void running(JobEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      if (!handler.acceptFuture(future)) {
        return;
      }

      IJobListener listener = new P_JobChangeListenerEx();
      m_listeners.put(future, listener);
      Jobs.getJobManager().addListener(Jobs.newEventFilter().andMatchFutures(future), listener);

      handler.onJobBegin(future);
    }

    private void done(JobEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      if (!handler.acceptFuture(future)) {
        return;
      }

      Jobs.getJobManager().removeListener(m_listeners.remove(future));
      handler.onJobEnd(future);
    }

    @Override
    public void changed(JobEvent event) {
      switch (event.getType()) {
        case ABOUT_TO_RUN:
          running(event);
          break;
        case DONE:
          done(event);
          break;
      }
    }
  }

  private class P_JobChangeListenerEx implements IJobListener {
    private void blockingConditionStart(JobEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      handler.onJobEnd(future);
    }

    private void blockingConditionEnd(JobEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      handler.onJobBegin(future);
    }

    @Override
    public void changed(JobEvent event) {
      switch (event.getType()) {
        case BLOCKED:
          blockingConditionStart(event);
          break;
        case UNBLOCKED:
          blockingConditionEnd(event);
          break;
      }
    }
  }
}
