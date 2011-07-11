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
package org.eclipse.scout.rt.client.services.common.exceptionhandler.internal;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.exceptionhandler.ErrorHandler;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.AbstractService;

@Priority(-1)
public class InternalClientExceptionHandlerService extends AbstractService implements IExceptionHandlerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(InternalClientExceptionHandlerService.class);
  private static final String SESSION_DATA_KEY = "clientExceptionHandlerServiceState";

  public InternalClientExceptionHandlerService() {
  }

  private ServiceState getServiceState() {
    IClientSession session = ClientJob.getCurrentSession();
    if (session == null) {
      throw new IllegalStateException("null client session in current job context");
    }
    ServiceState data = (ServiceState) session.getData(SESSION_DATA_KEY);
    if (data == null) {
      data = new ServiceState();
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

  @Override
  public void handleException(ProcessingException pe) {
    ServiceState state = getServiceState();
    try {
      if (state.m_handlerLock.acquire()) {
        if (!pe.isConsumed()) {
          if (Platform.inDevelopmentMode() || !(pe instanceof VetoException)) {
            IProcessingStatus s = pe.getStatus();
            boolean logThrowable = !(pe instanceof VetoException);
            int logLevel = IScoutLogger.LEVEL_ERROR;
            String logText = pe.getClass().getSimpleName() + ": " + s.toString();
            switch (s.getSeverity()) {
              case IProcessingStatus.INFO: {
                logLevel = IScoutLogger.LEVEL_INFO;
                break;
              }
              case IProcessingStatus.WARNING: {
                logLevel = IScoutLogger.LEVEL_WARN;
                break;
              }
              case IProcessingStatus.ERROR: {
                logLevel = IScoutLogger.LEVEL_ERROR;
                break;
              }
              case IProcessingStatus.FATAL: {
                logLevel = IScoutLogger.LEVEL_ERROR;
                break;
              }
            }
            differentiatedLog(InternalClientExceptionHandlerService.class.getName(), logLevel, logText, logThrowable ? pe : null);
          }
          // check if the desktop is observing this process
          IDesktop desktop = ClientSyncJob.getCurrentSession().getDesktop();
          if (desktop != null && desktop.isOpened()) {
            showExceptionInUI(pe);
          }
        }
      }
      else {
        LOG.error("loop detection in " + getClass().getSimpleName() + " when handling: " + pe + ". StackTrace is following", new Exception());
      }
    }
    finally {
      state.m_handlerLock.release();
      pe.consume();
    }
  }

  void differentiatedLog(String fqcn, int level, String m, Throwable t) {
    switch (level) {
      case IScoutLogger.LEVEL_TRACE:
        LOG.trace(m, t);
        break;
      case IScoutLogger.LEVEL_DEBUG:
        LOG.debug(m, t);
        break;
      case IScoutLogger.LEVEL_INFO:
        LOG.info(m, t);
        break;
      case IScoutLogger.LEVEL_WARN:
        LOG.warn(m, t);
        break;
      case IScoutLogger.LEVEL_ERROR:
        LOG.error(m, t);
        break;
    }
  }

  /**
   * To visualize the exception in the UI
   * 
   * @param pe
   *          the exception to be visualized
   */
  protected void showExceptionInUI(ProcessingException pe) {
    new ErrorHandler(pe).showMessageBox();
  }

  private static class ServiceState {
    final OptimisticLock m_handlerLock = new OptimisticLock();
  }

}
