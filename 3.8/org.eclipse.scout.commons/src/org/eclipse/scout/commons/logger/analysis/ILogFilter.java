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
package org.eclipse.scout.commons.logger.analysis;

import java.util.List;

public interface ILogFilter {

  boolean isIgnoredLine(String line);

  boolean isLogEntryStartLine(String line);

  LogEntry parse(List<String> entry) throws Exception;

  LogEntry filter(LogEntry entry) throws Exception;

  /**
   * format context only (date, thread, message)
   */
  String formatContext(LogEntry e);

  /**
   * format complete entry
   */
  String format(LogEntry e);

}
