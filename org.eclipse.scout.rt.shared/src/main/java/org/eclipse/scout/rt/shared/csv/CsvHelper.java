/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.BomInputStreamReader;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.NumberFormatProvider;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper for reading and writing CSV data. <br/>
 * Consider using {@link BomInputStreamReader} when working with unicode encoded input data.
 */
public class CsvHelper {
  /**
   * a column named with "null" will be ignored, its data will not be imported
   */
  public static final String IGNORED_COLUMN_NAME = "null";
  private static final Logger LOG = LoggerFactory.getLogger(CsvHelper.class);

  private final Locale m_locale;
  private final char m_separatorChar;// ";"
  private final char m_textDelimiterChar;// "\""
  private final String m_lineSeparator;// "\n"
  private int m_colCount;
  private List<String> m_colNames;
  private List<String> m_colTypes;
  private List<Format> m_colFormat;
  private boolean[] m_ignoredColumns;
  private boolean m_encodeLineSeparator;

  public CsvHelper() {
    this(null, ',', '"', "\n");
  }

  public CsvHelper(Locale locale, String separatorChar, String textDelimiterChar, String lineSeparator) {
    this(locale, (separatorChar != null && separatorChar.length() > 0 ? separatorChar.charAt(0) : 0x00), (textDelimiterChar != null && textDelimiterChar.length() > 0 ? textDelimiterChar.charAt(0) : 0x00), lineSeparator);
  }

  public CsvHelper(Locale locale, char separatorChar, char textDelimiterChar, String lineSeparator) {
    m_locale = locale == null ? NlsLocale.get() : locale;
    m_separatorChar = separatorChar != 0x00 ? separatorChar : ';';
    m_textDelimiterChar = textDelimiterChar != 0x00 ? textDelimiterChar : '"';
    m_lineSeparator = lineSeparator != null ? lineSeparator : "\n";
    m_colFormat = new ArrayList<>();
  }

  public Locale getLocale() {
    return m_locale;
  }

  public char getSeparatorChar() {
    return m_separatorChar;
  }

  public char getTextDelimiterChar() {
    return m_textDelimiterChar;
  }

  public String getLineSeparator() {
    return m_lineSeparator;
  }

  public boolean isEncodeLineSeparator() {
    return m_encodeLineSeparator;
  }

  /**
   * Configures whether a text containing the line separator should be encoded.
   */
  public void setEncodeLineSeparator(boolean encodeLineSeparator) {
    m_encodeLineSeparator = encodeLineSeparator;
  }

  /**
   * @return column headers
   */
  public List<String> getColumnNames() {
    return CollectionUtility.arrayList(m_colNames);
  }

  public void setColumnNames(List<String> list) {
    m_colNames = new ArrayList<String>(list);
    m_colCount = Math.max(m_colCount, m_colNames.size());
    m_ignoredColumns = new boolean[m_colNames.size()];
    for (int i = 0; i < m_colNames.size(); i++) {
      m_ignoredColumns[i] = IGNORED_COLUMN_NAME.equals(m_colNames.get(i));
    }
  }

  /**
   * @return column data types
   */
  public List<String> getColumnTypes() {
    return CollectionUtility.arrayList(m_colTypes);
  }

  public void setColumnTypes(List<String> list) {
    m_colTypes = new ArrayList<>(list.size());
    m_colFormat = new ArrayList<>(list.size());
    for (String s : list) {
      Format f = getFormat(s);
      if (f == null) {
        s = "string";
      }
      m_colTypes.add(s);
      m_colFormat.add(f);
    }
    m_colCount = Math.max(m_colCount, m_colTypes.size());
  }

  protected int getColCount() {
    return m_colCount;
  }

  /**
   * A format is described by the type optionally followed by _pattern. e.g.
   * <ul>
   * <li>integer_<pattern>, see {@link DecimalFormat}</li>
   * <li>float_<pattern>, see {@link DecimalFormat}</li>
   * <li>date_<pattern>, see {@link DateFormat}</li>
   * </ul>
   */
  protected Format getFormat(String s) {
    if (s == null) {
      return null;
    }
    String sLow = StringUtility.lowercase(s);

    if (sLow.startsWith("date")) {// date_<format>
      if (s.length() >= 5) {
        return new SimpleDateFormat(s.substring(5), getLocale());
      }
      else {
        return BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.SHORT, getLocale());
      }
    }
    else if (sLow.startsWith("integer")) {// integer_<format>
      if (s.length() >= 8) {
        Format f = new DecimalFormat(s.substring(8), new DecimalFormatSymbols(getLocale()));
        ((DecimalFormat) f).setParseIntegerOnly(true);
        return f;
      }
      else {
        return BEANS.get(NumberFormatProvider.class).getIntegerInstance(getLocale());
      }
    }

