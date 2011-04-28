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
package org.eclipse.scout.commons.logger;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.core.runtime.ILog;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.internal.Activator;
import org.eclipse.scout.commons.logger.internal.eclipse.EclipseScoutLogManager;
import org.eclipse.scout.commons.logger.internal.java.JavaScoutLogManager;

/**
 * The default factory to create {@link IScoutLogger} objects.
 * <p>
 * This factory creates transparent wrappers of JUL logger {@link Logger} and Eclipse logger {@link ILog}, see
 * {@link IScoutLogger} for more details.</br> Set the system (or config.ini) property
 * <code>org.eclipse.scout.log</code> to define a logging strategy. Valid values are:
 * <table border="1">
 * <tr>
 * <td><strong>System (or config.ini) property</strong></td>
 * <td><strong>Strategy</strong></td>
 * <td><strong>Description</strong></td>
 * </tr>
 * <tr>
 * <td>java</td>
 * <td>JUL (Java Util Logger)</td>
 * <td>All log events are passed to the java.util.log logger. This is the default strategy used.</td>
 * </tr>
 * <tr>
 * <td>eclipse</td>
 * <td>Eclipse Logger</td>
 * <td>All log events are passed to the Eclipse logger<br/>
 * The system (or config.ini) property <code>org.eclipse.scout.log.level</code> is only valid for eclipse log and
 * defines which log level is passed to Eclipse.<br>
 * The default is WARNING, since lower levels may produce substantial amount of log when using default eclipse log
 * settings. Valid values are ERROR, WARNING, INFO, DEBUG</td>
 * </tr>
 * <tr>
 * <td><i>EMPTY</td>
 * <td>Custom logger</i></td>
 * <td>By not setting the strategy property, the system looks for a class named
 * <code>org.eclipse.scout.commons.logger.CustomLogManager</code> within its classpath. If found, this class is
 * instantiated and used as logging strategy.</b>Typically, this is done by creating a fragment with this Plug-In
 * configured as host Plug-In. In addition, the fragment must contain the class
 * <code>org.eclipse.scout.commons.logger.CustomLogManager</code> (instance of {@link IScoutLogManager}) to be
 * considered as logging strategy.</td>
 * </tr>
 * </table>
 * <p>
 * It is possible to set a global log level to affect all loggers. Also, log messages can be recorded into a separate
 * file.
 * </p>
 * <p>
 * Example settings in config.ini for eclipse log:
 * </p>
 * <p>
 * 
 * <pre>
 * eclipse.consoleLog=true
 * org.eclipse.scout.log=eclipse
 * org.eclipse.scout.log.level=WARNING
 * </pre>
 * 
 * </p>
 * <p>
 * Example settings in config.ini for java log:
 * 
 * <pre>
 * eclipse.consoleLog=false
 * org.eclipse.scout.log=java
 * </pre>
 * 
 * </p>
 */
public final class ScoutLogManager {

  public static final IScoutLogManager instance;

  static {
    instance = createScoutLogManager();
    instance.initialize();
  }

  private ScoutLogManager() {
  }

  private static IScoutLogManager createScoutLogManager() {
    String strategy = getProperty("org.eclipse.scout.log");
    if (strategy != null) {
      if ("eclipse".equalsIgnoreCase(strategy)) {
        return new EclipseScoutLogManager();
      }
      else if ("java".equalsIgnoreCase(strategy)) {
        // default behavior is to use Java log strategy
        return new JavaScoutLogManager();
      }
    }

    // no logging strategy set. Try to find class 'org.eclipse.scout.commons.logger.CustomLogManager' for custom logging
    if (Activator.getDefault() != null) {
      try {
        Class clazz = Activator.getDefault().getBundle().loadClass("org.eclipse.scout.commons.logger.CustomLogManager");
        if (clazz != null && IScoutLogManager.class.isAssignableFrom(clazz)) {
          return (IScoutLogManager) clazz.newInstance();
        }
      }
      catch (ClassNotFoundException e) {
        // nop (custom logger is not installed)
      }
      catch (Exception e) {
        // error cannot be logged by log manager as not installed yet
        e.printStackTrace();
      }
    }

    return new JavaScoutLogManager();
  }

  public static String getProperty(String property) {
    if (Activator.getDefault() != null) {
      return Activator.getDefault().getBundle().getBundleContext().getProperty(property);
    }
    else {
    }
    return System.getProperty(property, null);
  }

  /**
   * To overwrite the level of all loggers with the given global level. If null is provided, no global log level is used
   * but the initial configuration instead.
   * 
   * @param globalLogLevel
   *          the global log level to set or null to read the initial log configuration
   */
  public static void setGlobalLogLevel(Integer level) {
    instance.setGlobalLogLevel(level);
  }

  /**
   * If a global log level is installed by {@link ScoutLogManager#setGlobalLogLevel(Integer)}, this global level is
   * returned.
   * 
   * @return the global log level or null, if no global log level is set
   */
  public static Integer getGlobalLogLevel() {
    return instance.getGlobalLogLevel();
  }

  /**
   * To start recording log messages. If a recording is already in progress by a previous call to this method, this
   * call has no effect.
   * 
   * @return true if the recording is started or false, if the recording is already in progress.
   * @throws ProcessingException
   *           is thrown if the recording could not be started
   */
  public static boolean startRecording() throws ProcessingException {
    return instance.startRecording();
  }

  /**
   * To stop recording log messages. If no recording is in progress, this call has no effect and null is returned.
   * 
   * @return the log file containing the recorded log messages or null, if no recording was in progress or an error
   *         occured while retrieving the log entries.
   */
  public static File stopRecording() {
    return instance.stopRecording();
  }

  /**
   * To get a new instance of the log wrapper
   * 
   * @param clazz
   * @return
   */
  public static IScoutLogger getLogger(Class clazz) {
    return instance.getLogger(clazz);
  }

  /**
   * To get a new instance of the log wrapper
   * 
   * @param name
   * @return
   */
  public static IScoutLogger getLogger(String name) {
    return instance.getLogger(name);
  }
}
