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

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.internal.slf4j.Slf4jScoutLogManager;

/**
 * The default factory to create {@link IScoutLogger} objects.
 * <p>
 * This factory creates transparent wrappers of JUL logger {@link Logger}.<br>
 * Add a class named <code>org.eclipse.scout.commons.logger.CustomLogManager</code> to the classpath to define a custom
 * logging manager.
 *
 * @see IScoutLogManager
 * @see IScoutLogger
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
    try {
      Class clazz = Class.forName("org.eclipse.scout.commons.logger.CustomLogManager");
      if (clazz != null && IScoutLogManager.class.isAssignableFrom(clazz)) {
        return (IScoutLogManager) clazz.newInstance();
      }
    }
    catch (ClassNotFoundException e) {// NOSONAR
      // nop (custom logger is not installed)
    }
    catch (Exception e) { // NOSONAR
      // error cannot be logged by log manager as not installed yet
      e.printStackTrace(); // NOSONAR
    }

    return new Slf4jScoutLogManager();
  }

  /**
   * To overwrite the level of all loggers with the given global level. If null is provided, no global log level is used
   * but the initial configuration instead.
   *
   * @param globalLogLevel
   *          the global log level to set or null to read the initial log configuration
   * @throws UnsupportedOperationException
   *           is thrown if the log implementation does not support global log level
   */
  public static void setGlobalLogLevel(Integer level) {
    instance.setGlobalLogLevel(level);
  }

  /**
   * If a global log level is installed by {@link ScoutLogManager#setGlobalLogLevel(Integer)}, this global level is
   * returned.
   *
   * @return the global log level or null, if no global log level is set
   * @throws UnsupportedOperationException
   *           is thrown if the log implementation does not support global log level
   */
  public static Integer getGlobalLogLevel() {
    return instance.getGlobalLogLevel();
  }

  /**
   * To start recording log messages. If a recording is already in progress by a previous call to this method, this call
   * has no effect.
   *
   * @return true if the recording is started or false, if the recording is already in progress.
   * @throws ProcessingException
   *           is thrown if the recording could not be started
   * @throws UnsupportedOperationException
   *           is thrown if the log implementation does not support recording of log messages
   */
  public static boolean startRecording() throws ProcessingException {
    return instance.startRecording();
  }

  /**
   * To stop recording log messages. If no recording is in progress, this call has no effect and null is returned.
   *
   * @return the log file containing the recorded log messages or null, if no recording was in progress or an error
   *         occured while retrieving the log entries.
   * @throws UnsupportedOperationException
   *           is thrown if the log implementation does not support recording of log messages
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