    else if (sLow.startsWith("float")) {
      if (s.length() >= 6) {// float_<format>
        return new DecimalFormat(s.substring(6), new DecimalFormatSymbols(getLocale()));
      }
      else {
        return BEANS.get(NumberFormatProvider.class).getNumberInstance(getLocale());
      }
    }

    return null;
  }

  /**
   * @return a copy of the ignored columns as a list
   */
  public List<Boolean> getIgnoredColumns() {
    if (m_ignoredColumns == null) {
      return Collections.emptyList();
    }

    List<Boolean> ignoredColumns = new ArrayList<Boolean>(m_ignoredColumns.length);
    for (int i = 0; i < m_ignoredColumns.length; i++) {
      ignoredColumns.add(m_ignoredColumns[i]);
    }
    return ignoredColumns;
  }

  public Object[][] importData(Reader reader, int headerRowCount, List<String> columnTypes, int rowCount) {
    if (columnTypes != null) {
      setColumnTypes(columnTypes);
    }
    ArrayConsumer cons = new ArrayConsumer();
    importData(cons, reader, false, false, headerRowCount, rowCount);
    Object[][] result = cons.getData();
    return result;
  }

  public void importData(IDataConsumer dataConsumer, Reader reader, boolean readNameHeader, boolean readTypeHeader, int headerRowCount) {
    importData(dataConsumer, reader, readNameHeader, readTypeHeader, headerRowCount, -1);
  }

  public void importData(IDataConsumer dataConsumer, Reader reader, boolean readNameHeader, boolean readTypeHeader, int headerRowCount, int rowCount) {
    importData(dataConsumer, reader, readNameHeader, readTypeHeader, headerRowCount, rowCount, false);
  }

  /**
   * @param dataConsumer
   * @param reader
   *          read data from
   * @param readNameHeader
   *          stream contains a column header line
   * @param readTypeHeader
   *          stream contains a column type line
   * @param headerRowCount
   *          total number of header lines
   * @param rowCount
   *          may be negative if unknown
   * @param allowVariableColumnCount
   *          true if not all lines have the same number of columns
   */
  public void importData(IDataConsumer dataConsumer, Reader reader, boolean readNameHeader, boolean readTypeHeader, int headerRowCount, int rowCount, boolean allowVariableColumnCount) {
    String cell = null;
    int colIndex = 0;
    int lineNr = -1;
    if (rowCount < 0) {
      rowCount = Integer.MAX_VALUE;
    }
    try {
      if (readNameHeader) {
        List<String> list = importRow(reader);
        if (list != null) {
          setColumnNames(list);
        }
        headerRowCount--;
      }
      if (readTypeHeader) {
        List<String> list = importRow(reader);
        if (list != null) {
          setColumnTypes(list);
        }
        headerRowCount--;
      }
      while (headerRowCount > 0) {
        importRow(reader);
        headerRowCount--;
      }
      // data
      List<String> cellList;
      lineNr = 1;
      while ((cellList = importRow(reader)) != null && lineNr <= rowCount) {
        // fill up with empty row if allowed
        while (allowVariableColumnCount && (cellList.size() < getColumnNames().size())) {
          cellList.add(cellList.size(), null);
        }
        // convert data types
        ArrayList<Object> objList = new ArrayList<Object>(cellList.size());
        for (colIndex = 0; colIndex < cellList.size(); colIndex++) {
          if (m_ignoredColumns == null || m_ignoredColumns.length == 0 || m_ignoredColumns.length < colIndex || !m_ignoredColumns[colIndex]) {
            cell = cellList.get(colIndex);
            try { // NOSONAR
              objList.add(importCell(cell, getColumnFormat(colIndex)));
            }
            catch (RuntimeException e) {
              throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
                  .withContextInfo("cell", cell)
                  .withContextInfo("colIndex", colIndex);
            }
          }
        }
        // add row to data
        dataConsumer.processRow(lineNr, objList);
        lineNr++;
      }
    }
    catch (IOException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("lineNr", lineNr);
    }
  }

  public void exportData(Object[][] data, File f, String encoding, List<String> columnNames, boolean writeColumnNames, List<String> columnTypes, boolean writeColumnTypes) {
    if (encoding == null) {
      encoding = StandardCharsets.UTF_8.name();
    }
    try (Writer writer = new OutputStreamWriter(new FileOutputStream(f), encoding)) {
      exportData(data, writer, columnNames, writeColumnNames, columnTypes, writeColumnTypes);
    }
    catch (IOException e) {
      throw new ProcessingException(f.getAbsolutePath(), e);
    }
  }

  public void exportData(Object[][] data, Writer writer, List<String> columnNames, boolean writeColumnNames, List<String> columnTypes, boolean writeColumnTypes) {
    try {
      if (columnNames != null) {
        setColumnNames(columnNames);
      }
      if (columnTypes != null) {
        setColumnTypes(columnTypes);
      }
      exportHeaderRows(writer, writeColumnNames, writeColumnTypes);
      for (Object[] row : data) {
        exportDataRow(row, writer, false);
      }
    }
    finally {
      try {
        writer.close();
      }
      catch (Exception e) {
        LOG.warn("Could not close writer", e);
      }
    }
  }

  /**
   * @param writer
   * @param writeNames
   *          write a line for the column names
   * @param writeTypes
   *          write a line for the data types
   * @throws ProcessingException
   *           Writes the header rows to the writer.
   */
  public void exportHeaderRows(Writer writer, boolean writeNames, boolean writeTypes) {
    String line = null;
    int colIndex = 0;
    Object val = null;
    String cell = null;
    try {
      if (writeNames) {
        line = exportRow(m_colNames);
        writer.write(line);
        writer.write(getLineSeparator());
      }
      if (writeTypes) {
        line = exportRow(m_colTypes);
        writer.write(line);
        writer.write(getLineSeparator());
      }
    }
    catch (IOException e) {
      throw new ProcessingException("line=" + line + " colIndex=" + colIndex + " value=" + val + " cell=" + cell, e);
    }
  }

  /**
   * @param writer
   * @throws ProcessingException
   *           Writes data rows to the writer and does close it.
   */
  public void exportDataRow(Object[] row, Writer writer) {
    exportDataRow(row, writer, true);
  }

  /**
   * @param writer
   * @param closeWriter
   *          true->will close the writer
   * @throws ProcessingException
   *           Writes data rows to the writer.
   */
  public void exportDataRow(Object[] row, Writer writer, boolean closeWriter) {
    String line = null;
    Object val = null;
    String cell = null;
    try {
      ArrayList<String> rowStrings = new ArrayList<String>();
      rowStrings.clear();
      for (int i = 0; i < row.length; i++) {
        val = row[i];
        cell = exportCell(val, getColumnFormat(i));
        rowStrings.add(cell);
      }
      line = exportRow(rowStrings);
      writer.write(line);
      writer.write(getLineSeparator());
    }
    catch (IOException e) {
      throw new ProcessingException("line=" + Arrays.asList(row), e);
    }
    finally {
      if (closeWriter) {
        try {
          writer.close();
        }
        catch (Exception e) {
          LOG.warn("Could not close writer", e);
        }
      }
    }
  }

  protected Format getColumnFormat(int colIndex) {
    if (colIndex < m_colFormat.size()) {
      return m_colFormat.get(colIndex);
    }
    else {
      return null;
    }
  }

  protected List<String> importRow(Reader reader) throws IOException {
    List<String> cellList = new ArrayList<String>(Math.max(getColCount(), 2));
    boolean inString = false;
    StringBuilder curBuf = new StringBuilder();
    String token;
    int ch = reader.read();

    while (ch == '\n' || ch == '\r') {
      ch = reader.read();
    }
    if (ch < 0) {
      return null;
    }
    while (true) {
      if (ch >= 0 && inString) {
        if (ch == getTextDelimiterChar()) {
          inString = false;
        }
        else {
          // still in string
        }
        curBuf.append((char) ch);
      }
      else {// ch<0 or out of string or end of line
        if (ch == getSeparatorChar() || ch < 0 || ch == '\n' || ch == '\r') {
          // consume token
          token = curBuf.toString();
          curBuf.setLength(0);
          int tokenLen = token.length();
          if (tokenLen > 0) {
            // remove delimiters
            if (token.charAt(0) == getTextDelimiterChar() && token.charAt(tokenLen - 1) == getTextDelimiterChar()) {
              token = token.substring(1, tokenLen - 1);
            }
            if (token.length() == 0) {
              token = null;
            }
          }
          else {
            token = null;
          }
          // decode and add token
          token = decodeText(token);
          cellList.add(token);
          // check if end of current line
          if (ch < 0 || ch == '\n' || ch == '\r') {
            break;
          }
        }
        else if (ch == getTextDelimiterChar()) {
          inString = true;
          curBuf.append((char) ch);
        }
        else {
          // normal character
          curBuf.append((char) ch);
        }
      }
      // next character
      ch = reader.read();
    }
    // end of file?
    if (cellList.size() == 0 && ch < 0) {
      cellList = null;
    }
    return cellList;
  }

  protected String exportRow(Collection<String> strings) {
    StringBuilder buf = new StringBuilder();
    if (strings != null) {
      for (Iterator<String> it = strings.iterator(); it.hasNext();) {
        buf.append(encodeText(it.next()));
        if (it.hasNext() && getSeparatorChar() != 0x00) {
          buf.append(getSeparatorChar());
        }
      }
    }
    return buf.toString();
  }

  protected Object importCell(String text, Format f) {
    if (text != null && f != null) {
      try {
        return f.parseObject(text.trim());
      }
      catch (ParseException e) {
        throw new ProcessingException("Cannot parse '{}' using format '{}'", text, f, e);
      }
    }
    else {
      return text;
    }
  }

  protected String exportCell(Object o, Format f) {
    if (f != null && o != null) {
      return f.format(o);
    }
    else if (o == null) {
      return "";
    }
    else {
      return o.toString();
    }
  }

  protected String encodeText(String text) {
    if (getTextDelimiterChar() != 0x00 && text != null) {
      text = stringReplace(text, "" + getTextDelimiterChar(), "" + getTextDelimiterChar() + getTextDelimiterChar());
      if (text.indexOf(getSeparatorChar()) >= 0 || text.indexOf(getTextDelimiterChar()) >= 0 || (isEncodeLineSeparator() && text.indexOf(getLineSeparator()) >= 0)) {
        text = getTextDelimiterChar() + text + getTextDelimiterChar();
      }
    }
    return text;
  }

  protected String decodeText(String text) {
    if (text != null && text.length() > 0 && getTextDelimiterChar() != 0x00) {
      if (text.charAt(0) == getTextDelimiterChar() && text.charAt(text.length() - 1) == getTextDelimiterChar()) {
        text = text.substring(1, text.length() - 1);
      }
      text = stringReplace(text, "" + getTextDelimiterChar() + getTextDelimiterChar(), "" + getTextDelimiterChar());
    }
    return text;
  }

  protected String stringReplace(String s, String sOld, String sNew) {
    sNew = (sNew == null ? "" : sNew);
    if (s == null || sOld == null) {
      return s;
    }
    StringBuilder buf = new StringBuilder();
    int oldLen = sOld.length();
    int pos = 0;
    int i = s.indexOf(sOld);
    while (i >= 0) {
      buf.append(s.substring(pos, i));
      buf.append(sNew);
      pos = i + oldLen;
      i = s.indexOf(sOld, pos);
    }
    buf.append(s.substring(pos));
    return buf.toString();
  }

  /**
   * @param file
   * @return the the first line of the file, expecting it to be the column names
   * @throws ProcessingException
   *           Read only
   */
  public List<String> getColumnNames(File f) {
    try (Reader r = new FileReader(f)) {
      List<String> result = getCurrentRow(r);
      return result;
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * @param reader
   * @return the current row in the reader as cell tokens based on this helpers context
   */
  public List<String> getCurrentRow(Reader reader) {
    try (
        BufferedReader bufferedReader = new BufferedReader(reader)) {
      return importRow(bufferedReader);
    }
    catch (Exception e) {
      throw new ProcessingException("reading header row", e);
    }
  }
}
