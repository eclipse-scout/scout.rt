/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jdbc.internal.exec;

import java.lang.reflect.InvocationTargetException;
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
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.BeanArrayHolderFilter;
import org.eclipse.scout.rt.platform.holders.IBeanArrayHolder;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.ITableBeanHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;
import org.eclipse.scout.rt.platform.reflect.FastPropertyDescriptor;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1166")
public class StatementProcessor implements IStatementProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(StatementProcessor.class);
  private static final Pattern REGEX_DOT = Pattern.compile("[.]");

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
  private final Object[] m_bindBases;
  private final int m_maxRowCount;
  private final int m_maxFetchMemorySize;
  private final BindModel m_bindModel;
  private final IToken[] m_ioTokens;
  private final List<IBindInput> m_inputList;
  private final List<IBindOutput> m_outputList;
  // state
  private int m_maxFetchSize = -1;
  private int m_currentInputBatchIndex = -1;
  private int m_currentOutputBatchIndex = -1;
  private String m_currentInputStm;
  private TreeMap<Integer/* jdbcBindIndex */, SqlBind> m_currentInputBindMap;

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
      // add session to binds if available
      final IServerSession session = ServerSessionProvider.currentSession();
      if (session != null) {
        if (bindBases == null) {
          m_bindBases = new Object[]{session};
        }
        else {
          m_bindBases = new Object[bindBases.length + 1];
          System.arraycopy(bindBases, 0, m_bindBases, 0, bindBases.length);
          m_bindBases[m_bindBases.length - 1] = session;
        }
      }
      else {
        if (bindBases == null) {
          m_bindBases = new Object[]{};
        }
        else {
          m_bindBases = bindBases;
        }
      }
      //
      m_inputList = new ArrayList<>();
      m_outputList = new ArrayList<>();
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
          if (in.isJdbcBind(sqlStyle)) { // NOSONAR
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
          throw new ProcessingException("out parameter is not a 'select into': {}", out);
        }
        if (out.isJdbcBind()) {
          throw new ProcessingException("out parameter is a jdbc bind: {}", out);
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
    List<Object[]> rows = new ArrayList<>();
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
      ArrayList<Object[]> rows = new ArrayList<>();
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

  @SuppressWarnings({"resource", "squid:S2095"})
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
            List<SqlBind> row = new ArrayList<>(colCount);
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
    m_currentInputBindMap = new TreeMap<>();
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
      catch (Exception t) {
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
      String p = m_currentInputStm != null ? m_currentInputStm : "";
      List<SqlBind> bindList = new ArrayList<>(m_currentInputBindMap.values());
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
        p = p.substring(0, pos) + replacement + p.substring(pos + 1);//NOSONAR squid:S2259
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
      writeBinds(ps);
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
    for (Entry<Integer, SqlBind> e : m_currentInputBindMap.entrySet()) {
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
    if (bindToken instanceof ValueInputToken) {
      final ValueInputToken valueInputToken = (ValueInputToken) bindToken;
      final String[] path = REGEX_DOT.split(valueInputToken.getName());

      IBindInput result = null;
      for (final Object bindBase : bindBases) {
        Class nullType = null;
        if (bindBase instanceof NVPair) {
          nullType = ((NVPair) bindBase).getNullType();
        }
        final IBindInput in = createInputRec(valueInputToken, path, bindBase, nullType);
        if (in != null) {
          // bind found
          if (isBindDuplicateCheckEnabled()) {
            if (result == null) {
              // first match found for the bind -> remember
              result = in;
            }
            else {
              // second match found
              onDuplicateBind(valueInputToken);
              return result;
            }
          }
          else {
            // no duplicate check necessary: directly return the first match
            return in;
          }
        }
      }

      if (result == null) {
        throw new ProcessingException("Cannot find input for '{}' in bind bases.", valueInputToken);
      }
      return result;
    }
    else if (bindToken instanceof FunctionInputToken) {
      return new FunctionInput(m_callerService, m_bindBases, (FunctionInputToken) bindToken);
    }
    throw new ProcessingException("Cannot find input for {}", bindToken.getClass());
  }

  protected boolean isBindDuplicateCheckEnabled() {
    return Platform.get().inDevelopmentMode();
  }

  protected void onDuplicateBind(IToken token) {
    LOG.warn("Multiple matches for bind '{}'.", token);
  }

  @SuppressWarnings({"squid:S2583", "squid:S138"})
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
        if (o instanceof ITableBeanHolder) {
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
              throw new ProcessingException("input bind {} resolves to null on path element: {}", bindToken, path[0]);
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
        if (o instanceof ITableBeanHolder) {
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
              throw new ProcessingException("input bind {} resolves to null on path element: {}", bindToken, path[0]);
            }
          }
        }
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
      /* bean property */
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
              throw new ProcessingException("input bind {} resolves to null on path element: {}", bindToken, path[0]);
            }
          }
        }
      }
      catch (IllegalAccessException | InvocationTargetException e) {
        LOG.warn("Exception while invoking bean getter", e);
        LOG.debug("Cannot access property.", e);
      }
    }
    //
    if (found) {
      if (terminal) {
        throw new ProcessingException("input bind '{}' was not recognized as a terminal", bindToken.getName());
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

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  private IBindOutput createOutput(IToken bindToken, Object[] bindBases) {
    if (bindToken instanceof ValueOutputToken) {
      IBindOutput result = null;
      ValueOutputToken valueOutputToken = (ValueOutputToken) bindToken;
      String[] path = REGEX_DOT.split(valueOutputToken.getName());
      for (Object bindBase : bindBases) {
        IBindOutput out = createOutputRec(valueOutputToken, path, bindBase);
        if (out != null) {
          if (isBindDuplicateCheckEnabled()) {
            if (result == null) {
              // first match found for the bind -> remember
              result = out;
            }
            else {
              // second match found
              onDuplicateBind(valueOutputToken);
              return result;
            }
          }
          else {
            // no duplicate check necessary: directly return the first match
            return out;
          }
        }
      }

      if (result == null) {
        throw new ProcessingException("Cannot find output for '{}' in bind base. When selecting into shared context variables make sure these variables are initialized using CONTEXT.set<i>PropertyName</i>(null)", valueOutputToken);
      }
      return result;
    }
    throw new ProcessingException("Cannot find output for {}", bindToken.getClass());
  }

  @SuppressWarnings({"unchecked", "squid:S2583", "squid:S138"})
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
            throw new ProcessingException("output bind {} resolves to null on path element: {}", bindToken, path[0]);
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
          throw new ProcessingException("output bind {} resolves to null on path element: {}", bindToken, path[0]);
        }
        else {
          if (terminal) {
            throw new ProcessingException("output bind {} is not a valid output container", bindToken);
          }
        }
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
              if (o instanceof ITableBeanHolder) {
                throw new ProcessingException("output bind '{}' is a table bean and should not be a terminal", bindToken.getName());
              }
              else if (o instanceof IBeanArrayHolder) {
                throw new ProcessingException("output bind '{}' is a bean array and should not be a terminal", bindToken.getName());
              }
              else if (o instanceof IHolder) {
                return createOutputTerminal((IHolder) o, bindToken);
              }
              else {
                return null;
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
      catch (IllegalAccessException | InvocationTargetException e) {
        LOG.warn("Exception while invoking bean getter", e);
      }
    }
    //
    if (found) {
      if (terminal) {
        throw new ProcessingException("output bind '{}' was not recognized as a terminal", bindToken.getName());
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

  protected void unregisterActiveStatement(Statement s) {
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
      catch (Exception e) {
        LOG.warn("could not close ResultSet", e);
      }
    }
    cache.releasePreparedStatement(ps);
  }
}
