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

import java.util.logging.LogRecord;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.JavaLogUtility;
import org.osgi.framework.Bundle;

/**
 * This implementation is a wrapper for {@link ILog}
 */
public class EclipseLogWrapper extends AbstractScoutLogger {

  private String m_name;
  private int m_level;

  public EclipseLogWrapper(String name) {
    m_name = name;
    String levelText = null;
    levelText = Activator.getDefault().getBundle().getBundleContext().getProperty("org.eclipse.scout.log.level");
    levelText = levelText != null ? levelText.toUpperCase() : "WARNING";
    //
    if ("ERROR".equals(levelText)) {
      m_level = IScoutLogger.LEVEL_ERROR;
    }
    else if ("WARNING".equals(levelText)) {
      m_level = IScoutLogger.LEVEL_WARN;
    }
    else if ("INFO".equals(levelText)) {
      m_level = IScoutLogger.LEVEL_INFO;
    }
    else if ("DEBUG".equals(levelText)) {
      m_level = IScoutLogger.LEVEL_DEBUG;
    }
    else {
      m_level = IScoutLogger.LEVEL_WARN;
    }
  }

  public String getName() {
    return m_name;
  }

  public int getLevel() {
    return m_level;
  }

  public void setLevel(int level) {
    m_level = level;
  }

  @Override
  protected void logImpl(LogRecord record) {
    String path = stripOffLastSegment(record.getLoggerName());
    Bundle bundle = null;
    while (path != null) {
      bundle = Platform.getBundle(path);
      if (bundle != null) {
        break;
      }
      path = stripOffLastSegment(path);
    }
    if (bundle == null) {
      bundle = Activator.getDefault().getBundle();
    }
    //
    StringBuffer buf = new StringBuffer();
    if (record.getSourceClassName() != null) {
      buf.append(record.getSourceClassName());
      if (record.getSourceMethodName() != null) {
        buf.append(".");
        buf.append(record.getSourceMethodName());
      }
    }
    else {
      buf.append(record.getLoggerName());
    }
    if (bundle != null) {
      int severity;
      switch (JavaLogUtility.javaToScoutLevel(record.getLevel())) {
        case IScoutLogger.LEVEL_OFF: {
          severity = Status.ERROR;
        }
        case IScoutLogger.LEVEL_ERROR: {
          severity = Status.ERROR;
        }
        case IScoutLogger.LEVEL_WARN: {
          severity = Status.WARNING;
        }
        case IScoutLogger.LEVEL_INFO: {
          severity = Status.INFO;
        }
        case IScoutLogger.LEVEL_DEBUG: {
          severity = Status.INFO;
        }
        case IScoutLogger.LEVEL_TRACE: {
          severity = Status.INFO;
        }
        default: {
          severity = Status.INFO;
        }
      }
      Status status = new Status(severity, bundle.getSymbolicName(), 0, buf.toString() + " " + record.getMessage(), record.getThrown());
      Platform.getLog(bundle).log(status);
    }
    else {
      System.err.println("Failed logging entry " + record.getLoggerName() + " " + buf.toString() + " " + record.getMessage());
    }
  }

  public String stripOffLastSegment(String s) {
    if (s == null) {
      return null;
    }
    int i = s.lastIndexOf('.');
    if (i >= 0) {
      s = s.substring(0, i).trim();
      return s.length() > 0 ? s : null;
    }
    else {
      return null;
    }
  }
}
