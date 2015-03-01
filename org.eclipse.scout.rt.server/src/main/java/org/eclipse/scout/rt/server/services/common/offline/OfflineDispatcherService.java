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
package org.eclipse.scout.rt.server.services.common.offline;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.LinkedList;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.job.internal.ServerJobManager;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;

public class OfflineDispatcherService extends AbstractService implements IOfflineDispatcherService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OfflineDispatcherService.class);

  private Class<? extends IServerSession> m_serverSessionClass;
  private IServerSession m_serverSession;
  private Subject m_subject;
  private final Thread m_dispatcherThread;
  // queue
  private final Object m_queueLock;
  private final LinkedList<Runnable> m_queue;

  public OfflineDispatcherService() {
    m_queue = new LinkedList<Runnable>();
    m_queueLock = new Object();
    m_dispatcherThread = new Thread("Dispatcher for " + getClass().getName()) {
      @Override
      public void run() {
        // offline dispatcher thread is always in offline mode
        OfflineState.setOfflineInCurrentThread(true);
        while (true) {
          try {
            dispatchNextJob();
          }
          catch (Throwable t) {
            LOG.error("Error while executing job in offline dispatcher thread.", t);
          }
        }
      }
    };
    m_dispatcherThread.setDaemon(true);
    m_dispatcherThread.start();
  }

  @Override
  public String getServerSessionClass() {
    return (m_serverSessionClass != null ? m_serverSessionClass.getName() : null);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setServerSessionClass(String className) {
    int i = className.lastIndexOf('.');
    try {
      m_serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(className.substring(0, i)).loadClass(className);
    }
    catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Loading class " + className, e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public ServiceTunnelResponse dispatch(final IServiceTunnelRequest request, final IProgressMonitor monitor) {
    final Subject subject = Subject.getSubject(AccessController.getContext());
    if (m_serverSessionClass == null) {
      String className = Platform.getProduct().getDefiningBundle().getSymbolicName();
      className = className.replaceAll("\\.ui\\..*$", ".server.core") + ".ServerSession";
      int i = className.lastIndexOf('.');
      try {
        LOG.warn("missing config.ini property: " + getClass().getName() + "#serverSessionClass=your.app.server.ServerSession. Trying to find default class " + className);
        m_serverSessionClass = (Class<? extends IServerSession>) Platform.getBundle(className.substring(0, i)).loadClass(className);
      }
      catch (ClassNotFoundException e) {
        // nop
      }
      if (m_serverSessionClass == null) {
        return new ServiceTunnelResponse(null, null, new ProcessingException("missing config.ini property: " + getClass().getName() + "#serverSessionClass=your.app.server.ServerSession"));
      }
    }

    Thread currentThread = Thread.currentThread();
    if (currentThread == m_dispatcherThread) {
      // when already in dispatcher thread, call service directly
      ServiceTunnelResponse res;
      try {
        res = invokeService(request);
      }
      catch (Throwable e) {
        return new ServiceTunnelResponse(null, null, e);
      }
      return res;

    }
    // create a job and run inside the server dispatcher thread
    final Object waitLock = new Object();
    final Holder<ServiceTunnelResponse> responseHolder = new Holder<ServiceTunnelResponse>(ServiceTunnelResponse.class);
    if ((!currentThread.isInterrupted()) && (monitor == null || !monitor.isCanceled())) {
      Runnable job = new Runnable() {
        @Override
        public void run() {
          try {
            ServiceTunnelResponse res = invokeInServerJob(request, subject);
            responseHolder.setValue(res);
          }
          finally {
            synchronized (waitLock) {
              waitLock.notifyAll();
            }
          }
        }
      };
      enqueueJob(job);
      // wait until done
      synchronized (waitLock) {
        while (responseHolder.getValue() == null && (!currentThread.isInterrupted()) && (!(monitor != null && monitor.isCanceled()))) {
          try {
            waitLock.wait(2000);
          }
          catch (InterruptedException e) {
            responseHolder.setValue(new ServiceTunnelResponse(null, null, e));
          }
        }
      }
    }

    if (responseHolder.getValue() != null) {
      return responseHolder.getValue();
    }
    else {
      return new ServiceTunnelResponse(null, null, new InterruptedException("Result from handler was null"));
    }
  }

  private ServiceTunnelResponse invokeInServerJob(final IServiceTunnelRequest request, final Subject subject) {
    try {
      IServerSession session = getOrCreateSession(subject, request.getUserAgent());

      return ServerJobManager.DEFAULT.runNow(new ICallable<ServiceTunnelResponse>() {

        @Override
        public ServiceTunnelResponse call() throws Exception {
          return invokeService(request);
        }
      }, ServerJobInput.defaults().name("Offline invocation").session(session).subject(subject));
    }
    catch (ProcessingException e) {
      return new ServiceTunnelResponse(null, null, e);
    }
  }

  private ServiceTunnelResponse invokeService(IServiceTunnelRequest serviceReq) throws ProcessingException {
    IServerSession serverSession = (IServerSession) ISession.CURRENT.get();
    Class<?> serviceInterfaceClass = resolveClass(serverSession.getBundle(), serviceReq.getServiceInterfaceClassName());
    Object service = Assertions.assertNotNull(SERVICES.getService(serviceInterfaceClass), "service not found in service registry: %s", serviceReq.getServiceInterfaceClassName());
    Method serviceOp = ServiceUtility.getServiceOperation(serviceInterfaceClass, serviceReq.getOperation(), serviceReq.getParameterTypes());
    Object data = ServiceUtility.invoke(serviceOp, service, serviceReq.getArgs());
    Object[] outParameters = ServiceUtility.extractHolderArguments(serviceReq.getArgs());
    ServiceTunnelResponse serviceRes = new ServiceTunnelResponse(data, outParameters, null);
    // add accumulated client notifications as side-payload
    serviceRes.setClientNotifications(SERVICES.getService(IClientNotificationService.class).getNextNotifications(0));
    return serviceRes;
  }

  private void enqueueJob(Runnable r) {
    synchronized (m_queueLock) {
      m_queue.add(r);
      m_queueLock.notifyAll();
    }
  }

  private void dispatchNextJob() throws InterruptedException {
    synchronized (m_queueLock) {
      if (m_queue.isEmpty()) {
        m_queueLock.wait();
      }
      if (!m_queue.isEmpty()) {
        Runnable r = m_queue.remove(0);
        r.run();
      }
    }
  }

  protected IServerSession getOrCreateSession(Subject subject, String userAgent) throws ProcessingException {
    if (m_serverSession == null || CompareUtility.notEquals(subject, m_subject)) {
      m_subject = subject;
      m_serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(m_serverSessionClass, subject, UserAgent.createByIdentifier(userAgent));
    }
    return m_serverSession;
  }

  protected Class<?> resolveClass(Bundle bundle, String name) {
    try {
      return bundle.loadClass(name);
    }
    catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Failed to load class: " + name, e);
    }
  }
}
