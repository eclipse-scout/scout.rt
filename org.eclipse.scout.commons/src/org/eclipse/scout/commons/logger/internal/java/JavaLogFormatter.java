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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JavaLogFormatter extends Formatter {
  private final static SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS", Locale.US);

  private Date m_tmpDate = new Date();

  /**
   * Format the given LogRecord.
   * 
   * @param record
   *          the log record to be formatted.
   * @return a formatted log record
   */
  @Override
  public synchronized String format(LogRecord record) {
    StringBuffer buf = new StringBuffer();
    //single instance of date to save memory
    m_tmpDate.setTime(record.getMillis());
    buf.append(TIMESTAMP_FORMAT.format(m_tmpDate));
    buf.append(" ");
    String levelText = record.getLevel().getName();
    if (levelText.equals("SEVERE")) {
      levelText = "ERROR";
    }
    buf.append(levelText);
    buf.append(" ");
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
    buf.append(" ");
    buf.append(formatMessage(record));
    if (record.getThrown() != null) {
      buf.append("\n");
      try {
        StringWriter sw = new StringWriter();
        PrintWriter p = new PrintWriter(sw);
        record.getThrown().printStackTrace(p);
        p.close();
        buf.append(sw.toString());
      }
      catch (Exception e) {
        //nop
      }
    }
    buf.append("\n");
    return buf.toString();
  }
}
