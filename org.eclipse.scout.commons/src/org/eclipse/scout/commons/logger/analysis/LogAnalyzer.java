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

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.scout.commons.CompositeObject;

/**
 * Analyze a log file with records of the following format LOG_MESSAGE_LINE
 * optional STACK TRACE and info
 */
public class LogAnalyzer {
  private int m_totalCount;
  private int m_filterCount;
  private ArrayList<LogCategory> m_categories;

  public LogAnalyzer() {
  }

  public void start(File in, File out, ILogFilter filter) throws Exception {
    File tmp = new File(out.getAbsolutePath() + ".tmp");
    tmp.deleteOnExit();
    start(new FileReader(in), new FileWriter(tmp), filter);
    if (out.exists() && !out.delete()) {
      throw new IOException("Cannot delete old version of " + out);
    }
    tmp.renameTo(out);
  }

  public void start(ILogFilter filter) throws Exception {
    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
    DataFlavor f = DataFlavor.getTextPlainUnicodeFlavor();
    if (!t.isDataFlavorSupported(f)) {
      throw new IOException("Expected COPY/PASTE text");
    }
    StringWriter out = new StringWriter();
    start(f.getReaderForText(t), out, filter);
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(out.getBuffer().toString()), null);
    System.out.println("PASTED text to clipboard");
  }

  public void start(Reader in, Writer out, ILogFilter filter) throws Exception {
    analyze(in, out, filter);
  }

  private void analyze(Reader in, Writer out, ILogFilter filter) throws Exception {
    m_totalCount = 0;
    m_filterCount = 0;
    m_categories = new ArrayList<LogCategory>();
    BufferedReader r = new BufferedReader(in);
    try {
      String line;
      ArrayList<String> buf = null;
      while ((line = r.readLine()) != null) {
        if (filter.isIgnoredLine(line)) {
          System.out.println("ignoring line: " + line);
        }
        else {
          if (filter.isLogEntryStartLine(line)) {
            analyzeBuffer(buf, out, filter);
            buf = new ArrayList<String>();
          }
          if (buf != null) {
            buf.add(line);
          }
        }
      }
      analyzeBuffer(buf, out, filter);
      // write output
      TreeMap<CompositeObject, LogCategory> sortMap = new TreeMap<CompositeObject, LogCategory>();
      int index = 0;
      for (LogCategory c : m_categories) {
        sortMap.put(new CompositeObject(-c.getEntryCount(), index), c);
        index++;
      }
      for (LogCategory c : sortMap.values()) {
        out.write(c.format(filter));
        out.write("\n");
      }
      System.out.println("TOTAL " + m_totalCount + " entries\nACCEPTED " + m_filterCount + " in " + m_categories.size() + " CATEGORIES");
    }
    finally {
      try {
        r.close();
      }
      catch (Throwable t) {
      }
      try {
        out.close();
      }
      catch (Throwable t) {
      }
    }
  }

  private void analyzeBuffer(List<String> buf, Writer out, ILogFilter filter) throws Exception {
    if (buf != null && buf.size() > 0) {
      m_totalCount++;
      LogEntry e = filter.parse(buf);
      e = filter.filter(e);
      if (e != null) {
        m_filterCount++;
        LogCategory c = null;
        for (LogCategory cat : m_categories) {
          if (cat.isCategoryFor(e)) {
            c = cat;
            break;
          }
        }
        if (c == null) {
          // not categorized
          c = new LogCategory(e);
          m_categories.add(c);
        }
        c.add(e);
      }
    }
  }

  public static void main(String[] args) throws Exception {
    ILogFilter filter = new AbstractLogFilter() {
      @Override
      public LogEntry filter(LogEntry e) {
        return e;
      }
    };
    new LogAnalyzer().start(filter);
  }
}
