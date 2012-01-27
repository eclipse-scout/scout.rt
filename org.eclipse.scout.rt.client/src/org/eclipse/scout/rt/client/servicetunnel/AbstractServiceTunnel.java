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
package org.eclipse.scout.rt.client.servicetunnel;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Arrays;

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBox;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.VersionMismatchException;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;

/**
 * Service tunnel is Thread-Safe
 */
public abstract class AbstractServiceTunnel implements IServiceTunnel {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractServiceTunnel.class);

  private final String m_version;
  private URL m_serverURL;
  private final IClientSession m_clientSession;
  private long m_pollInterval = -1L;
  private boolean m_analyzeNetworkLatency = true;
  //
  private boolean m_notifiedVersionMismatchToUser;

  /**
   * If the version parameter is null, the product bundle (e.g.
   * com.bsiag.crm.ui.swing) version is used
   */
  public AbstractServiceTunnel(IClientSession session, String version) {
    m_clientSession = session;
    if (version == null) {
      if (Platform.getProduct() != null) {
        version = (String) Platform.getProduct().getDefiningBundle().getHeaders().get("Bundle-Version");
      }
    }
    m_version = version;
  }

  @Override
  public void setClientNotificationPollInterval(long intervallMillis) {
    m_pollInterval = intervallMillis;
  }

  @Override
  public long getClientNotificationPollInterval() {
    return m_pollInterval;
  }

  @Override
  public boolean isAnalyzeNetworkLatency() {
    return m_analyzeNetworkLatency;
  }

  @Override
  public void setAnalyzeNetworkLatency(boolean b) {
    m_analyzeNetworkLatency = b;
  }

  public String getVersion() {
    return m_version;
  }

  @Override
  public URL getServerURL() {
    return m_serverURL;
  }

  @Override
  public void setServerURL(URL url) {
    m_serverURL = url;
  }

  public IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) throws ProcessingException {
    if (getServerURL() == null) {
      throw new ProcessingException("serverURL is null. Check proxyHandler extension. Example value is: http://localhost:8080/myapp/process");
    }
    long t0 = System.nanoTime();
    try {
      if (callerArgs == null) {
        callerArgs = new Object[0];
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("" + serviceInterfaceClass + "." + operation + "(" + Arrays.asList(callerArgs) + ")");
      }
      Object[] serializableArgs = ServiceUtility.filterHolderArguments(callerArgs);
      ServiceTunnelRequest call = new ServiceTunnelRequest(getVersion(), serviceInterfaceClass, operation, serializableArgs);
      call.setClientSubject(getClientSession().getSubject());
      call.setVirtualSessionId(getClientSession().getVirtualSessionId());
      //
      ServiceTunnelResponse response = tunnel(call);
      // check if response is interrupted (incomplete /null=interrupted)
      if (response == null) {
        response = new ServiceTunnelResponse(null, null, new InterruptedException());
      }
      // performance analyzer
      IPerformanceAnalyzerService perf = SERVICES.getService(IPerformanceAnalyzerService.class);
      if (perf != null) {
        long totalMillis = (System.nanoTime() - t0) / 1000000L;
        Long execMillis = response.getProcessingDuration();
        if (execMillis != null) {
          perf.addNetworkLatencySample(totalMillis - execMillis);
          perf.addServerExecutionTimeSample(execMillis);
        }
        else {
          perf.addNetworkLatencySample(totalMillis);
        }
      }
      // client notification handler
      IClientNotificationConsumerService cns = SERVICES.getService(IClientNotificationConsumerService.class);
      if (cns != null) {
        cns.dispatchClientNotifications(response.getClientNotifications(), m_clientSession);
      }
      // error handler
      Throwable t = response.getException();
      if (t != null) {
        String msg = "Calling " + serviceInterfaceClass.getSimpleName() + "." + operation.getName() + "()";
        ProcessingException pe;
        if (t instanceof VersionMismatchException) {
          VersionMismatchException ve = (VersionMismatchException) t;
          handleVersionMismatch(ve);
          pe = ve;
        }
        else if (t instanceof ProcessingException) {
          ((ProcessingException) t).addContextMessage(msg);
          pe = (ProcessingException) t;
        }
        else {
          pe = new ProcessingException(msg, t);
        }
        // combine local and remote stacktraces
        StackTraceElement[] trace1 = pe.getStackTrace();
        StackTraceElement[] trace2 = new Exception().getStackTrace();
        StackTraceElement[] both = new StackTraceElement[trace1.length + trace2.length];
        System.arraycopy(trace1, 0, both, 0, trace1.length);
        System.arraycopy(trace2, 0, both, trace1.length, trace2.length);
        pe.setStackTrace(both);
        throw pe;
      }
      ServiceUtility.updateHolderArguments(callerArgs, response.getOutVars(), false);
      return response.getData();
    }
    catch (Throwable t) {
      if (t instanceof ProcessingException) {
        throw (ProcessingException) t;
      }
      else {
        throw new ProcessingException(serviceInterfaceClass.getSimpleName() + "." + operation.getName() + "(" + VerboseUtility.dumpObjects(callerArgs) + ")", t);
      }
    }
  }

  protected ServiceTunnelResponse tunnel(ServiceTunnelRequest call) {
    boolean offline = OfflineState.isOfflineInCurrentThread();
    //
    if (offline) {
      return tunnelOffline(call);
    }
    else {
      return tunnelOnline(call);
    }
  }

  protected abstract ServiceTunnelResponse tunnelOnline(ServiceTunnelRequest call);

  protected void handleVersionMismatch(final VersionMismatchException ve) {
    if (m_notifiedVersionMismatchToUser) {
      return;
    }
    if (ClientSyncJob.getCurrentSession() != m_clientSession) {
      new ClientSyncJob("Version mismatch", m_clientSession) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          handleVersionMismatch(ve);
        }
      }.schedule();
      return;
    }
    // session thread sync
    m_notifiedVersionMismatchToUser = true;
    if (m_clientSession.getDesktop() != null && m_clientSession.getDesktop().isOpened()) {
      ve.consume();
      int response = MessageBox.showYesNoCancelMessage(ScoutTexts.get("VersionMismatchTitle"), ScoutTexts.get("VersionMismatchTextXY", ve.getOldVersion(), ve.getNewVersion()), ScoutTexts.get("VersionMismatchAction"));
      switch (response) {
        case MessageBox.YES_OPTION: {
          m_clientSession.stopSession(IApplication.EXIT_RELAUNCH);
          break;
        }
        case MessageBox.NO_OPTION: {
          break;
        }
        case MessageBox.CANCEL_OPTION: {
          break;
        }
      }
    }
  }

  /**
   * Default for offline handling
   */
  protected ServiceTunnelResponse tunnelOffline(final ServiceTunnelRequest call) {
    final IProgressMonitor monitor;
    Job job = Job.getJobManager().currentJob();
    if (job instanceof ClientJob) {
      monitor = ((ClientJob) job).getMonitor();
    }
    else {
      monitor = new NullProgressMonitor();
    }

    IClientSession clientSession = ClientSyncJob.getCurrentSession();
    if (clientSession != null && clientSession.getOfflineSubject() != null) {
      Object response = Subject.doAs(clientSession.getOfflineSubject(), new PrivilegedAction<ServiceTunnelResponse>() {
        @Override
        public ServiceTunnelResponse run() {
          return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call, monitor);
        }
      });
      return (ServiceTunnelResponse) response;
    }
    else {
      return SERVICES.getService(IOfflineDispatcherService.class).dispatch(call, monitor);
    }
  }

}
