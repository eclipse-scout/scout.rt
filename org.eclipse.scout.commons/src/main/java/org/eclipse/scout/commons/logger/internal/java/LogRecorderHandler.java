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
import java.io.IOException;
import java.util.logging.FileHandler;

import org.eclipse.scout.commons.FileUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class LogRecorderHandler extends FileHandler {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogRecorderHandler.class);

  private String m_logDirectory;

  public LogRecorderHandler(String logDirectory, int limit, int count, boolean append) throws SecurityException, IOException {
    super(logDirectory + "/log_%u%g.log", limit, count, append);
    m_logDirectory = logDirectory;
  }

  public File getLogFile() {
    try {
      File zipFile = IOUtility.createTempFile("logging.zip", null);
      FileUtility.compressArchive(IOUtility.toFile(m_logDirectory), zipFile);
      return zipFile;
    }
    catch (Exception e) {
      LOG.error("could not pack log files into ZIP archive", e);
    }
    return null;
  }

  public String getLogDirectory() {
    return m_logDirectory;
  }
}
