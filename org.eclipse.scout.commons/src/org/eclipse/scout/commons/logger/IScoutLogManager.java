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

import org.eclipse.scout.commons.exception.ProcessingException;

public interface IScoutLogManager {

  /**
   * To initialize the log manager
   */
  void initialize();

  /**
   * To get a new instance of the log wrapper
   * 
   * @param name
   * @return
   */
  IScoutLogger getLogger(String name);

  /**
   * To get a new instance of the log wrapper
   * 
   * @param clazz
   * @return
   */
  IScoutLogger getLogger(Class clazz);

  /**
   * To overwrite the level of all loggers with the given global level. If null is provided, no global log level is used
   * but the initial configuration instead.
   * 
   * @param globalLogLevel
   *          the global log level to set or null to read the initial log configuration
   */
  void setGlobalLogLevel(Integer globalLogLevel);

  /**
   * If a global log level is installed by {@link IScoutLogManager#setGlobalLogLevel(Integer)}, this global level is
   * returned.
   * 
   * @return the global log level or null, if no global log level is set
   */
  Integer getGlobalLogLevel();

  /**
   * To start recording log messages. If a recording is already in progress by a previous call to this method, this
   * call has no effect.
   * 
   * @return true if the recording is started or false, if the recording is already in progress.
   * @throws ProcessingException
   *           is thrown if the recording could not be started
   */
  boolean startRecording() throws ProcessingException;

  /**
   * To stop recording log messages. If no recording is in progress, this call has no effect and null is returned.
   * 
   * @return the log file containing the recorded log messages or null, if no recording was in progress or an error
   *         occured while retrieving the log entries.
   */
  File stopRecording();
}
