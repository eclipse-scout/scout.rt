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

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.scout.commons.logger.JavaLogUtility;
import org.eclipse.scout.commons.logger.internal.AbstractScoutLogger;

/**
 * This implementation is a wrapper for {@link Logger}
 */
public class JavaLogWrapper extends AbstractScoutLogger {

  private Logger m_logger;

  public JavaLogWrapper(String name) {
    m_logger = Logger.getLogger(name);
  }

  public String getName() {
    return m_logger.getName();
  }

  public int getLevel() {
    Logger loggerWithLevel = m_logger;
    while (loggerWithLevel.getLevel() == null && loggerWithLevel.getParent() != null) {
      loggerWithLevel = loggerWithLevel.getParent();
    }
    return JavaLogUtility.javaToScoutLevel(loggerWithLevel.getLevel());
  }

  public void setLevel(int level) {
    m_logger.setLevel(JavaLogUtility.scoutToJavaLevel(level));
  }

  @Override
  protected void logImpl(LogRecord record) {
    m_logger.log(record);
  }

  @Override
  public boolean isLoggable(int logLevel) {
    return m_logger.isLoggable(JavaLogUtility.scoutToJavaLevel(logLevel));
  }

}
