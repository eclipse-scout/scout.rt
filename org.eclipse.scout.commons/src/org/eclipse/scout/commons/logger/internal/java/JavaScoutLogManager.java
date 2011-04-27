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
package org.eclipse.scout.commons.logger.internal.java;

import java.io.File;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.EclipseLogUtility;
import org.eclipse.scout.commons.logger.IScoutLogManager;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.JavaLogUtility;

/**
 * The default Scout log manager when using java.util.logging (JUL).
 * <p>
 * When being initialized, the manager installs a listener to the {@link Platform} in order to also delegate Eclipse log
 * event to JUL.
 * </p>
 * <p>
 * A global log level is set by setting the levels of all child loggers to null to inherit the configuration of the root
 * logger. In turn, the level of the root logger is set to the global log level. Furthermore, the level of all attached
 * log handlers is set to the given global log level.
 * </p>
 * <p>
 * Recording of log messages is implemented the way of installing a log handler to JUL. Thereby, all log messages are
 * written into a temporary ZIP file that is returned when recording is stopped.
 * </p>
 */
public class JavaScoutLogManager implements IScoutLogManager {

  private Object m_recordingLock;
  private LogRecorderHandler m_logRecorderHandler;
  private ILogListener m_eclipseLogListener;
  private boolean m_globalLogLevelSet;

  public JavaScoutLogManager() {
    m_recordingLock = new Object();
    m_eclipseLogListener = new P_EclipseToJavaDelegateListener();
  }

  /**
   * Redirect eclipse log to java log
   * <p>
   * Make two enhancements
   * <ol>
   * <li>set default log level for root to WARNING if there is no custom config. INFO is insane as a default-default</li>
   * <li>install "better" simple log formatter when SimpleFormatter is used</li>
   * </ol>
   */
  @Override
  public void initialize() {
    if (Platform.isRunning()) {
      Platform.addLogListener(m_eclipseLogListener);
    }

    Logger root = Logger.getLogger("");
    if (root.getLevel() == Level.INFO) {
      if (System.getProperty("java.util.logging.config.class") == null && System.getProperty("java.util.logging.config.file") == null) {
        root.setLevel(Level.WARNING);
      }
    }

    for (Handler h : root.getHandlers()) {
      if (h.getFormatter() instanceof SimpleFormatter) {
        h.setFormatter(new JavaLogFormatter());
      }
    }
  }

  @Override
  public IScoutLogger getLogger(String name) {
    return new JavaLogWrapper(name);
  }

  @Override
  public IScoutLogger getLogger(Class clazz) {
    return getLogger(clazz.getName());
  }

  @Override
  public void setGlobalLogLevel(Integer globalLogLevel) {
    if (globalLogLevel == null) {
      // reset logger configuration
      m_globalLogLevelSet = false;
      try {
        LogManager.getLogManager().getLogger("").setLevel(null);
        LogManager.getLogManager().readConfiguration();
      }
      catch (Exception e) {
        getLogger(JavaScoutLogManager.class).error("could not restore logging configuration", e);
      }
    }
    else {
      //set global log level to all loggers and handlers
      m_globalLogLevelSet = true;

      Level javaLogLevel = JavaLogUtility.scoutToJavaLevel(globalLogLevel);

      // first, reset the level of loggers as only level of root logger is set afterwards
      Enumeration<String> loggerNames = LogManager.getLogManager().getLoggerNames();
      while (loggerNames.hasMoreElements()) {
        Logger logger = LogManager.getLogManager().getLogger(loggerNames.nextElement());
        if (logger.getLevel() != null && logger.getLevel() != javaLogLevel) {
          logger.setLevel(null);
        }

        // update level of attached handlers to the given level
        for (Handler handler : logger.getHandlers()) {
          handler.setLevel(javaLogLevel);
        }
      }

      // instruct root logger to log all messages of the given level
      LogManager.getLogManager().getLogger("").setLevel(javaLogLevel);
    }
  }

  @Override
  public Integer getGlobalLogLevel() {
    if (m_globalLogLevelSet) {
      Level level = LogManager.getLogManager().getLogger("").getLevel();
      return JavaLogUtility.javaToScoutLevel(level);
    }
    return null;
  }

  @Override
  public boolean startRecording() throws ProcessingException {
    synchronized (m_recordingLock) {
      if (m_logRecorderHandler == null) {
        m_logRecorderHandler = createLogRecordingHandler();

        Logger root = Logger.getLogger("");
        root.addHandler(m_logRecorderHandler);
        return true;
      }
      return false;
    }
  }

  @Override
  public File stopRecording() {
    synchronized (m_recordingLock) {
      if (m_logRecorderHandler == null) {
        return null;
      }
      File logFile = m_logRecorderHandler.getLogFile();
      m_logRecorderHandler = null;

      // remove installed handler
      Logger root = Logger.getLogger("");
      root.removeHandler(m_logRecorderHandler);

      return logFile;
    }
  }

  /**
   * To create a handler to record the log messages
   * 
   * @return
   * @throws ProcessingException
   */
  protected LogRecorderHandler createLogRecordingHandler() throws ProcessingException {
    try {
      File tempLogDir = IOUtility.createTempDirectory("log");
      LogRecorderHandler handler = new LogRecorderHandler(tempLogDir.getAbsolutePath(), 1024 * 1024, 0, false);
      handler.setLevel(Level.FINEST);
      handler.setFormatter(new JavaLogFormatter());
      handler.setEncoding("UTF-8");
      return handler;
    }
    catch (Exception e) {
      throw new ProcessingException("could not create handler to record log messages", e);
    }
  }

  /**
   * To listen for Eclipse log events and send them to JUL
   */
  private final class P_EclipseToJavaDelegateListener implements ILogListener {

    public void logging(IStatus status, String plugin) {
      int scoutLogLevel = EclipseLogUtility.eclipseToScoutLevel(status.getSeverity());
      Level javaLogLevel = JavaLogUtility.scoutToJavaLevel(scoutLogLevel);

      String codeText = status.getCode() != 0 ? "[code " + status.getCode() + "] " : "";
      LogRecord record = new LogRecord(javaLogLevel, codeText + status.getMessage());
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
}
