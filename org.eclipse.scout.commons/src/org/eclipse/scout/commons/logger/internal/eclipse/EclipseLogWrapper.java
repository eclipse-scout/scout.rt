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
package org.eclipse.scout.commons.logger.internal.eclipse;

import java.util.logging.LogRecord;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.EclipseLogUtility;
import org.eclipse.scout.commons.logger.JavaLogUtility;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.logger.internal.AbstractScoutLogger;
import org.osgi.framework.Bundle;

/**
 * This implementation is a wrapper for {@link ILog}
 */
public class EclipseLogWrapper extends AbstractScoutLogger {

  private String m_name;
  private int m_level;

  public EclipseLogWrapper(String name, int level) {
    m_name = name;
    m_level = level;
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public int getLevel() {
    Integer globalLogLevel = ScoutLogManager.getGlobalLogLevel();
    if (globalLogLevel != null) {
      return globalLogLevel;
    }
    return m_level;
  }

  @Override
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
    StringBuilder buf = new StringBuilder();
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
    buf.append(' ');
    buf.append(record.getMessage());

    if (bundle != null) {
      Throwable thrown = record.getThrown();
      if (thrown != null) {
        String message = thrown.getMessage();
        if (message != null) {
          buf.append('\n');
          buf.append(message);
        }
      }

      int scoutLevel = JavaLogUtility.javaToScoutLevel(record.getLevel());
      int severity = EclipseLogUtility.scoutToEclipseLevel(scoutLevel);
      Status status = new Status(severity, bundle.getSymbolicName(), 0, buf.toString(), thrown);
      Platform.getLog(bundle).log(status);
    }
    else {
      System.err.println("Failed logging entry " + record.getLoggerName() + " " + buf.toString());
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
