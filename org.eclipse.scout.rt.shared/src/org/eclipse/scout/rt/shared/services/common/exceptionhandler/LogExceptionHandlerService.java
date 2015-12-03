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
package org.eclipse.scout.rt.shared.services.common.exceptionhandler;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.IProcessingStatus;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.service.AbstractService;

@Priority(-2)
@RemoteServiceAccessDenied
public class LogExceptionHandlerService extends AbstractService implements IExceptionHandlerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogExceptionHandlerService.class);

  public LogExceptionHandlerService() {
  }

  @Override
  public void handleException(ProcessingException pe) {
    if (pe.isInterruption()) {
      return;
    }
    IProcessingStatus s = pe.getStatus();
    int logLevel = IScoutLogger.LEVEL_ERROR;
    if (pe instanceof VetoException) {
      logLevel = IScoutLogger.LEVEL_INFO;
    }
    else {
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
    }
    differentiatedLog(LogExceptionHandlerService.class.getName(), logLevel, s.toString(), pe);
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
}
