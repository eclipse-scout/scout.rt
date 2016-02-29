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
package org.eclipse.scout.rt.server.csv;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class CsvSettings implements Serializable {
  private static final long serialVersionUID = 1L;
  private File m_file;
  private String m_encoding;
  private Locale m_contentLocale;
  private int m_headerRowCount;
  private char m_colSeparator;
  private char m_textDelimiter;
  private String m_tableName;
  private String m_groupKeyColumnName;
  private Object m_groupKeyValue;
  private String m_lineNumberColumnName;
  private List<String> m_csvColumnNames;
  private List<String> m_csvColumnTypes;
  private String m_sqlSelect;
  private Object[] m_bindBase;
  private boolean m_writeColumnNames;
  private boolean m_writeColumnTypes;
  private boolean m_allowVariableColumnCount;

  public File getFile() {
    return m_file;
  }

  public void setFile(File file) {
    m_file = file;
  }

  public String getEncoding() {
    return m_encoding;
  }

  public void setEncoding(String encoding) {
    m_encoding = encoding;
  }

  public char getColSeparator() {
    return m_colSeparator;
  }

  public void setColSeparator(char colSeparator) {
    m_colSeparator = colSeparator;
  }

  public Locale getContentLocale() {
    return m_contentLocale;
  }

  public void setContentLocale(Locale contentLocale) {
    m_contentLocale = contentLocale;
  }

  public List<String> getCsvColumnNames() {
    return m_csvColumnNames;
  }

  public void setCsvColumnNames(List<String> csvColumnNames) {
    m_csvColumnNames = csvColumnNames;
  }

  public List<String> getCsvColumnTypes() {
    return m_csvColumnTypes;
  }

  public void setCsvColumnTypes(List<String> csvColumnTypes) {
    m_csvColumnTypes = csvColumnTypes;
  }

  public String getGroupKeyColumnName() {
    return m_groupKeyColumnName;
  }

  public void setGroupKeyColumnName(String groupKeyColumnName) {
    m_groupKeyColumnName = groupKeyColumnName;
  }

  public Object getGroupKeyValue() {
    return m_groupKeyValue;
  }

  public void setGroupKeyValue(Object groupKeyValue) {
    m_groupKeyValue = groupKeyValue;
  }

  public int getHeaderRowCount() {
    return m_headerRowCount;
  }

  public void setHeaderRowCount(int headerRowCount) {
    m_headerRowCount = headerRowCount;
  }

  public String getLineNumberColumnName() {
    return m_lineNumberColumnName;
  }

  public void setLineNumberColumnName(String lineNumberColumnName) {
    m_lineNumberColumnName = lineNumberColumnName;
  }

  public String getTableName() {
    return m_tableName;
  }

  public void setTableName(String tableName) {
    m_tableName = tableName;
  }

  public char getTextDelimiter() {
    return m_textDelimiter;
  }

  public void setTextDelimiter(char textDelimiter) {
    m_textDelimiter = textDelimiter;
  }

  public void setWriteColumnNames(boolean val) {
    m_writeColumnNames = val;
  }

  public boolean getWriteColumnNames() {
    return m_writeColumnNames;
  }

  public void setWriteColumnTypes(boolean val) {
    m_writeColumnTypes = val;
  }

  public boolean getWriteColumnTypes() {
    return m_writeColumnTypes;
  }

  public void setAllowVariableColumnCount(boolean val) {
    m_allowVariableColumnCount = val;
  }

  public boolean getAllowVariableColumnCount() {
    return m_allowVariableColumnCount;
  }

  /**
   * @since 2.6
   */
  public void setSqlSelect(String s) {
    m_sqlSelect = s;
  }

  /**
   * @since 2.6
   */
  public String getSqlSelect() {
    return m_sqlSelect;
  }

  public void setBindBase(Object[] base) {
    m_bindBase = base;
  }

  public Object[] getBindBase() {
    return m_bindBase;
  }
}
