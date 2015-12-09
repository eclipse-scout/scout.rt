/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.busy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

//TODO [5.2] bsh: Use or remove BusiManager Code in Scout client
/**
 * The busy manager is the primary place to register/unregister {@link IBusyHandler} per {@link IClientSession}
 * <p>
 * This service is registered by default with priority -1000
 *
 * @author imo
 * @since 3.8
 */

public class BusyManagerService implements IBusyManagerService, IJobListener {
  private static final String HANDLER_CLIENT_SESSION_KEY = IBusyHandler.class.getName();

//  private IJobListenerRegistration m_jobListenerRegistration;

  public BusyManagerService() {
  }

  @PostConstruct
  public void initializeService() {
//    IFilter<JobEvent> clientOrModelJobFilter = new OrFilter<>(ClientJobs.newEventFilter(), ModelJobs.newEventFilter());
//    m_jobListenerRegistration = Jobs.getJobManager().addListener(Jobs.newEventFilter().andMatch(clientOrModelJobFilter).andMatchAnyEventType(JobEventType.ABOUT_TO_RUN, JobEventType.DONE), this);
  }

  @Override
  public void changed(JobEvent event) {
    switch (event.getType()) {
      case ABOUT_TO_RUN: {
        onJobAboutToRun(event.getFuture());
      }
      case DONE: {
        onJobEnd(event.getFuture());
      }
    }
  }

  @PreDestroy
  public void disposeServices() {
//    m_jobListenerRegistration.dispose();
  }

  private IBusyHandler getHandlerInternal(IFuture<?> future) {
    if (future.getJobInput().getRunContext() instanceof ClientRunContext) {
      return getHandler(((ClientRunContext) future.getJobInput().getRunContext()).getSession());
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

  protected void onJobAboutToRun(final IFuture<?> future) {
    final IBusyHandler handler = getHandlerInternal(future);
    if (handler == null) {
      return;
    }
    if (!handler.acceptFuture(future)) {
      return;
    }

    future.addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.BLOCKED)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(JobEvent blockedEvent) {
            handler.onJobEnd(future);
          }
        });

    future.addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.UNBLOCKED)
        .toFilter(), new IJobListener() {

          @Override
          public void changed(JobEvent blockedEvent) {
            handler.onJobBegin(future);
          }
        });

    handler.onJobBegin(future);
  }

  protected void onJobEnd(final IFuture<?> future) {
    IBusyHandler handler = getHandlerInternal(future);
    if (handler == null) {
      return;
    }
    if (!handler.acceptFuture(future)) {
      return;
    }

    handler.onJobEnd(future);
  }
}
