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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.EclipseLogUtility;
import org.eclipse.scout.commons.logger.IScoutLogManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.JavaLogUtility;
import org.eclipse.scout.commons.logger.internal.java.JavaScoutLogManager;

/**
 * The default Scout log manager when using Eclipse logging.
 * <p>
 * When being initialized, the manager installs a listener to the {@link Platform} in order to also delegate Eclipse log
 * event to JUL.
 * </p>
 * <p>
 * If a global log level is set, it is simply hold by this log manager. It is business of the @{link
 * {@link EclipseLogWrapper}'s to take into account this level when handling log events.
 * </p>
 * <p>
 * Recording of log messages is implemented the way of installing a {@link ILogListener} to the {@link Platform} in
 * order to intercept log message events. In turn, the log messages are written into a temporary file that is returned
 * when recording is stopped.
 * </p>
 */
public class EclipseScoutLogManager implements IScoutLogManager {

  private Integer m_globalLogLevel;
  private P_LogListener m_logListener;
  private Object m_recordingLock;

  public EclipseScoutLogManager() {
    m_recordingLock = new Object();
  }

  @Override
  public void initialize() {
  }

  @Override
  public void setGlobalLogLevel(Integer globalLogLevel) {
    m_globalLogLevel = globalLogLevel;
  }

  @Override
  public Integer getGlobalLogLevel() {
    return m_globalLogLevel;
  }

  @Override
  public IScoutLogger getLogger(String name) {
    return new EclipseLogWrapper(name, getSystemLogLevel());
  }

  @Override
  public IScoutLogger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  @Override
  public boolean startRecording() throws ProcessingException {
    synchronized (m_recordingLock) {
      if (m_logListener == null) {
        m_logListener = new P_LogListener();
        Platform.addLogListener(m_logListener);
        return true;
      }
    }
    return false;
  }

  @Override
  public File stopRecording() {
    synchronized (m_recordingLock) {
      if (m_logListener == null) {
        return null;
      }
      Platform.removeLogListener(m_logListener);
      File logFile = m_logListener.getLogFile();
      m_logListener = null;
      return logFile;
    }
  }

  private int getSystemLogLevel() {
    String levelText = StringUtility.uppercase(Activator.getDefault().getBundle().getBundleContext().getProperty("org.eclipse.scout.log.level"));
    if (!StringUtility.hasText(levelText)) {
      levelText = "WARNING";
    }

    if ("ERROR".equals(levelText)) {
      return IScoutLogger.LEVEL_ERROR;
    }
    else if ("WARNING".equals(levelText)) {
      return IScoutLogger.LEVEL_WARN;
    }
    else if ("INFO".equals(levelText)) {
      return IScoutLogger.LEVEL_INFO;
    }
    else if ("DEBUG".equals(levelText)) {
      return IScoutLogger.LEVEL_DEBUG;
    }
    else {
      return IScoutLogger.LEVEL_WARN;
    }
  }

  private final class P_LogListener implements ILogListener {

    private File m_logFile;
    private BufferedWriter m_logWriter;

    public P_LogListener() throws ProcessingException {
      try {
        m_logFile = IOUtility.createTempFile("log.log", null);
        m_logWriter = new BufferedWriter(new FileWriter(m_logFile));
      }
      catch (Exception e) {
        throw new ProcessingException("could not install log listener", e);
      }
    }

    @Override
    public void logging(IStatus status, String plugin) {
      int severity = EclipseLogUtility.eclipseToScoutLevel(status.getSeverity());
      Level level = JavaLogUtility.scoutToJavaLevel(severity);

      String logLevel = "[" + level.getName() + "] ";
      String logPlugin;
      if (StringUtility.hasText(status.getPlugin())) {
        logPlugin = "plug-In=" + status.getPlugin();
      }
      else {
        logPlugin = "plug-In=" + plugin;
      }
      String logCode = null;
      if (status.getCode() != 0) {
        logCode = "code=" + status.getCode();
      }

      String log = StringUtility.join(" ", logLevel, getFormattedDate(), logPlugin, logCode, status.getMessage(), getStackTrace(status.getException()));
      try {
        m_logWriter.write(log);
        m_logWriter.newLine();
      }
      catch (IOException e) {
        System.err.println("Failed recording logging entry");
      }
    }

    public File getLogFile() {
      try {
        m_logWriter.flush();
        m_logWriter.close();
        return m_logFile;
      }
      catch (IOException e) {
        getLogger(JavaScoutLogManager.class).error("could not get log file", e);
      }
      return null;
    }

    private String getFormattedDate() {
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      return new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(calendar.getTime());
    }

    private String getStackTrace(Throwable t) {
      if (t == null) {
        return null;
      }
      StringWriter writer = new StringWriter();
      t.printStackTrace(new PrintWriter(writer));
      return writer.toString();
    }
  }
}
