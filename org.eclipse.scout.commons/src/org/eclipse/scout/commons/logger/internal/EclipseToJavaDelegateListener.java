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
package org.eclipse.scout.commons.logger.internal;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

/**
 *
 */
public class EclipseToJavaDelegateListener implements ILogListener {

  public void logging(IStatus status, String plugin) {
    Level level;
    switch (status.getSeverity()) {
      case IStatus.CANCEL:
      case IStatus.ERROR: {
        level = Level.SEVERE;
        break;
      }
      case IStatus.WARNING: {
        level = Level.WARNING;
        break;
      }
      case IStatus.OK:
      case IStatus.INFO: {
        level = Level.INFO;
        break;
      }
      default: {
        level = Level.INFO;
      }
    }
    String codeText = status.getCode() != 0 ? "[code " + status.getCode() + "] " : "";
    LogRecord record = new LogRecord(level, codeText + status.getMessage());
    record.setLoggerName(plugin);
    if (status.getPlugin() != null && status.getPlugin().length() > 0) {
      record.setSourceClassName(status.getPlugin());
    }
    else {
      record.setSourceClassName(plugin);
    }
    record.setSourceMethodName(null);
    record.setThrown(status.getException());
    Logger.getLogger(record.getLoggerName()).log(record);
  }
}
