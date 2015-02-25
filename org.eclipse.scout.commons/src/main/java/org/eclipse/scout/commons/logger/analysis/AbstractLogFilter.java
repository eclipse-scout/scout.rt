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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example of simple log record [FATAL] 2008-02-01 08:21:06.934 TP-Processor23
 * org.eclipse.scout.rt.server.servlet.HttpProxyHandlerServlet.service(
 * HttpProxyHandlerServlet.java:102) Example of eclipse log record !ENTRY
 * org.eclipse
 * .scout.rt.shared.services.common.exceptionhandler.LogExceptionHandlerService
 * .handleException(LogExceptionHandlerService.java:33) 4 0 2008-09-23
 * 08:11:43.776 !MESSAGE ProcessingStatus[ERROR code=0
 * Client=BSIAG\Administrator@127.0.0.1/127.0.0.1 / Identity=BSIAG\Administrator
 * / cannot find a userNr for 'administrator' java.lang.SecurityException:
 * access denied] !STACK 0 and !SESSION 2008-09-30 12:20:35.435
 * ----------------------------------------------- ...
 */
public abstract class AbstractLogFilter implements ILogFilter {
  private static final Pattern SIMPLE_LOG_PARSE_PATTERN = Pattern.compile("^\\[([^]]+)\\]\\s+(....-..-.....:..:..\\....)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.*)$");
  private static final Pattern ECLIPSE_LOG_PARSE_PATTERN1 = Pattern.compile("^\\!ENTRY\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(....-..-.....:..:..\\....)$");
  private static final Pattern ECLIPSE_LOG_PARSE_PATTERN2 = Pattern.compile("^\\!SESSION\\s+(....-..-.....:..:..\\....).*$");
  private static final SimpleDateFormat ECLIPSE_LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private static final SimpleDateFormat SIMPLE_LOG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  private final String m_formatPattern;
  private final SimpleDateFormat m_dateFormat;

  public AbstractLogFilter() {
    this("[{1}] {2} {3} {4} {5}", "yyyy-MM-dd HH:mm:ss.SSS");
  }

  /**
   * @param parsePattern
   *          defines the first log line of a log entry and consists of some
   *          groups
   * @param formatPattern
   *          defines a pattern with the variables {1},{2},{3},{4},{5} that
   *          represent (severity) (date) (thread) (source) (message)
   */
  public AbstractLogFilter(String formatPattern, String dateFormat) {
    m_formatPattern = formatPattern;
    m_dateFormat = new SimpleDateFormat(dateFormat);
  }

  @Override
  public boolean isIgnoredLine(String line) {
    if (line.startsWith("PasswordSecurityFilter::")) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isLogEntryStartLine(String line) {
    if (SIMPLE_LOG_PARSE_PATTERN.matcher(line).matches()) {
      return true;
    }
    if (ECLIPSE_LOG_PARSE_PATTERN1.matcher(line).matches()) {
      return true;
    }
    if (ECLIPSE_LOG_PARSE_PATTERN2.matcher(line).matches()) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("null")
  @Override
  public LogEntry parse(List<String> entry) throws Exception {
    LogEntry e = null;
    // try simple log
    if (e == null) {
      Matcher m = SIMPLE_LOG_PARSE_PATTERN.matcher(entry.get(0));
      if (m.matches()) {
        e = new LogEntry();
        e.severity = m.group(1);
        e.date = SIMPLE_LOG_DATE_FORMAT.parse(m.group(2));
        e.thread = m.group(3);
        e.source = m.group(4);
        e.message = m.group(5);
        if (entry.size() > 1) {
          StringBuffer b = new StringBuffer();
          for (int i = 1; i < entry.size(); i++) {
            if (b.length() > 0) {
              b.append("\n");
            }
            b.append(entry.get(i));
          }
          e.attachment = b.toString();
        }
        else {
          e.attachment = "";
        }
      }
    }
    // try eclipse log
    if (e == null) {
      Matcher m = ECLIPSE_LOG_PARSE_PATTERN1.matcher(entry.get(0));
      if (m.matches()) {
        e = new LogEntry();
        e.severity = "LOG";
        e.thread = "";
        e.source = m.group(1);
        e.date = ECLIPSE_LOG_DATE_FORMAT.parse(m.group(4));
        e.message = "";
        StringBuffer attachmentBuf = new StringBuffer();
        for (String line : entry) {
          if (line.startsWith("!ENTRY")) {
            // ignore
          }
          else if (line.startsWith("!MESSAGE")) {
            if (e.message.length() == 0) {
              e.message = line.substring(9);
            }
          }
          else if (line.startsWith("!STACK")) {
            // ignore
          }
          else if (line.startsWith("!SESSION")) {
            // ignore
          }
          else {
            if (attachmentBuf.length() > 0) {
              attachmentBuf.append("\n");
            }
            attachmentBuf.append(line);
          }
        }
        e.attachment = attachmentBuf.toString().trim();
      }
    }
    if (e == null) {
      Matcher m = ECLIPSE_LOG_PARSE_PATTERN2.matcher(entry.get(0));
      if (m.matches()) {
        e = new LogEntry();
        e.severity = "LOG";
        e.thread = "";
        e.source = "";
        e.date = ECLIPSE_LOG_DATE_FORMAT.parse(m.group(1));
        e.message = "New Session";
        StringBuffer attachmentBuf = new StringBuffer();
        for (String line : entry) {
          if (attachmentBuf.length() > 0) {
            attachmentBuf.append("\n");
          }
          attachmentBuf.append(line);
        }
        e.attachment = attachmentBuf.toString().trim();
      }
    }
    /*
     * workaround for logger that doesn't handle logger stop class
     */
    if (e.source.indexOf("LogExceptionHandlerService.handleException") >= 0) {
      Matcher m = Pattern.compile("\\sat\\s([^\\s(:]+\\([^\\s:]+\\:[0-9]+\\))").matcher(e.attachment);
      if (m.find()) {
        e.source = m.group(1);
      }
    }
    return e;
  }

  @Override
  public LogEntry filter(LogEntry e) {
    return e;
  }

  @Override
  public String formatContext(LogEntry e) {
    return m_dateFormat.format(e.date) + " " + e.thread + " " + e.message;
  }

  @Override
  public String format(LogEntry e) {
    StringBuffer b = new StringBuffer();
    String line = m_formatPattern;
    line = line.replace("{1}", e.severity);
    line = line.replace("{2}", m_dateFormat.format(e.date));
    line = line.replace("{3}", e.thread);
    line = line.replace("{4}", e.source);
    line = line.replace("{5}", e.message);
    b.append(line);
    b.append("\n");
    if (e.attachment.length() > 0) {
      b.append(e.attachment);
      b.append("\n");
    }
    return b.toString();
  }

}
