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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogCategory {
  private static Pattern causePattern = Pattern.compile(".*(Caused by: .*)");

  private String severity;
  private String source;
  private String attachmentSignature;
  private ArrayList<LogEntry> m_entries;

  public LogCategory(LogEntry template) {
    m_entries = new ArrayList<LogEntry>();
    severity = template.severity;
    source = template.source;
    attachmentSignature = createAttachmentSignature(template.attachment);
  }

  public int getEntryCount() {
    return m_entries.size();
  }

  // parse all "Caused by: javax.servlet.ServletException: abc def"
  private String createAttachmentSignature(String s) {
    if (s == null) {
      return "- none -";
    }
    s = s.trim();
    if (s.length() == 0) {
      return "- none -";
    }
    //
    StringBuffer buf = new StringBuffer();
    for (String line : s.split("\n")) {
      Matcher m = causePattern.matcher(line);
      if (m.matches()) {
        if (buf.length() > 0) {
          buf.append("\n");
        }
        buf.append(m.group(1));
      }
    }
    return buf.toString();
  }

  public boolean isCategoryFor(LogEntry e) {
    if (severity.equals(e.severity) && source.equals(e.source) && attachmentSignature.equals(createAttachmentSignature(e.attachment))) {
      return true;
    }
    else {
      return false;
    }
  }

  public void add(LogEntry e) {
    m_entries.add(e);
  }

  public String format(ILogFilter filter) {
    StringBuffer b = new StringBuffer();
    HashSet<String> attachmentSet = new HashSet<String>();
    for (LogEntry e : m_entries) {
      if (e.attachment.length() > 0) {
        attachmentSet.add(e.attachment);
      }
    }
    // source
    b.append("[");
    b.append(severity);
    b.append("] ");
    b.append(source);
    b.append("\n");
    b.append(m_entries.size() + " occurrences");
    if (attachmentSet.size() >= 2) {
      b.append(" with " + attachmentSet.size() + " different attachments");
    }
    b.append("\n");
    b.append("*** Signature ***\n");
    b.append(attachmentSignature);
    b.append("\n");
    // occurrences
    b.append("*** Headers ***\n");
    for (LogEntry e : m_entries) {
      b.append(filter.formatContext(e));
      b.append("\n");
    }
    // attachment
    b.append("*** Details ***\n");
    for (String s : attachmentSet) {
      b.append(s);
      b.append("\n");
    }
    return b.toString();
  }
}
