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
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.BeanArrayHolderFilter;
import org.eclipse.scout.rt.platform.holders.IBeanArrayHolder;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.ITableBeanHolder;
import org.eclipse.scout.rt.platform.holders.ITableHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;
import org.eclipse.scout.rt.platform.holders.TableHolderFilter;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlTransactionMember;
import org.eclipse.scout.rt.server.jdbc.ISelectStreamHandler;
import org.eclipse.scout.rt.server.jdbc.ISqlService;
import org.eclipse.scout.rt.server.jdbc.IStatementCache;
import org.eclipse.scout.rt.server.jdbc.IStatementProcessor;
import org.eclipse.scout.rt.server.jdbc.IStatementProcessorMonitor;
import org.eclipse.scout.rt.server.jdbc.SqlBind;
import org.eclipse.scout.rt.server.jdbc.oracle.OracleSqlStyle;
import org.eclipse.scout.rt.server.jdbc.parsers.BindModel;
import org.eclipse.scout.rt.server.jdbc.parsers.BindParser;
import org.eclipse.scout.rt.server.jdbc.parsers.IntoModel;
import org.eclipse.scout.rt.server.jdbc.parsers.IntoParser;
import org.eclipse.scout.rt.server.jdbc.parsers.sql.SqlFormatter;
import org.eclipse.scout.rt.server.jdbc.parsers.token.DatabaseSpecificToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.FunctionInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.IToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueInputToken;
import org.eclipse.scout.rt.server.jdbc.parsers.token.ValueOutputToken;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatementProcessor implements IStatementProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(StatementProcessor.class);

  static {
    if (LOG.isDebugEnabled()) {
      LOG.debug("SQL Log 'DEBUG': statements are logged with bind details and also as plaintext (suitable for direct use in executable form)");
    }
    else if (LOG.isInfoEnabled()) {
      LOG.info("SQL Log 'INFO': statements are logged with bind details, but not as plaintext. Use level 'FINE' to also log SQL as plain text (suitable for direct use in executable form)");
    }
  }

  private final ISqlService m_callerService;
  private final String m_originalStm;
  private Object[] m_bindBases;
  private int m_maxRowCount;
  private int m_maxFetchSize = -1;
  private BindModel m_bindModel;
  private IToken[] m_ioTokens;
  private List<IBindInput> m_inputList;
  private List<IBindOutput> m_outputList;
  // state
  private int m_currentInputBatchIndex = -1;
  private int m_currentOutputBatchIndex = -1;
  private String m_currentInputStm;
  private TreeMap<Integer/* jdbcBindIndex */, SqlBind> m_currentInputBindMap;
  private int m_maxFetchMemorySize;

  public StatementProcessor(ISqlService callerService, String stm, Object[] bindBases) {
    this(callerService, stm, bindBases, 0);
  }

  public StatementProcessor(ISqlService callerService, String stm, Object[] bindBases, int maxRowCount) {
    this(callerService, stm, bindBases, maxRowCount, AbstractSqlService.DEFAULT_MEMORY_PREFETCH_SIZE);
  }

  public StatementProcessor(ISqlService callerService, String stm, Object[] bindBases, int maxRowCount, int maxFetchMemorySize) {
    if (stm == null) {
      throw new ProcessingException("statement is null");
    }
    try {
      m_callerService = callerService;
      m_originalStm = stm;
      m_maxRowCount = maxRowCount;
      m_maxFetchMemorySize = maxFetchMemorySize;
      // expand bind bases to list
      ArrayList<Object> bases = new ArrayList<Object>();
      if (bindBases != null) {
        for (int i = 0, n = bindBases.length; i < n; i++) {
          bases.add(bindBases[i]);
        }
      }
      // add server session as default
      IServerSession session = ServerSessionProvider.currentSession();
      if (session != null) {
        bases.add(session);
      }
      m_bindBases = bases.toArray();
      //
      m_inputList = new ArrayList<IBindInput>();
      m_outputList = new ArrayList<IBindOutput>();
      //
      IntoModel intoModel = new IntoParser(m_originalStm).parse();
      String stmWithoutSelectInto = intoModel.getFilteredStatement();
      //
      m_bindModel = new BindParser(stmWithoutSelectInto).parse();
      m_ioTokens = m_bindModel.getIOTokens();
      //
      int jdbcBindIndex = 1;
      ISqlStyle sqlStyle = m_callerService.getSqlStyle();
      for (IToken t : m_ioTokens) {
        IBindInput in = null;
        IBindOutput out = null;
        if (t.isInput()) {
          in = createInput(t, m_bindBases);
          if (in.isJdbcBind(sqlStyle)) {
            in.setJdbcBindIndex(jdbcBindIndex);
          }
          m_inputList.add(in);
        }
        if (t.isOutput()) {
          out = createOutput(t, m_bindBases);
          if (out.isJdbcBind()) {
            out.setJdbcBindIndex(jdbcBindIndex);
          }
          m_outputList.add(out);
        }
        //
        if ((in != null && in.isJdbcBind(sqlStyle)) || (out != null && out.isJdbcBind())) {
          jdbcBindIndex++;
        }
      }
      for (IToken t : m_bindModel.getAllTokens()) {
        if (t instanceof DatabaseSpecificToken) {
          processDatabaseSpecificToken((DatabaseSpecificToken) t, callerService.getSqlStyle());
        }
      }
      // add select into out binds
      for (IToken t : intoModel.getOutputTokens()) {
        IBindOutput out = createOutput(t, m_bindBases);
        if (!out.isSelectInto()) {
          throw new ProcessingException("out parameter is not a 'select into': " + out);
        }
        if (out.isJdbcBind()) {
          throw new ProcessingException("out parameter is a jdbc bind: " + out);
        }
        out.setJdbcBindIndex(-1);
        m_outputList.add(out);
      }
    }
    catch (RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
  }

  protected TreeMap<Integer, SqlBind> getCurrentInputBindMap() {
    return m_currentInputBindMap;
  }

  protected ISqlService getCallerService() {
    return m_callerService;
  }

  protected Object[] processResultRow(ResultSet rs) throws SQLException {
    ISqlStyle sqlStyle = m_callerService.getSqlStyle();
    ResultSetMetaData meta = rs.getMetaData();
    int colCount = meta.getColumnCount();
    Object[] row = new Object[colCount];
    for (int i = 0; i < colCount; i++) {
      int type = meta.getColumnType(i + 1);
      row[i] = sqlStyle.readBind(rs, meta, type, i + 1);
    }
    return row;
  }

  private int getMaxFetchSize(ResultSet rs) throws SQLException {
    if (m_maxFetchSize == -1) {
      int memoryUsagePerRow = 32; // reference to array
      ResultSetMetaData meta = rs.getMetaData();
      for (int i = 1; i <= meta.getColumnCount(); i++) {
        memoryUsagePerRow += meta.getColumnDisplaySize(i);
      }
      m_maxFetchSize = m_maxFetchMemorySize / memoryUsagePerRow;
    }
    return m_maxFetchSize;
  }

  protected List<Object[]> processResultRows(ResultSet rs, int maxRowCount) throws SQLException {
    boolean isDynamicPrefetch = false;
    int rowCount = 0;
    int initialFetchSize = 0;
    int dynamicFetchSize = 0;
    if (m_callerService.getSqlStyle() != null && m_callerService.getSqlStyle() instanceof OracleSqlStyle) {
      // init prefetch params
      isDynamicPrefetch = true;
      initialFetchSize = rs.getFetchSize();
      dynamicFetchSize = initialFetchSize;
    }
    ArrayList<Object[]> rows = new ArrayList<Object[]>();
    while (rs.next()) {
      if (isDynamicPrefetch && ++rowCount % dynamicFetchSize == 0 && dynamicFetchSize < getMaxFetchSize(rs)) {
        dynamicFetchSize = Math.min(Math.max(initialFetchSize, rowCount / 2), getMaxFetchSize(rs));
        rs.setFetchSize(dynamicFetchSize);
      }
      Object[] row = processResultRow(rs);
      rows.add(row);
      if (maxRowCount > 0 && rows.size() >= maxRowCount) {
        break;
      }
    }
    return rows;
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor#processSelect(java.sql.Connection,
   * org.eclipse.scout.rt.
   * server.services.common.sql.internal.exec.PreparedStatementCache)
   */
  @SuppressWarnings("resource")
  @Override
  public Object[][] processSelect(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      ArrayList<Object[]> rows = new ArrayList<Object[]>();
      while (hasNextInputBatch()) {
        nextInputBatch();
        prepareInputStatementAndBinds();
        dump();
        ps = cache.getPreparedStatement(conn, m_currentInputStm);
        bindBatch(ps);
        registerActiveStatement(ps);
        try {
          rs = ps.executeQuery();
          for (Object[] row : processResultRows(rs, m_maxRowCount)) {
            rows.add(row);
            nextOutputBatch();
            consumeSelectIntoRow(row);
          }
        }
        finally {
          unregisterActiveStatement(ps);
          /*
           * The PreparedStatement and the ResultSet of the last input batch are not allowed to be closed
           * yet because the monitor could do some post-fetching of the data.
           * Closing the last PreparedStatement and its ResultSet is done in the outer finally block.
           */
          if (hasNextInputBatch()) {
            releasePreparedStatementAndResultSet(ps, cache, rs);
          }
        }
      }
      finishOutputBatch();
      if (monitor != null) {
        monitor.postFetchData(conn, ps, rs, rows);
      }
      return rows.toArray(new Object[rows.size()][]);
    }
    catch (SQLException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
    finally {
      releasePreparedStatementAndResultSet(ps, cache, rs);
    }
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor
   * #processSelectInto(java.sql.Connection,org.eclipse.scout.
   * rt.server.services.common.sql.internal.exec.PreparedStatementCache)
   */
  @SuppressWarnings("resource")
  @Override
  public void processSelectInto(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      while (hasNextInputBatch()) {
        nextInputBatch();
        prepareInputStatementAndBinds();
        dump();
        ps = cache.getPreparedStatement(conn, m_currentInputStm);
        bindBatch(ps);
        registerActiveStatement(ps);
        try {
          rs = ps.executeQuery();
          for (Object[] row : processResultRows(rs, m_maxRowCount)) {
            nextOutputBatch();
            consumeSelectIntoRow(row);
          }
        }
        finally {
          unregisterActiveStatement(ps);
          /*
           * The PreparedStatement and the ResultSet of the last input batch are not allowed to be closed
           * yet because the monitor could do some post-fetching of the data.
           * Closing the last PreparedStatement and its ResultSet is done in the outer finally block.
           */
          if (hasNextInputBatch()) {
            releasePreparedStatementAndResultSet(ps, cache, rs);
          }
        }
      }
      finishOutputBatch();
      if (monitor != null) {
        monitor.postFetchData(conn, ps, rs, null);
      }
    }
    catch (SQLException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
    finally {
      releasePreparedStatementAndResultSet(ps, cache, rs);
    }
  }

  @SuppressWarnings("resource")
  @Override
  public void processSelectStreaming(Connection conn, IStatementCache cache, ISelectStreamHandler handler) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    ISqlStyle sqlStyle = m_callerService.getSqlStyle();
    try {
      int rowCount = 0;
      while (hasNextInputBatch()) {
        nextInputBatch();
        prepareInputStatementAndBinds();
        dump();
        ps = cache.getPreparedStatement(conn, m_currentInputStm);
        bindBatch(ps);
        registerActiveStatement(ps);
        try {
          rs = ps.executeQuery();

          ResultSetMetaData meta = rs.getMetaData();
          int colCount = meta.getColumnCount();
          while (rs.next()) {
            ArrayList<SqlBind> row = new ArrayList<SqlBind>(colCount);
            for (int i = 0; i < colCount; i++) {
              int type = meta.getColumnType(i + 1);
              Object value = sqlStyle.readBind(rs, meta, type, i + 1);
              row.add(new SqlBind(type, value));
            }
            handler.handleRow(conn, ps, rs, rowCount, row);
            rowCount++;
            if (m_maxRowCount > 0 && rowCount >= m_maxRowCount) {
              break;
            }
          }
        }
        finally {
          unregisterActiveStatement(ps);
          /*
           * The PreparedStatement and the ResultSet of the last input batch are not allowed to be closed
           * yet because the handler could do finishing work.
           * Closing the last PreparedStatement and its ResultSet is done in the outer finally block.
           */
          if (hasNextInputBatch()) {
            releasePreparedStatementAndResultSet(ps, cache, rs);
          }
        }
      }
      finishOutputBatch();
      handler.finished(conn, ps, rs, rowCount);
    }
    catch (SQLException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
    finally {
      releasePreparedStatementAndResultSet(ps, cache, rs);
    }
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor#processModification(java.sql.Connection,
   * org.eclipse.scout
   * .rt.server.services.common.sql.internal.exec.PreparedStatementCache)
   */
  @SuppressWarnings("resource")
  @Override
  public int processModification(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor) {
    PreparedStatement ps = null;
    int rowCount = 0;
    try {
      while (hasNextInputBatch()) {
        nextInputBatch();
        prepareInputStatementAndBinds();
        dump();
        ps = cache.getPreparedStatement(conn, m_currentInputStm);
        bindBatch(ps);
        registerActiveStatement(ps);
        try {
          rowCount = rowCount + ps.executeUpdate();
        }
        finally {
          unregisterActiveStatement(ps);
          cache.releasePreparedStatement(ps);
        }
      }
      return rowCount;
    }
    catch (SQLException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
    finally {
      cache.releasePreparedStatement(ps);
    }
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor#processStoredProcedure(java.sql.Connection,
   * org.eclipse.
   * scout.rt.server.services.common.sql.internal.exec.PreparedStatementCache)
   */
  @SuppressWarnings("resource")
  @Override
  public boolean processStoredProcedure(Connection conn, IStatementCache cache, IStatementProcessorMonitor monitor) {
    CallableStatement cs = null;
    boolean status = true;
    try {
      while (hasNextInputBatch()) {
        nextInputBatch();
        prepareInputStatementAndBinds();
        dump();
        cs = cache.getCallableStatement(conn, m_currentInputStm);
        bindBatch(cs);
        registerActiveStatement(cs);
        try {
          status = status && cs.execute();
          nextOutputBatch();
          consumeOutputRow(cs);
        }
        finally {
          unregisterActiveStatement(cs);
          cache.releaseCallableStatement(cs);
        }
      }
      finishOutputBatch();
      return status;
    }
    catch (SQLException | RuntimeException e) {
      throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
          .withContextInfo("statement", createSqlDump(true, false));
    }
    finally {
      cache.releaseCallableStatement(cs);
    }
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor#createPlainText()
   */
  @Override
  public String createPlainText() {
    for (IToken t : m_ioTokens) {
      if (t instanceof ValueInputToken) {
        ValueInputToken vt = (ValueInputToken) t;
        if (vt.isPlainSql()) {
          // ok
        }
        else if (vt.isPlainValue()) {
          // ok
        }
        else {
          vt.setPlainValue(true);
        }
      }
      else if (t instanceof FunctionInputToken) {
        FunctionInputToken ft = (FunctionInputToken) t;
        ft.setPlainValue(true);
      }
    }
    if (hasNextInputBatch()) {
      nextInputBatch();
      prepareInputStatementAndBinds();
      resetInputBatch();
    }
    return m_currentInputStm;
  }

  /*
   * (non-Javadoc)
   * @seeorg.eclipse.scout.rt.server.services.common.sql.internal.exec.
   * IStatementProcessor#simulate()
   */
  @Override
  public void simulate() {
    while (hasNextInputBatch()) {
      nextInputBatch();
      prepareInputStatementAndBinds();
      dump();
    }
  }

  /**
   * when there are batch inputs, all batch inputs must agree to have another batch when there are no batch inputs, only
   * first batch is valid
   */
  private boolean hasNextInputBatch() {
    int nextIndex = m_currentInputBatchIndex + 1;
    int batchInputCount = 0;
    int batchAcceptCount = 0;
    for (IBindInput input : m_inputList) {
      if (input.isBatch()) {
        batchInputCount++;
        if (input.hasBatch(nextIndex)) {
          batchAcceptCount++;
        }
      }
    }
    if (batchInputCount > 0) {
      return batchInputCount == batchAcceptCount;
    }
    else {
      return nextIndex == 0;
    }
  }

  private void resetInputBatch() {
    m_currentInputBatchIndex = -1;
    for (IBindInput in : m_inputList) {
      in.setNextBatchIndex(m_currentInputBatchIndex);
    }
  }

  private void nextInputBatch() {
    m_currentInputBatchIndex++;
    for (IBindInput in : m_inputList) {
      in.setNextBatchIndex(m_currentInputBatchIndex);
    }
  }

  private void nextOutputBatch() {
    m_currentOutputBatchIndex++;
    for (IBindOutput out : m_outputList) {
      out.setNextBatchIndex(m_currentOutputBatchIndex);
    }
  }

  private void consumeSelectIntoRow(Object[] row) {
    int index = 0;
    for (IBindOutput out : m_outputList) {
      if (out.isSelectInto()) {
        out.consumeValue(row[index]);
        index++;
      }
    }
  }

  private void consumeOutputRow(CallableStatement cs) throws SQLException {
    for (IBindOutput out : m_outputList) {
      if (out.isJdbcBind()) {
        out.consumeValue(cs.getObject(out.getJdbcBindIndex()));
      }
    }
  }

  private void finishOutputBatch() {
    for (IBindOutput out : m_outputList) {
      out.finishBatch();
    }
  }

  private void prepareInputStatementAndBinds() {
    // bind inputs and set replace token on inputs
    m_currentInputBindMap = new TreeMap<Integer, SqlBind>();
    ISqlStyle sqlStyle = m_callerService.getSqlStyle();
    for (IBindInput in : m_inputList) {
      SqlBind bind = in.produceSqlBindAndSetReplaceToken(sqlStyle);
      assert (bind != null) == in.isJdbcBind(sqlStyle);
      if (bind != null) {
        m_currentInputBindMap.put(in.getJdbcBindIndex(), bind);
      }
    }
    // set replace token on outputs
    for (IBindOutput out : m_outputList) {
      out.setReplaceToken(sqlStyle);
    }
    m_currentInputStm = m_bindModel.getFilteredStatement();
  }

  protected String createSqlDump(boolean statementWithBinds, boolean statementPlainText) {
    StringBuilder debugBindBuf = new StringBuilder();
    if (m_currentInputBindMap == null) {
      try {
        prepareInputStatementAndBinds();
      }
      catch (Throwable t) {
        return t.getMessage();
      }
    }
    if (m_currentInputBindMap == null) {
      return "";
    }
    if (m_inputList != null) {
      for (IBindInput in : m_inputList) {
        SqlBind bind = m_currentInputBindMap.get(in.getJdbcBindIndex());
        if (bind == null) {
          continue;
        }
        debugBindBuf.append("IN  ");
        debugBindBuf.append(in.getToken().getParsedToken());
        debugBindBuf.append(" => ");
        debugBindBuf.append(in.getToken().getReplaceToken());
        debugBindBuf.append(" [");
        debugBindBuf.append(SqlBind.decodeJdbcType(bind.getSqlType()));
        switch (bind.getSqlType()) {
          case Types.BLOB:
          case Types.CLOB: {
            //nop
            break;
          }
          default: {
            debugBindBuf.append(" ");
            debugBindBuf.append(bind.getValue());
          }
        }
        debugBindBuf.append("]");
        debugBindBuf.append("\n");
      }
    }
    if (m_outputList != null) {
      for (IBindOutput out : m_outputList) {
        debugBindBuf.append("OUT ");
        debugBindBuf.append(out.getToken().getParsedToken());
        debugBindBuf.append(" => ");
        debugBindBuf.append(out.getToken().getReplaceToken());
        Class bindType = out.getBindType();
        if (bindType != null) {
          debugBindBuf.append(" [");
          debugBindBuf.append(bindType.getSimpleName());
          debugBindBuf.append("]");
        }
        debugBindBuf.append("\n");
      }
    }
    StringBuilder buf = new StringBuilder();
    if (statementWithBinds) {
      buf.append("SQL with binds:\n");
      buf.append(SqlFormatter.wellform(m_originalStm).trim());
      if (debugBindBuf != null && debugBindBuf.length() > 0) {
        buf.append("\n");
        buf.append(debugBindBuf.toString().trim());
      }
    }
    if (statementPlainText) {
      String p = "" + m_currentInputStm;
      ArrayList<SqlBind> bindList = new ArrayList<SqlBind>(m_currentInputBindMap.values());
      int pos = findNextBind(p, 0);
      int bindIndex = 0;
      while (pos >= 0 && bindIndex < bindList.size()) {
        SqlBind bind = bindList.get(bindIndex);
        String replacement;
        switch (bind.getSqlType()) {
          case Types.BLOB: {
            replacement = "__BLOB__";
            break;
          }
          case Types.CLOB: {
            replacement = "__CLOB__";
            break;
          }
          default: {
            replacement = m_callerService.getSqlStyle().toPlainText(bind.getValue());
          }
        }
        if (replacement == null) {
          replacement = "NULL";
        }
        replacement = replacement.replace('?', ' ');
        //next
        p = p.substring(0, pos) + replacement + p.substring(pos + 1);
        pos = findNextBind(p, pos);
        bindIndex++;
      }
      if (buf.length() > 0) {
        buf.append("\n");
      }
      buf.append("SQL PLAIN Log:\n");
      buf.append(SqlFormatter.wellform(p).trim());
    }
    return buf.toString();
  }

  private static int findNextBind(String s, int start) {
    if (s == null || start < 0 || start >= s.length()) {
      return -1;
    }
    int candidate = s.indexOf('?', start);
    if (candidate < 0) {
      return -1;
    }
    P_TextSectionFinder red = new P_TextSectionFinder(s);
    while (red.find()) {
      if (candidate < red.start()) {
        //outside red section
        return candidate;
      }
      if (candidate >= red.start() && candidate < red.end()) {
        //inside red section, find next candidate outside red section
        candidate = s.indexOf('?', red.end());
        if (candidate < 0) {
          return -1;
        }
        //got next candidate after red section
      }
      //continue with next red section
    }
    return candidate;
  }

  /**
   * Replacement for:
   * <p>
   * <code>
   * Pattern TEXT_SECTION_PATTERN = Pattern.compile("'([^']|'')*'", Pattern.DOTALL);
   * Matcher red = TEXT_SECTION_PATTERN.matcher(s);
   * </code>
   * <p>
   * which is broken due to <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6337993">Sun bug 6337993</a>
   * when matching large (> ~1000 characters) texts.
   */
  private static class P_TextSectionFinder {

    private final String m_string;
    private P_TextSection m_currentTextSection = null;

    public P_TextSectionFinder(String s) {
      m_string = s;
    }

    public boolean find() {
      int start = 0;
      if (m_currentTextSection != null) {
        start = m_currentTextSection.to + 1;
      }
      m_currentTextSection = findNextTextSection(m_string, start);
      return (m_currentTextSection != null);
    }

    public int start() {
      return (m_currentTextSection == null ? -1 : m_currentTextSection.from);
    }

    public int end() {
      return (m_currentTextSection == null ? -1 : m_currentTextSection.to);
    }

    private static P_TextSection findNextTextSection(String s, int start) {
      if (s == null || start < 0 || start >= s.length()) {
        return null;
      }
      int secStart = -1;
      int pos = start;
      while (pos < s.length()) {
        char c = s.charAt(pos);
        if (c == '\'') {
          if (secStart < 0) {
            // Outside section -> start a new one
            secStart = pos;
          }
          else {
            // Inside section
            if (pos == s.length() - 1 || s.charAt(pos + 1) != '\'') {
              // Last position or next position is not a ' -> we found a section
              return new P_TextSection(secStart, pos + 1);
            }
            // Skip double '
            pos++;
          }
        }
        pos++;
      }
      return null;
    }

    @SuppressWarnings("squid:S00116")
    private static class P_TextSection {
      final int from;
      final int to;

      public P_TextSection(int from, int to) {
        this.from = from;
        this.to = to;
      }
    }
  }

  protected void dump() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("\n" + createSqlDump(true, true));
    }
    else if (LOG.isInfoEnabled()) {
      LOG.info("\n" + createSqlDump(true, false));
    }
  }

  private void bindBatch(PreparedStatement ps) {
    try {
      // bind inputs
      if (ps instanceof PreparedStatement) {
        writeBinds(ps);
      }
      // register outputs
      if (ps instanceof CallableStatement) {
        registerOutputs((CallableStatement) ps);
      }
    }
    catch (SQLException e) {
      throw new ProcessingException("unexpected exception", e);
    }
  }

  protected void writeBinds(PreparedStatement ps) throws SQLException {
    ISqlStyle sqlStyle = m_callerService.getSqlStyle();
    for (Map.Entry<Integer, SqlBind> e : m_currentInputBindMap.entrySet()) {
      sqlStyle.writeBind(ps, e.getKey(), e.getValue());
    }
  }

  protected void registerOutputs(CallableStatement cs) throws SQLException {
    ISqlStyle sqlStyle = m_callerService.getSqlStyle();
    for (IBindOutput out : m_outputList) {
      if (out.isJdbcBind()) {
        sqlStyle.registerOutput(cs, out.getJdbcBindIndex(), out.getBindType());
      }
    }
  }

  private IBindInput createInput(IToken bindToken, Object[] bindBases) {
    IBindInput o = null;
    if (bindToken instanceof ValueInputToken) {
      ValueInputToken valueInputToken = (ValueInputToken) bindToken;
      String[] path = valueInputToken.getName().split("[.]");
      for (int i = 0; i < bindBases.length; i++) {
        Object bindBase = bindBases[i];
        Class nullType = null;
        if (bindBase instanceof NVPair) {
          nullType = ((NVPair) bindBase).getNullType();
        }
        o = createInputRec(valueInputToken, path, bindBases[i], nullType);
        if (o != null) {
          break;
        }
      }
      if (o == null) {
        throw new ProcessingException("Cannot find input for '" + valueInputToken + "' in bind bases.");
      }
    }
    else if (bindToken instanceof FunctionInputToken) {
      o = new FunctionInput(m_callerService, m_bindBases, (FunctionInputToken) bindToken);
    }
    return o;
  }

  private IBindInput createInputRec(ValueInputToken bindToken, String[] path, Object bindBase, Class nullType) {
    boolean terminal = (path.length == 1);
    Object o = null;
    boolean found = false;
    if (bindBase instanceof Map) {
      // handle all terminal cases for map
      o = ((Map) bindBase).get(path[0]);
      if (o != null) {
        found = true;
      }
      else if (((Map) bindBase).containsKey(path[0])) {
        found = true;
      }
      if (found) {
        // special case: table holder and table filter are preemptive terminals
        if (o instanceof ITableHolder) {
          return new TableHolderInput((ITableHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof TableHolderFilter) {
          return new TableHolderInput(((TableHolderFilter) o).getTableHolder(), ((TableHolderFilter) o).getFilteredRows(), path[1], bindToken);
        }
        else if (o instanceof ITableBeanHolder) {
          return new TableBeanHolderInput((ITableBeanHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof TableBeanHolderFilter) {
          return new TableBeanHolderInput(((TableBeanHolderFilter) o).getTableBeanHolder(), ((TableBeanHolderFilter) o).getFilteredRows(), path[1], bindToken);
        }
        else if (o instanceof IBeanArrayHolder) {
          return new BeanArrayHolderInput((IBeanArrayHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof BeanArrayHolderFilter) {
          return new BeanArrayHolderInput(((BeanArrayHolderFilter) o).getBeanArrayHolder(), ((BeanArrayHolderFilter) o).getFilteredBeans(), path[1], bindToken);
        }
        else {
          if (terminal) {
            return createInputTerminal(o, nullType, bindToken);
          }
          else {
            if (o == null) {
              throw new ProcessingException("input bind " + bindToken + " resolves to null on path element: " + path[0]);
            }
          }
        }
      }
    }
    else if (bindBase instanceof NVPair) {
      // handle all terminal cases for nvpair
      if (((NVPair) bindBase).getName().equals(path[0])) {
        o = ((NVPair) bindBase).getValue();
        found = true;
        // special case: table holder and table filter are preemptive terminals
        if (o instanceof ITableHolder) {
          return new TableHolderInput((ITableHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof TableHolderFilter) {
          return new TableHolderInput(((TableHolderFilter) o).getTableHolder(), ((TableHolderFilter) o).getFilteredRows(), path[1], bindToken);
        }
        else if (o instanceof ITableBeanHolder) {
          return new TableBeanHolderInput((ITableBeanHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof TableBeanHolderFilter) {
          return new TableBeanHolderInput(((TableBeanHolderFilter) o).getTableBeanHolder(), ((TableBeanHolderFilter) o).getFilteredRows(), path[1], bindToken);
        }
        else if (o instanceof IBeanArrayHolder) {
          return new BeanArrayHolderInput((IBeanArrayHolder) o, null, path[1], bindToken);
        }
        else if (o instanceof BeanArrayHolderFilter) {
          return new BeanArrayHolderInput(((BeanArrayHolderFilter) o).getBeanArrayHolder(), ((BeanArrayHolderFilter) o).getFilteredBeans(), path[1], bindToken);
        }
        else {
          if (terminal) {
            return createInputTerminal(o, nullType, bindToken);
          }
          else {
            if (o == null) {
              throw new ProcessingException("input bind " + bindToken + " resolves to null on path element: " + path[0]);
            }
          }
        }
      }
    }
    else if (bindBase instanceof ITableHolder) {
      // handle all terminal cases for table holder
      ITableHolder table = (ITableHolder) bindBase;
      try {
        Method m = table.getClass().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1), new Class[]{int.class});
        if (m != null) {
          found = true;
          return new TableHolderInput(table, null, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        found = false;
        // nop
      }
    }
    else if (bindBase instanceof TableHolderFilter) {
      // handle all terminal cases for table holder filter
      TableHolderFilter filter = (TableHolderFilter) bindBase;
      ITableHolder table = filter.getTableHolder();
      try {
        Method m = table.getClass().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1), new Class[]{int.class});
        if (m != null) {
          found = true;
          return new TableHolderInput(table, filter.getFilteredRows(), path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        // nop
        found = false;
      }
    }
    else if (bindBase instanceof ITableBeanHolder) {
      // handle all terminal cases for table holder
      ITableBeanHolder table = (ITableBeanHolder) bindBase;
      try {
        Method m = table.getRowType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new TableBeanHolderInput(table, null, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        found = false;
        // nop
      }
    }
    else if (bindBase instanceof TableBeanHolderFilter) {
      // handle all terminal cases for table holder filter
      TableBeanHolderFilter filter = (TableBeanHolderFilter) bindBase;
      ITableBeanHolder table = filter.getTableBeanHolder();
      try {
        Method m = table.getRowType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new TableBeanHolderInput(table, filter.getFilteredRows(), path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        // nop
        found = false;
      }
    }
    else if (bindBase instanceof IBeanArrayHolder) {
      // handle all terminal cases for BeanArrayHolder
      IBeanArrayHolder<?> holder = (IBeanArrayHolder) bindBase;
      try {
        Method m = holder.getHolderType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new BeanArrayHolderInput(holder, null, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t1) {
        try {
          Method m = holder.getHolderType().getMethod("is" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
          if (m != null) {
            found = true;
            return new BeanArrayHolderInput(holder, null, path[0], bindToken);
          }
        }
        catch (NoSuchMethodException | SecurityException t2) {
          found = false;
          // nop
        }
      }
    }
    else if (bindBase instanceof BeanArrayHolderFilter) {
      // handle all terminal cases for table holder filter
      BeanArrayHolderFilter filter = (BeanArrayHolderFilter) bindBase;
      IBeanArrayHolder<?> holder = filter.getBeanArrayHolder();
      try {
        Method m = holder.getHolderType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new BeanArrayHolderInput(holder, filter.getFilteredBeans(), path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t1) {
        try {
          Method m = holder.getHolderType().getMethod("is" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
          if (m != null) {
            found = true;
            return new BeanArrayHolderInput(holder, null, path[0], bindToken);
          }
        }
        catch (NoSuchMethodException | SecurityException t2) {
          found = false;
          // nop
        }
      }
    }
    else if (bindBase != null) {
      if (bindBase.getClass().isArray() && terminal) {
        return new BeanPropertyInput(path[0], (Object[]) bindBase, bindToken);
      }
      if (bindBase instanceof Collection && terminal) {
        return new BeanPropertyInput(path[0], ((Collection) bindBase).toArray(), bindToken);
      }
      /* bean propertry */
      try {
        Object propertyBean = bindBase;
        FastPropertyDescriptor pd = BeanUtility.getFastBeanInfo(propertyBean.getClass(), null).getPropertyDescriptor(path[0]);
        Method getter = pd != null ? pd.getReadMethod() : null;
        if (getter != null) {
          // getter exists
          o = getter.invoke(propertyBean);
          found = true;
          if (terminal) {
            return createInputTerminal(o, getter.getReturnType(), bindToken);
          }
          else {
            if (o == null) {
              throw new ProcessingException("input bind " + bindToken + " resolves to null on path element: " + path[0]);
            }
          }
        }
      }
      catch (Exception e) {
        // obviously there is no such property
      }
    }
    //
    if (found) {
      if (terminal) {
        throw new ProcessingException("input bind '" + bindToken.getName() + "' was not recognized as a terminal");
      }
      // continue
      String[] newPath = new String[path.length - 1];
      System.arraycopy(path, 1, newPath, 0, newPath.length);
      IBindInput input = null;
      if (o instanceof IHolder<?>) {
        /* dereference value if current object is an IHolder. If input cannot be resolved (i.e. ProcessingException occurs),
         * search given property names on holder. Hence both of the following forms are supported on holder types:
         * :holder.property
         * :holder.value.property
         */
        try {
          input = createInputRec(bindToken, newPath, ((IHolder) o).getValue(), nullType);
        }
        catch (RuntimeException pe) {
          // nop, see below
        }
      }
      if (input == null) {
        // strict search without dereferncing holder objects
        input = createInputRec(bindToken, newPath, o, nullType);
      }
      return input;
    }
    else {
      return null;
    }
  }

  private IBindOutput createOutput(IToken bindToken, Object[] bindBases) {
    IBindOutput o = null;
    if (bindToken instanceof ValueOutputToken) {
      ValueOutputToken valueOutputToken = (ValueOutputToken) bindToken;
      String[] path = valueOutputToken.getName().split("[.]");
      for (int i = 0; i < bindBases.length; i++) {
        o = createOutputRec(valueOutputToken, path, bindBases[i]);
        if (o != null) {
          break;
        }
      }
      if (o == null) {
        throw new ProcessingException("Cannot find output for '" + valueOutputToken + "' in bind base. When selecting into shared context variables make sure these variables are initialized using CONTEXT.set<i>PropertyName</i>(null)");
      }
    }
    else {
      throw new ProcessingException("Cannot find output for '" + bindToken.getClass());
    }
    return o;
  }

  @SuppressWarnings("unchecked")
  private IBindOutput createOutputRec(ValueOutputToken bindToken, String[] path, final Object bindBase) {
    boolean terminal = (path.length == 1);
    Object o = null;
    boolean found = false;
    if (bindBase instanceof Map) {
      // handle all terminal cases for map
      o = ((Map) bindBase).get(path[0]);
      if (o != null) {
        found = true;
      }
      else if (((Map) bindBase).containsKey(path[0])) {
        found = true;
      }
      if (found) {
        // special case: table holder is preemptive terminal
        if (o instanceof ITableHolder) {
          ITableHolder table = (ITableHolder) o;
          return new TableHolderOutput(table, path[1], bindToken);
        }
        if (o instanceof ITableBeanHolder) {
          ITableBeanHolder table = (ITableBeanHolder) o;
          return new TableBeanHolderOutput(table, path[1], bindToken);
        }
        else if (o instanceof IBeanArrayHolder) {
          IBeanArrayHolder holder = (IBeanArrayHolder) o;
          return new BeanArrayHolderOutput(holder, path[1], bindToken);
        }
        else if (o instanceof IHolder) {
          if (terminal) {
            return createOutputTerminal((IHolder) o, bindToken);
          }
          else {
            o = ((IHolder) o).getValue();
          }
        }
        else if (o == null) {
          if (terminal) {
            return new MapOutput((Map) bindBase, path[0], bindToken);
          }
          else {
            throw new ProcessingException("output bind " + bindToken + " resolves to null on path element: " + path[0]);
          }
        }
        else {
          if (terminal) {
            return new MapOutput((Map) bindBase, path[0], bindToken);
          }
        }
      }
    }
    else if (bindBase instanceof NVPair) {
      // handle all terminal cases for nvpair
      if (((NVPair) bindBase).getName().equals(path[0])) {
        o = ((NVPair) bindBase).getValue();
        found = true;
        // special case: table holder is preemptive terminal
        if (o instanceof ITableHolder) {
          ITableHolder table = (ITableHolder) o;
          return new TableHolderOutput(table, path[1], bindToken);
        }
        else if (o instanceof ITableBeanHolder) {
          ITableBeanHolder table = (ITableBeanHolder) o;
          return new TableBeanHolderOutput(table, path[1], bindToken);
        }
        else if (o instanceof IBeanArrayHolder) {
          IBeanArrayHolder holder = (IBeanArrayHolder) o;
          return new BeanArrayHolderOutput(holder, path[1], bindToken);
        }
        else if (o instanceof IHolder) {
          if (terminal) {
            return createOutputTerminal((IHolder) o, bindToken);
          }
          else {
            o = ((IHolder) o).getValue();
          }
        }
        else if (o == null) {
          throw new ProcessingException("output bind " + bindToken + " resolves to null on path element: " + path[0]);
        }
        else {
          if (terminal) {
            throw new ProcessingException("output bind " + bindToken + " is not a valid output container");
          }
        }
      }
    }
    else if (bindBase instanceof ITableHolder) {
      // handle all terminal cases for table holder
      ITableHolder table = (ITableHolder) bindBase;
      try {
        Method m = table.getClass().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1), new Class[]{int.class});
        if (m != null) {
          found = true;
          return new TableHolderOutput(table, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        // nop
        found = false;
      }
    }
    else if (bindBase instanceof ITableBeanHolder) {
      // handle all terminal cases for table holder
      ITableBeanHolder table = (ITableBeanHolder) bindBase;
      try {
        Method m = table.getRowType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new TableBeanHolderOutput(table, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t) {
        // nop
        found = false;
      }
    }
    else if (bindBase instanceof IBeanArrayHolder) {
      // handle all terminal cases for BeanArrayHolder
      IBeanArrayHolder holder = (IBeanArrayHolder) bindBase;
      try {
        Method m = holder.getHolderType().getMethod("get" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
        if (m != null) {
          found = true;
          return new BeanArrayHolderOutput(holder, path[0], bindToken);
        }
      }
      catch (NoSuchMethodException | SecurityException t1) {
        try {
          Method m = holder.getHolderType().getMethod("is" + Character.toUpperCase(path[0].charAt(0)) + path[0].substring(1));
          if (m != null) {
            found = true;
            return new BeanArrayHolderOutput(holder, path[0], bindToken);
          }
        }
        catch (NoSuchMethodException | SecurityException t2) {
          found = false;
          // nop
        }
      }
    }
    else/* bean property */ {
      // handle all terminal cases for bean property
      try {
        FastPropertyDescriptor pd = BeanUtility.getFastBeanInfo(bindBase.getClass(), null).getPropertyDescriptor(path[0]);
        if (terminal) {
          Method setter = pd != null ? pd.getWriteMethod() : null;
          if (setter != null) {
            found = true;
            return new AbstractBeanPropertyOutput(bindBase.getClass(), path[0], bindToken) {
              @Override
              protected Object[] getFinalBeanArray() {
                return new Object[]{bindBase};
              }
            };
          }
          else {
            Method getter = pd != null ? pd.getReadMethod() : null;
            if (getter != null) {
              o = getter.invoke(bindBase, (Object[]) null);
              if (o instanceof ITableHolder) {
                throw new ProcessingException("output bind '" + bindToken.getName() + "' is a table and should not be a terminal");
              }
              else if (o instanceof ITableBeanHolder) {
                throw new ProcessingException("output bind '" + bindToken.getName() + "' is a table bean and should not be a terminal");
              }
              else if (o instanceof IBeanArrayHolder) {
                throw new ProcessingException("output bind '" + bindToken.getName() + "' is a bean array and should not be a terminal");
              }
              else if (o instanceof IHolder) {
                return createOutputTerminal((IHolder) o, bindToken);
              }
              else {
                throw new ProcessingException("output bind '" + bindToken.getName() + "' is not a holder");
              }
            }
          }
        }
        else {
          Method getter = pd != null ? pd.getReadMethod() : null;
          if (getter != null) {
            Object readValue = getter.invoke(bindBase, (Object[]) null);
            o = readValue;
            found = true;
          }
        }
      }
      catch (Exception e) {
        // obviously there is no such property
      }
    }
    //
    if (found) {
      if (terminal) {
        throw new ProcessingException("output bind '" + bindToken.getName() + "' was not recognized as a terminal");
      }
      // continue
      String[] newPath = new String[path.length - 1];
      System.arraycopy(path, 1, newPath, 0, newPath.length);
      return createOutputRec(bindToken, newPath, o);
    }
    else {
      return null;
    }
  }

  private IBindOutput createOutputTerminal(IHolder h, ValueOutputToken bindToken) {
    Class cls = h.getHolderType();
    if (Collection.class.isAssignableFrom(cls)) {
      return new CollectionHolderOutput(h, bindToken);
    }
    else if (cls.isArray()) {
      // byte[] and char[] are no "arrays"
      if (cls == byte[].class || cls == char[].class) {
        return new SingleHolderOutput(h, bindToken);
      }
      else {
        return new ArrayHolderOutput(h, bindToken);
      }
    }
    else {
      return new SingleHolderOutput(h, bindToken);
    }
  }

  private IBindInput createInputTerminal(Object o, Class nullType, ValueInputToken bindToken) {
    if (o == null) {
      return new SingleInput(null, nullType, bindToken);
    }
    else if (o instanceof IHolder) {
      Class cls = ((IHolder) o).getHolderType();
      if (nullType == null) {
        nullType = cls;
      }
      if (Collection.class.isAssignableFrom(cls)) {
        Collection value = (Collection) ((IHolder) o).getValue();
        if (value == null) {
          return new ArrayInput(m_callerService.getSqlStyle(), null, bindToken);
        }
        else {
          return new ArrayInput(m_callerService.getSqlStyle(), value.toArray(), bindToken);
        }
      }
      else if (cls.isArray()) {
        // byte[] and char[] are no "arrays"
        if (cls == byte[].class || cls == char[].class) {
          return new SingleInput(((IHolder) o).getValue(), nullType, bindToken);
        }
        else {
          return new ArrayInput(m_callerService.getSqlStyle(), ((IHolder) o).getValue(), bindToken);
        }
      }
      else if (cls == TriState.class) {
        return new TriStateInput(m_callerService.getSqlStyle(), (TriState) ((IHolder) o).getValue(), bindToken);
      }
      else {
        return new SingleInput(((IHolder) o).getValue(), nullType, bindToken);
      }
    }
    else {
      if (o instanceof Collection) {
        return new ArrayInput(m_callerService.getSqlStyle(), ((Collection) o).toArray(), bindToken);
      }
      else if (o.getClass().isArray()) {
        Class cls = o.getClass();
        // byte[] and char[] are no "arrays"
        if (cls == byte[].class || cls == char[].class) {
          return new SingleInput(o, nullType, bindToken);
        }
        else {
          return new ArrayInput(m_callerService.getSqlStyle(), o, bindToken);
        }
      }
      else if (o.getClass() == TriState.class) {
        return new TriStateInput(m_callerService.getSqlStyle(), (TriState) o, bindToken);
      }
      else {
        return new SingleInput(o, nullType, bindToken);
      }
    }
  }

  protected void processDatabaseSpecificToken(DatabaseSpecificToken t, ISqlStyle sqlStyle) {
    String name = t.getName().toLowerCase();
    if ("sysdate".equals(name)) {
      t.setReplaceToken(sqlStyle.getSysdateToken());
    }
    else if ("upper".equals(name)) {
      t.setReplaceToken(sqlStyle.getUpperToken());
    }
    else if ("lower".equals(name)) {
      t.setReplaceToken(sqlStyle.getLowerToken());
    }
    else if ("trim".equals(name)) {
      t.setReplaceToken(sqlStyle.getTrimToken());
    }
    else if ("nvl".equals(name)) {
      t.setReplaceToken(sqlStyle.getNvlToken());
    }
    else {
      LOG.warn("used unknown database specific token {}", t.getParsedToken());
      t.setReplaceToken(name);
    }
  }

  protected void registerActiveStatement(Statement s) throws SQLException {
    ITransaction tx = ITransaction.CURRENT.get();
    if (tx == null) {
      return;
    }
    ITransactionMember member = tx.getMember(getCallerService().getTransactionMemberId());
    if (member instanceof AbstractSqlTransactionMember) {
      ((AbstractSqlTransactionMember) member).registerActiveStatement(s);
    }
  }

  protected void unregisterActiveStatement(Statement s) throws SQLException {
    ITransaction tx = ITransaction.CURRENT.get();
    if (tx == null) {
      return;
    }
    ITransactionMember member = tx.getMember(getCallerService().getTransactionMemberId());
    if (member instanceof AbstractSqlTransactionMember) {
      ((AbstractSqlTransactionMember) member).unregisterActiveStatement(s);
    }
  }

  private void releasePreparedStatementAndResultSet(PreparedStatement ps, IStatementCache cache, ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      }
      catch (Throwable t) {
      }
    }
    cache.releasePreparedStatement(ps);
  }
}
