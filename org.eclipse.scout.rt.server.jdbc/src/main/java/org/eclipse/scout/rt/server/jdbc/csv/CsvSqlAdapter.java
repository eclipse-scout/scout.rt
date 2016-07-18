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
package org.eclipse.scout.rt.server.jdbc.csv;

/**
 * Title: BSI Scout V3
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.util.BomInputStreamReader;
import org.eclipse.scout.rt.server.csv.CsvSettings;
import org.eclipse.scout.rt.server.jdbc.ISelectStreamHandler;
import org.eclipse.scout.rt.server.jdbc.ISqlService;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.shared.csv.CsvHelper;
import org.eclipse.scout.rt.shared.csv.IDataConsumer;

public class CsvSqlAdapter {

  private ISqlService m_sqlService;

  public CsvSqlAdapter(ISqlService service) {
    if (service == null) {
      throw new IllegalArgumentException("sql service must not be null");
    }
    m_sqlService = service;
  }

  /**
   * Export a table's data into a file
   *
   * @param f
   *          file to write data to
   * @param contentLocale
   *          see {@link CsvHelper}
   * @param colSeparator
   *          see {@link CsvHelper}
   * @param textDelimiter
   *          see {@link CsvHelper}
   * @param tableName
   *          database table to export from
   * @param groupKeyColumnName
   *          the column where the id for this import is stored (or null if not used)
   * @param groupKeyValue
   *          the id for this csv import
   * @param lineNumberColumnName
   *          where the line number starting with 1 is stored
   * @param csvColumnNames
   *          the names that should appear in the csv file
   * @param writeColumnNames
   *          the sql names that are exported
   * @param csvColumnTypes
   *          the csv types of the corresponding column
   * @param writeColumnTypes
   *          the sql types of the corresponding column
   */
  public void exportDataFromTable(File f, String encoding, Locale contentLocale, String colSeparator, String textDelimiter, String tableName, String groupKeyColumnName, Object groupKeyValue, String lineNumberColumnName,
      List<String> csvColumnNames, boolean writeColumnNames, List<String> csvColumnTypes, boolean writeColumnTypes) {
    if (encoding == null) {
      encoding = StandardCharsets.UTF_8.name();
    }
    CsvSettings params = new CsvSettings();
    params.setFile(f);
    params.setEncoding(encoding);
    params.setContentLocale(contentLocale);
    params.setColSeparator((colSeparator == null) ? 0 : colSeparator.charAt(0));
    params.setTextDelimiter((textDelimiter == null) ? 0 : textDelimiter.charAt(0));
    params.setTableName(tableName);
    params.setGroupKeyColumnName(groupKeyColumnName);
    params.setGroupKeyValue(groupKeyValue);
    params.setLineNumberColumnName(lineNumberColumnName);
    params.setWriteColumnNames(writeColumnNames);
    params.setWriteColumnTypes(writeColumnTypes);
    if (csvColumnNames != null) {
      params.setCsvColumnNames(csvColumnNames);
    }
    if (csvColumnTypes != null) {
      params.setCsvColumnTypes(csvColumnTypes);
    }
    exportData(params);
  }

  /**
   * Export sql select data into a file
   *
   * @param f
   *          file to write data to
   * @param contentLocale
   *          see {@link CsvHelper}
   * @param colSeparator
   *          see {@link CsvHelper}
   * @param textDelimiter
   *          see {@link CsvHelper}
   * @param sqlSelect
   *          the source sql statement
   * @param bindBase
   *          the necessary jdbc binds
   * @param csvColumnNames
   *          the names that should appear in the csv file
   * @param writeColumnNames
   *          the sql names that are exported
   * @param csvColumnTypes
   *          the csv types of the corresponding column
   * @param writeColumnTypes
   *          the sql types of the corresponding column
   */
  public void exportDataWithSql(File f, String encoding, Locale contentLocale, String colSeparator, String textDelimiter, String sqlSelect, NVPair[] bindBase, List<String> csvColumnNames, boolean writeColumnNames,
      List<String> csvColumnTypes, boolean writeColumnTypes) {
    if (encoding == null) {
      encoding = StandardCharsets.UTF_8.name();
    }
    try {
      CsvSettings params = new CsvSettings();
      params.setFile(f);
      params.setEncoding(encoding);
      params.setContentLocale(contentLocale);
      params.setColSeparator((colSeparator == null) ? 0 : colSeparator.charAt(0));
      params.setTextDelimiter((textDelimiter == null) ? 0 : textDelimiter.charAt(0));
      params.setSqlSelect(sqlSelect);
      params.setBindBase(bindBase);
      params.setWriteColumnNames(writeColumnNames);
      params.setWriteColumnTypes(writeColumnTypes);
      if (csvColumnNames != null) {
        params.setCsvColumnNames(csvColumnNames);
      }
      if (csvColumnTypes != null) {
        params.setCsvColumnTypes(csvColumnTypes);
      }
      exportData(params);
    }
    catch (Exception e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  /**
   * Export sql data into a file
   *
   * @param params
   */
  public void exportData(CsvSettings params) {
    final CsvHelper h = new CsvHelper(params.getContentLocale(), params.getColSeparator(), params.getTextDelimiter(), "\n");
    if (params.getCsvColumnTypes() != null) {
      h.setColumnTypes(params.getCsvColumnTypes());
    }
    if (params.getCsvColumnNames() != null) {
      h.setColumnNames(params.getCsvColumnNames());
    }
    try {
      ArrayList<String> cols = new ArrayList<String>();
      cols.addAll(params.getCsvColumnNames());
      // prepare select statement
      String sqlText;
      Object[] base = null;
      if (params.getSqlSelect() != null) {
        sqlText = params.getSqlSelect();
        base = params.getBindBase();
      }
      else {
        StringBuilder buf = new StringBuilder();
        buf.append("SELECT ");
        for (Iterator<String> it = cols.iterator(); it.hasNext();) {
          String colName = it.next();
          buf.append(colName);
          if (it.hasNext()) {
            buf.append(",");
          }
        }
        buf.append(" FROM ");
        buf.append(params.getTableName());
        if (params.getGroupKeyValue() != null) {
          buf.append(" WHERE ");
          buf.append(params.getGroupKeyColumnName());
          buf.append("=:groupKeyColumnValue");
        }
        if (params.getLineNumberColumnName() != null) {
          buf.append(" ORDER BY ");
          buf.append(params.getLineNumberColumnName());
        }
        sqlText = buf.toString();
        if (params.getGroupKeyValue() != null) {
          base = new Object[1];
          base[0] = new NVPair("groupKeyColumnValue", params.getGroupKeyValue());
        }
      }
      final Writer w = new OutputStreamWriter(new FileOutputStream(params.getFile()), params.getEncoding());
      try {
        h.exportHeaderRows(w, params.getWriteColumnNames(), params.getWriteColumnTypes());
        ISelectStreamHandler handler = new ISelectStreamHandler() {
          @Override
          public void handleRow(Connection con, PreparedStatement stm, ResultSet rs, int rowIndex, List<SqlBind> values) {
            Object[] row = new Object[values.size()];
            for (int i = 0; i < row.length; i++) {
              row[i] = values.get(i).getValue();
            }
            h.exportDataRow(row, w, false);
          }

          @Override
          public void finished(Connection con, PreparedStatement stm, ResultSet rs, int rowCount) {
          }
        };
        m_sqlService.selectStreaming(sqlText, handler, base);
      }
      finally {
        try {
          w.close();
        }
        catch (Exception e) {
        }
      }
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  public void importDataIntoTable(File f, String encoding, Locale contentLocale, int headerRowCount, String colSeparator, String textDelimiter, String tableName, String groupKeyColumnName, Object groupKeyValue, String lineNumberColumnName,
      List<String> csvColumnNames, List<String> csvColumnTypes, boolean allowVariableColumnCount) {
    if (encoding == null) {
      encoding = StandardCharsets.UTF_8.name();
    }
    CsvSettings params = new CsvSettings();
    params.setFile(f);
    params.setEncoding(encoding);
    params.setContentLocale(contentLocale);
    params.setHeaderRowCount(headerRowCount);
    params.setColSeparator((colSeparator == null) ? 0 : colSeparator.charAt(0));
    params.setTextDelimiter((textDelimiter == null) ? 0 : textDelimiter.charAt(0));
    params.setTableName(tableName);
    params.setGroupKeyColumnName(groupKeyColumnName);
    params.setGroupKeyValue(groupKeyValue);
    params.setLineNumberColumnName(lineNumberColumnName);
    params.setAllowVariableColumnCount(allowVariableColumnCount);
    if (csvColumnNames != null) {
      params.setCsvColumnNames(csvColumnNames);
    }
    if (csvColumnTypes != null) {
      params.setCsvColumnTypes(csvColumnTypes);
    }
    importData(params);
  }

  public void importData(CsvSettings params) {
    CsvHelper h = new CsvHelper(params.getContentLocale(), params.getColSeparator(), params.getTextDelimiter(), "\n");
    if (params.getCsvColumnTypes() != null) {
      h.setColumnTypes(params.getCsvColumnTypes());
    }
    if (params.getCsvColumnNames() != null) {
      h.setColumnNames(params.getCsvColumnNames());
    }
    try {
      ArrayList<String> cols = new ArrayList<String>();
      if (params.getGroupKeyValue() != null) {
        cols.add(params.getGroupKeyColumnName());
      }
      if (params.getLineNumberColumnName() != null) {
        cols.add(params.getLineNumberColumnName());
      }
      cols.addAll(params.getCsvColumnNames()); //
      StringBuilder buf = new StringBuilder();
      buf.append("INSERT INTO ");
      buf.append(params.getTableName());
      buf.append("(");
      for (Iterator<String> it = cols.iterator(); it.hasNext();) {
        String colName = it.next();
        if (!CsvHelper.IGNORED_COLUMN_NAME.equals(colName)) {
          buf.append(colName);
          buf.append(",");
        }
      }
      buf.deleteCharAt(buf.length() - 1);
      buf.append(") VALUES (");
      int i = 0;
      for (Iterator<String> it = cols.iterator(); it.hasNext();) {
        String colName = it.next();
        if (!CsvHelper.IGNORED_COLUMN_NAME.equals(colName)) {
          buf.append(":v" + i);
          buf.append(",");
          i++;
        }
      }
      buf.deleteCharAt(buf.length() - 1);
      buf.append(")");
      String stm = buf.toString();

      final Reader reader = new BomInputStreamReader(new FileInputStream(params.getFile()), params.getEncoding());
      try {
        SqlInsertDataConsumer cons = new SqlInsertDataConsumer(stm, params.getGroupKeyValue(), params.getLineNumberColumnName() != null);
        h.importData(cons, reader, false, false, params.getHeaderRowCount(), -1, params.getAllowVariableColumnCount());
      }
      finally {
        reader.close();
      }
    }
    catch (IOException e) {
      throw new ProcessingException(e.getMessage(), e);
    }
  }

  private class SqlInsertDataConsumer implements IDataConsumer {
    private String m_statement;
    private Object m_groupKey;
    private boolean m_useLineNumberColumnName;

    public SqlInsertDataConsumer(String stm, Object groupKey, boolean useLineNumberColumnName) {
      m_statement = stm;
      m_groupKey = groupKey;
      m_useLineNumberColumnName = useLineNumberColumnName;
    }

    @Override
    public void processRow(int lineNr, List<Object> row) {
      try {
        ArrayList<Object> bindBase = new ArrayList<Object>();
        int i = 0;
        if (m_groupKey != null) {
          bindBase.add(new NVPair("v" + i, m_groupKey));
          i++;
        }
        if (m_useLineNumberColumnName) {
          bindBase.add(new NVPair("v" + i, lineNr));
          i++;
        }
        for (Iterator<Object> it = row.iterator(); it.hasNext();) {
          bindBase.add(new NVPair("v" + i, it.next()));
          i++;
        }
        m_sqlService.insert(m_statement, bindBase.toArray());
      }
      catch (RuntimeException e) {
        throw new ProcessingException("line=" + lineNr + " row=" + row + "\n" + e.getMessage());
      }
    }
  }
}
