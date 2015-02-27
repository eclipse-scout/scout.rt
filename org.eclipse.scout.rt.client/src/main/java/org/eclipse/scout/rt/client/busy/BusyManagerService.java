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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.job.FutureJobChangeEventFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobChangeEvent;
import org.eclipse.scout.commons.job.IJobChangeEventFilter;
import org.eclipse.scout.commons.job.IJobChangeListener;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.internal.JobChangeEvent;
import org.eclipse.scout.commons.job.internal.JobChangeListeners;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.job.ClientJobInput;
import org.eclipse.scout.service.AbstractService;

/**
 * The busy manager is the primary place to register/unregister {@link IBusyHandler} per {@link IClientSession}
 * <p>
 * This service is registered by default with priority -1000
 *
 * @author imo
 * @since 3.8
 */
@Priority(-1000)
public class BusyManagerService extends AbstractService implements IBusyManagerService {
  private static final String HANDLER_CLIENT_SESSION_KEY = IBusyHandler.class.getName();

  private final IJobChangeListener m_jobChangeListener;
  private final IJobChangeListener m_jobChangeListenerEx;

  public BusyManagerService() {
    m_jobChangeListener = new P_JobChangeListener();
    m_jobChangeListenerEx = new P_JobChangeListenerEx();
  }

  @Override
  public void initializeService() {
    super.initializeService();
    JobChangeListeners.DEFAULT.add(m_jobChangeListener);
  }

  @Override
  public void disposeServices() {
    try {
      JobChangeListeners.DEFAULT.remove(m_jobChangeListener);
    }
    finally {
      super.disposeServices();
    }
  }

  private IBusyHandler getHandlerInternal(IFuture<?> future) {
    if (future == null) {
      return null;
    }

    IJobInput input = future.getJobInput();
    if (input instanceof ClientJobInput) {
      ClientJobInput cji = (ClientJobInput) input;
      IClientSession session = cji.getSession();
      return getHandler(session);
    }
    return null;
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

  private class P_JobChangeListener implements IJobChangeListener {

    private final Map<IFuture<?>, IJobChangeEventFilter> m_filters = new HashMap<>();

    private void running(IJobChangeEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      if (!handler.acceptFuture(future, event.getSourceManager())) {
        return;
      }
      FutureJobChangeEventFilter eventFilter = new FutureJobChangeEventFilter(future);
      m_filters.put(future, eventFilter);
      JobChangeListeners.DEFAULT.add(m_jobChangeListenerEx, eventFilter);
      handler.onJobBegin(future);
    }

    private void done(IJobChangeEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      if (!handler.acceptFuture(future, event.getSourceManager())) {
        return;
      }
      IJobChangeEventFilter eventFilter = m_filters.get(future);
      JobChangeListeners.DEFAULT.remove(m_jobChangeListenerEx, eventFilter);
      handler.onJobEnd(future);
    }

    @Override
    public void jobChanged(IJobChangeEvent event) {
      if (event.getMode() == JobChangeEvent.EVENT_MODE_ASYNC) {
        if (event.getType() == JobChangeEvent.EVENT_TYPE_DONE) {
          done(event);
        }
        else if (event.getType() == JobChangeEvent.EVENT_TYPE_ABOUT_TO_RUN) {
          running(event);
        }
      }
    }
  }

  private class P_JobChangeListenerEx implements IJobChangeListener {
    private void blockingConditionStart(IJobChangeEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      handler.onJobEnd(future);
    }

    private void blockingConditionEnd(IJobChangeEvent event) {
      IFuture<?> future = event.getFuture();
      IBusyHandler handler = getHandlerInternal(future);
      if (handler == null) {
        return;
      }
      handler.onJobBegin(future);
    }

    @Override
    public void jobChanged(IJobChangeEvent event) {
      if (event.getType() == JobChangeEvent.EVENT_TYPE_BLOCKED) {
        blockingConditionStart(event);
      }
      else if (event.getType() == JobChangeEvent.EVENT_TYPE_UN_BLOCKED) {
        blockingConditionEnd(event);
      }
    }
  }
}
