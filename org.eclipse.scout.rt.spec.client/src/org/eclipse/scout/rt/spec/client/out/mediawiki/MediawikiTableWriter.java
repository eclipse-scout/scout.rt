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
package org.eclipse.scout.rt.spec.client.out.mediawiki;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.spec.client.out.IDocTable;

/**
 * A Writer for {@link IDocTable} in mediawiki format.
 */
public class MediawikiTableWriter {
  private final Writer m_writer;

  public static final String NEWLINE = System.getProperty("line.separator");

  private static final String TABLE_START = "{|";
  private static final String TABLE_END = "|}";
  private static final String TABLE_NEW_ROW = "|-" + NEWLINE;
  private static final String TABLE_COLUMN_SEPARATOR = "| ";
  private static final String EMPTY_CELL_TEXT = "&nbsp;";
  private static final char HEADING_CHAR = '=';
  private static final String DEFAULT_TABLE_BORDER = "border=\"1\" ";

  private final String m_table_options;

  /**
   * @param {@link Writer}
   */
  public MediawikiTableWriter(Writer writer) {
    this(writer, DEFAULT_TABLE_BORDER);
  }

  public MediawikiTableWriter(Writer writer, String tableOptions) {
    m_writer = writer;
    m_table_options = tableOptions;
  }

  /**
   * Append a table in wiki format to the {@link Writer}.
   * 
   * @param table
   *          {@link IDocTable} containing column headers and cells.
   * @throws IOException
   */
  public void appendTable(IDocTable table) throws IOException {
    m_writer.append(TABLE_START);
    m_writer.append(StringUtility.nvl(m_table_options, ""));
    m_writer.append(NEWLINE);
    appendTableHeaders(table.getHeaderTexts());
    appendDataRows(table.getCellTexts());
    m_writer.append(TABLE_END);
    m_writer.append(NEWLINE);
    m_writer.flush();
  }

  /**
   * Append a transposed table in wiki format to the {@link Writer}.
   * 
   * @param table
   *          {@link IDocTable} containing row headers and cells.
   * @throws IOException
   */
  public void appendTableTransposed(IDocTable table) throws IOException {
    m_writer.append(TABLE_START);
    m_writer.append(StringUtility.nvl(m_table_options, ""));
    m_writer.append(NEWLINE);
    String[] headerTexts = table.getHeaderTexts();
    String[][] cellTexts = table.getCellTexts();
    for (int i = 0; i < headerTexts.length; i++) {
      int rowLength = cellTexts.length + 1;
      m_writer.append(TABLE_NEW_ROW);
      for (int j = 0; j < rowLength; j++) {
        String text;
        if (j == 0) {
          text = getHeaderCellText(headerTexts[i]);
        }
        else {
          text = getCellText(cellTexts[j - 1][i]);
        }
        m_writer.append(text);
        m_writer.append(NEWLINE);
      }
    }
    m_writer.append(TABLE_END);
    m_writer.append(NEWLINE);
    m_writer.flush();
  }

  private void appendDataRows(String[][] cellTexts) throws IOException {
    for (String[] row : cellTexts) {
      m_writer.append(TABLE_NEW_ROW);
      for (String cell : row) {
        m_writer.append(TABLE_COLUMN_SEPARATOR);
        String cellText = getEscapedText(cell);
        m_writer.append(cellText);
        m_writer.append(System.getProperty("line.separator"));
      }
    }
  }

  private String getHeaderCellText(String text) {
    return "!" + getEscapedText(text);
  }

  private String getCellText(String text) {
    return TABLE_COLUMN_SEPARATOR + getEscapedText(text);
  }

  private String getEscapedText(String text) {
    String nonEmptyText = StringUtility.isNullOrEmpty(text) ? EMPTY_CELL_TEXT : text;
    return nonEmptyText.replace("[", "<nowiki>[</nowiki>").replace("]", "<nowiki>]</nowiki>");
  }

  private void appendTableHeaders(String[] headerTexts) throws IOException {
    for (String header : headerTexts) {
      String text = getHeaderCellText(header);
      m_writer.append(text);
      m_writer.append(NEWLINE);
    }
  }

  public void appendHeading(String name, int level) throws IOException {
    String prefix = repeat(HEADING_CHAR, level);
    m_writer.append(prefix);
    m_writer.append(" ");
    m_writer.append(name);
    m_writer.append(" ");
    m_writer.append(prefix);
    m_writer.append(NEWLINE);
  }

  private String repeat(char c, int n) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < n; i++) {
      b.append(c);
    }
    return b.toString();
  }

}
