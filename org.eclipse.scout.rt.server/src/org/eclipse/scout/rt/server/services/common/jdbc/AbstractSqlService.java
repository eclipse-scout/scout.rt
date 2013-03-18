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
package org.eclipse.scout.rt.server.services.common.jdbc;

import java.lang.reflect.Method;
import java.security.Permission;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.BundleContextUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.holders.LongHolder;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.exec.PreparedStatementCache;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.exec.StatementProcessor;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.legacy.LegacyStatementBuilder;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.pool.SqlConnectionBuilder;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.pool.SqlConnectionPool;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.jdbc.ILegacySqlQueryService;
import org.eclipse.scout.rt.shared.services.common.jdbc.LegacySearchFilter;
import org.eclipse.scout.rt.shared.services.common.jdbc.LegacySearchFilter.WhereToken;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IServiceInventory;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractSqlService extends AbstractService implements ISqlService, ILegacySqlQueryService, IAdaptable {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSqlService.class);
  public static final int DEFAULT_MEMORY_PREFETCH_SIZE = 1048576; // = 1MB default

  private SqlConnectionPool m_pool;
  private Class<? extends ScoutTexts> m_nlsProvider;
  private ISqlStyle m_sqlStyle;
  private String m_transactionMemberId;
  private boolean m_directJdbcConnection;
  private String m_jndiName;
  private String m_jndiInitialContextFactory;
  private String m_jndiProviderUrl;
  private String m_jndiUrlPkgPrefixes;
  private String m_jdbcMappingName;
  private String m_jdbcDriverName;
  private String m_jdbcProps;
  private int m_jdbcPoolSize;
  private long m_jdbcPoolConnectionLifetime;
  private long m_jdbcPoolConnectionBusyTimeout;
  private String m_defaultUser;
  private String m_defaultPass;
  private int m_queryCacheSize;
  private int m_maxFetchMemorySize = DEFAULT_MEMORY_PREFETCH_SIZE;

  //
  private HashMap<String, List<BundleClassDescriptor>> m_permissionNameToDescriptor;
  private HashMap<String, List<BundleClassDescriptor>> m_codeNameToDescriptor;

  public AbstractSqlService() {
    initConfig();
  }

  @SuppressWarnings("deprecation")
  @Override
  public void initializeService() {
    super.initializeService();

    // load code and permission names
    m_permissionNameToDescriptor = new HashMap<String, List<BundleClassDescriptor>>();
    IPermissionService psvc = SERVICES.getService(IPermissionService.class);
    if (psvc != null) {
      for (BundleClassDescriptor d : psvc.getAllPermissionClasses()) {
        List<BundleClassDescriptor> list = m_permissionNameToDescriptor.get(d.getSimpleClassName());
        if (list == null) {
          list = new ArrayList<BundleClassDescriptor>();
          m_permissionNameToDescriptor.put(d.getSimpleClassName(), list);
        }
        list.add(d);
        //
        list = m_permissionNameToDescriptor.get(d.getClassName());
        if (list == null) {
          list = new ArrayList<BundleClassDescriptor>();
          m_permissionNameToDescriptor.put(d.getClassName(), list);
        }
        list.add(d);
      }
    }
    m_codeNameToDescriptor = new HashMap<String, List<BundleClassDescriptor>>();
    ICodeService csvc = SERVICES.getService(ICodeService.class);
    if (csvc != null) {
      for (BundleClassDescriptor d : csvc.getAllCodeTypeClasses("")) {
        List<BundleClassDescriptor> list = m_codeNameToDescriptor.get(d.getSimpleClassName());
        if (list == null) {
          list = new ArrayList<BundleClassDescriptor>();
          m_codeNameToDescriptor.put(d.getSimpleClassName(), list);
        }
        list.add(d);
        //
        list = m_codeNameToDescriptor.get(d.getClassName());
        if (list == null) {
          list = new ArrayList<BundleClassDescriptor>();
          m_codeNameToDescriptor.put(d.getClassName(), list);
        }
        list.add(d);
      }
    }
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredDirectJdbcConnection() {
    return true;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredUsername() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  @ConfigPropertyValue("null")
  protected String getConfiguredPassword() {
    return null;
  }

  @ConfigProperty(ConfigProperty.NLS_PROVIDER)
  @Order(70)
  @ConfigPropertyValue("null")
  protected Class<? extends ScoutTexts> getConfiguredNlsProvider() {
    return null;
  }

  @ConfigProperty(ConfigProperty.SQL_STYLE)
  @Order(80)
  @ConfigPropertyValue("null")
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return null;
  }

  /**
   * @deprecated use {@link #getConfiguredTransactionMemberId()} Will be removed in Release 3.10.
   */
  @Deprecated
  protected String getConfiguredXAResourceId() {
    return getConfiguredTransactionMemberId();
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(90)
  @ConfigPropertyValue("null")
  protected String getConfiguredTransactionMemberId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  @ConfigPropertyValue("\"oracle.jdbc.OracleDriver\"")
  protected String getConfiguredJdbcDriverName() {
    return "oracle.jdbc.OracleDriver";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  @ConfigPropertyValue("\"jdbc:oracle:thin:@localhost:1521:ORCL\"")
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:oracle:thin:@localhost:1521:ORCL";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(120)
  @ConfigPropertyValue("null")
  protected String getConfiguredJdbcProperties() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(130)
  @ConfigPropertyValue("25")
  protected int getConfiguredJdbcPoolSize() {
    return 25;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(140)
  @ConfigPropertyValue("300000L")
  protected long getConfiguredJdbcPoolConnectionLifetime() {
    return 300000L;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(150)
  @ConfigPropertyValue("21600000L")
  protected long getConfiguredJdbcPoolConnectionBusyTimeout() {
    return 21600000L;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  @ConfigPropertyValue("25")
  protected int getConfiguredJdbcStatementCacheSize() {
    return 25;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(170)
  @ConfigPropertyValue("null")
  protected String getConfiguredJndiName() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(180)
  @ConfigPropertyValue("null")
  protected String getConfiguredJndiInitialContextFactory() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(190)
  @ConfigPropertyValue("null")
  protected String getConfiguredJndiProviderUrl() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(200)
  @ConfigPropertyValue("null")
  protected String getConfiguredJndiUrlPkgPrefixes() {
    return null;
  }

  @ConfigOperation
  @Order(10)
  protected void execTestConnection(Connection conn) throws Throwable {
    ISqlStyle s = getSqlStyle();
    if (s != null) {
      s.testConnection(conn);
    }
  }

  @ConfigOperation
  @Order(15)
  protected void execAfterConnectionCreated(Connection conn) throws ProcessingException {
  }

  /**
   * called just after the transaction has started
   */
  @ConfigOperation
  @Order(20)
  protected void execBeginTransaction() throws ProcessingException {
  }

  @ConfigOperation
  @Order(30)
  protected Connection execCreateConnection() throws Throwable {
    return leaseConnectionInternal();
  }

  @ConfigOperation
  @Order(35)
  protected void execReleaseConnection(Connection conn) throws Throwable {
    releaseConnectionInternal(conn);
  }

  /**
   * Custom functions that can be used in sql statements as binds or sql style independent functions
   * <p>
   * Default functions are<br>
   * ::level(permissionClass) --> int //to resolve a permissin level by executing a permission<br>
   * ::level(permissionLevel) --> int //to resolve a permissin level by its id<br>
   * ::code(codeClass or codeTypeClass) --> the ID of the code or code type<br>
   * ::text(textId) --> the text in the user sessions language
   * <p>
   * Examples:<br>
   * ::level(UpdatePersonPermission)<br>
   * ::level(UpdatePersonPermission.LEVEL_OWN)<br>
   * <br>
   * ::code(CompanyAddressCodeType.MainAddressCode)<br>
   * ::code(MainAddressCode)<br>
   * <br>
   * ::text(SalutationMr)
   * <p>
   * 
   * @return a plain object value or in case of a null value preferrably a {@link IHolder} of the correct value type
   */
  @ConfigOperation
  @Order(40)
  protected Object execCustomBindFunction(String functionName, String[] args, Object[] bindBases) throws ProcessingException {
    if (functionName.equals("level")) {
      if (args.length != 1) {
        throw new IllegalArgumentException("expected 1 argument for function '" + functionName + "'");
      }
      String permissionClassName = args[0];
      String levelField = null;
      // eventually a level id?
      int levelDot = permissionClassName.indexOf(".LEVEL_");
      if (levelDot >= 0) {
        levelField = permissionClassName.substring(levelDot + 1);
        permissionClassName = permissionClassName.substring(0, levelDot);
      }
      Class permissionClass = loadBundleClassLenient(m_permissionNameToDescriptor, permissionClassName);
      IAccessControlService accessControlService = SERVICES.getService(IAccessControlService.class);
      Object ret = tryGetPermissionLevel(permissionClass, levelField, accessControlService);
      return ret != null ? ret : new LongHolder();
    }
    else if (functionName.equals("code")) {
      if (args.length != 1) {
        throw new IllegalArgumentException("expected 1 argument for function '" + functionName + "'");
      }
      String codeClassName = args[0];
      Class codeClass = loadBundleClassLenient(m_codeNameToDescriptor, codeClassName);
      if (codeClass == null) {
        throw new ProcessingException("cannot find class for code '" + args[0] + "'");
      }
      try {
        Object ret = codeClass.getField("ID").get(null);
        return ret != null ? ret : new LongHolder();
      }
      catch (Throwable t) {
        throw new ProcessingException("ID of code '" + args[0] + "'", t);
      }
    }
    else if (functionName.equals("text")) {
      if (args.length < 1) {
        throw new IllegalArgumentException("expected at least 1 argument for function '" + functionName + "'");
      }
      if (args.length == 1) {
        String[] tmp = new String[2];
        tmp[0] = args[0];
        tmp[1] = null;
        args = tmp;
      }
      try {
        Method m = getNlsProvider().getMethod("get", new Class[]{String.class, String[].class});
        Object ret = m.invoke(null, (Object[]) args);
        return ret != null ? ret : new StringHolder();
      }
      catch (Throwable t) {
        throw new ProcessingException("unknown function in DynamicNls, check 'getConfiguredNlsProvider' / 'getNlsProvider': get", t);
      }
    }
    else {
      throw new IllegalArgumentException("undefined function '" + functionName + "'");
    }
  }

  private Object tryGetPermissionLevel(Class permissionClass, String levelField, IAccessControlService accessControlService) throws ProcessingException {
    if (permissionClass == null) {
      return null;
    }
    try {
      if (levelField != null) {
        return permissionClass.getField(levelField).get(null);
      }
      else {
        Permission p = (Permission) permissionClass.newInstance();
        return accessControlService.getPermissionLevel(p);
      }
    }
    catch (Exception e) {
      throw new ProcessingException("getLevel of permission '" + permissionClass + "'", e);
    }
  }

  /**
   * Called just before the transaction is being committed or rollbacked.<br>
   * Do not call commit here, the flag is just meant as a hint.
   * Statements are executed, even if the transaction is canceled.
   */
  @ConfigOperation
  @Order(50)
  protected void execEndTransaction(boolean willBeCommitted) throws ProcessingException {
  }

  protected void initConfig() {
    String tid = getConfiguredTransactionMemberId();
    if (tid == null) {
      tid = getClass().getSimpleName() + "." + "transaction";
    }
    setTransactionMemberId(tid);
    setDirectJdbcConnection(getConfiguredDirectJdbcConnection());
    setUsername(getConfiguredUsername());
    setPassword(getConfiguredPassword());
    setJndiName(getConfiguredJndiName());
    setJndiInitialContextFactory(getConfiguredJndiInitialContextFactory());
    setJndiProviderUrl(getConfiguredJndiProviderUrl());
    setJndiUrlPkgPrefixes(getConfiguredJndiUrlPkgPrefixes());
    setJdbcMappingName(getConfiguredJdbcMappingName());
    setJdbcDriverName(getConfiguredJdbcDriverName());
    setJdbcProperties(getConfiguredJdbcProperties());
    setJdbcStatementCacheSize(getConfiguredJdbcStatementCacheSize());
    setJdbcPoolSize(getConfiguredJdbcPoolSize());
    setJdbcPoolConnectionBusyTimeout(getConfiguredJdbcPoolConnectionBusyTimeout());
    setJdbcPoolConnectionLifetime(getConfiguredJdbcPoolConnectionLifetime());
    setNlsProvider(getConfiguredNlsProvider());
    // sql style
    Class<? extends ISqlStyle> styleClass = getConfiguredSqlStyle();
    if (styleClass != null) {
      try {
        setSqlStyle(styleClass.newInstance());
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    else {
      setSqlStyle(new OracleSqlStyle());
    }
  }

  /*
   * Runtime
   */
  public void callbackAfterConnectionCreated(Connection conn) throws ProcessingException {
    execAfterConnectionCreated(conn);
  }

  public void callbackTestConnection(Connection conn) throws Throwable {
    execTestConnection(conn);
  }

  public Object callbackCustomBindFunction(String functionName, String[] args, Object[] bindBases) throws ProcessingException {
    return execCustomBindFunction(functionName, args, bindBases);
  }

  @Override
  public String getTransactionMemberId() {
    return m_transactionMemberId;
  }

  public boolean isDirectJdbcConnection() {
    return m_directJdbcConnection;
  }

  public int getJdbcStatementCacheSize() {
    return m_queryCacheSize;
  }

  public String getJndiName() {
    return m_jndiName;
  }

  public String getJndiInitialContextFactory() {
    return m_jndiInitialContextFactory;
  }

  public String getJndiProviderUrl() {
    return m_jndiProviderUrl;
  }

  public String getJndiUrlPkgPrefixes() {
    return m_jndiUrlPkgPrefixes;
  }

  public String getJdbcMappingName() {
    return m_jdbcMappingName;
  }

  public String getJdbcDriverName() {
    return m_jdbcDriverName;
  }

  public String getJdbcProperties() {
    return m_jdbcProps;
  }

  public String getUsername() {
    return m_defaultUser;
  }

  public String getPassword() {
    return m_defaultPass;
  }

  public int getJdbcPoolSize() {
    return m_jdbcPoolSize;
  }

  public long getJdbcPoolConnectionLifetime() {
    return m_jdbcPoolConnectionLifetime;
  }

  public long getJdbcPoolConnectionBusyTimeout() {
    return m_jdbcPoolConnectionBusyTimeout;
  }

  public int getMaxFetchMemorySize() {
    return m_maxFetchMemorySize;
  }

  public void setTransactionMemberId(String s) {
    m_transactionMemberId = s;
  }

  public void setDirectJdbcConnection(boolean b) {
    m_directJdbcConnection = b;
  }

  public void setJdbcStatementCacheSize(int n) {
    m_queryCacheSize = n;
  }

  public void setJndiName(String s) {
    m_jndiName = s;
  }

  public void setJndiInitialContextFactory(String s) {
    m_jndiInitialContextFactory = s;
  }

  public void setJndiProviderUrl(String s) {
    m_jndiProviderUrl = s;
  }

  public void setJndiUrlPkgPrefixes(String s) {
    m_jndiUrlPkgPrefixes = s;
  }

  /**
   * Supports ${...} variables resolved by {@link BundleContextUtility#resolve(String)}
   */
  public void setJdbcMappingName(String s) {
    m_jdbcMappingName = BundleContextUtility.resolve(s);
  }

  public void setJdbcDriverName(String s) {
    m_jdbcDriverName = s;
  }

  public void setJdbcProperties(String s) {
    m_jdbcProps = s;
  }

  public void setUsername(String s) {
    m_defaultUser = s;
  }

  public void setPassword(String s) {
    m_defaultPass = s;
  }

  public void setJdbcPoolSize(int n) {
    m_jdbcPoolSize = n;
  }

  public void setJdbcPoolConnectionLifetime(long t) {
    m_jdbcPoolConnectionLifetime = t;
  }

  public void setJdbcPoolConnectionBusyTimeout(long t) {
    m_jdbcPoolConnectionBusyTimeout = t;
  }

  public void setMaxFetchMemorySize(int maxFetchMemorySize) {
    m_maxFetchMemorySize = maxFetchMemorySize;
  }

  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IServiceInventory.class) {
      if (m_pool != null) {
        return m_pool.getInventory();
      }
    }
    return null;
  }

  public Class<? extends ScoutTexts> getNlsProvider() {
    return m_nlsProvider;
  }

  public void setNlsProvider(Class<? extends ScoutTexts> nlsProvider) {
    m_nlsProvider = nlsProvider;
  }

  @Override
  public ISqlStyle getSqlStyle() {
    return m_sqlStyle;
  }

  public void setSqlStyle(ISqlStyle sqlStyle) {
    m_sqlStyle = sqlStyle;
  }

  /*
   * Internals
   */
  private Connection leaseConnection() throws Throwable {
    Connection conn = execCreateConnection();
    return conn;
  }

  private Connection leaseConnectionInternal() throws Throwable {
    try {
      if (isDirectJdbcConnection()) {
        // get connection from internal pool
        Connection conn = getSqlConnectionPool().leaseConnection(this);
        return conn;
      }
      else {
        Connection conn = new SqlConnectionBuilder().createJndiConnection(this);
        // do not call execAfterConnectionCreated(conn) because jndi connections
        // are normally pooled
        return conn;
      }
    }
    catch (Exception e) {
      ProcessingException pe;
      if (e instanceof ProcessingException) {
        pe = (ProcessingException) e;
      }
      else {
        pe = new ProcessingException("unexpected exception", e);
      }
      if (isDirectJdbcConnection()) {
        pe.addContextMessage("jdbcDriverName=" + getJdbcDriverName() + ", jdbcMappingName=" + getJdbcMappingName());
      }
      else {
        pe.addContextMessage("jndiName=" + getJndiName());
      }
      throw pe;
    }
  }

  private void releaseConnection(Connection conn) {
    try {
      execReleaseConnection(conn);
    }
    catch (Throwable e) {
      LOG.error(null, e);
    }
  }

  private void releaseConnectionInternal(Connection conn) throws Throwable {
    if (isDirectJdbcConnection()) {
      // delegate to internal pool
      getSqlConnectionPool().releaseConnection(conn);
    }
    else {
      conn.close();
    }
  }

  private synchronized SqlConnectionPool getSqlConnectionPool() {
    if (m_pool == null) {
      m_pool = SqlConnectionPool.getPool(getClass(), getJdbcPoolSize(), getJdbcPoolConnectionLifetime(), getJdbcPoolConnectionBusyTimeout());
    }
    return m_pool;
  }

  @Override
  public Connection getConnection() throws ProcessingException {
    return getTransaction();
  }

  protected Connection getTransaction() throws ProcessingException {
    ITransaction reg = ThreadContext.getTransaction();
    if (reg == null) {
      throw new ProcessingException("no ITransaction available, use ServerJob to run truncactions");
    }
    SqlTransactionMember member = (SqlTransactionMember) reg.getMember(getTransactionMemberId());
    if (member == null) {
      Connection conn;
      try {
        conn = leaseConnection();
        member = new SqlTransactionMember(getTransactionMemberId(), conn);
        reg.registerMember(member);
        // this is the start of the transaction
        execBeginTransaction();
      }
      catch (ProcessingException e) {
        throw e;
      }
      catch (Throwable e) {
        throw new ProcessingException("getTransaction", e);
      }
    }
    return member.getConnection();
  }

  /**
   * @return the statement cache used for this {@link ITransaction} transaction
   */
  protected final IStatementCache getStatementCache() throws ProcessingException {
    ITransaction reg = ThreadContext.getTransaction();
    if (reg == null) {
      throw new ProcessingException("no ITransaction available, use ServerJob to run truncactions");
    }
    IStatementCache res = (IStatementCache) reg.getMember(PreparedStatementCache.TRANSACTION_MEMBER_ID);
    if (res == null) {
      res = new PreparedStatementCache(getJdbcStatementCacheSize());
      reg.registerMember((ITransactionMember) res);
    }
    return res;
  }

  /*
   * Operations
   */

  @Override
  public Object[][] select(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).processSelect(getTransaction(), getStatementCache(), null);
  }

  @Override
  public Object[][] selectLimited(String s, int maxRowCount, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, maxRowCount).processSelect(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectInto(String s, Object... bindBases) throws ProcessingException {
    createStatementProcessor(s, bindBases, 0).processSelectInto(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectIntoLimited(String s, int maxRowCount, Object... bindBases) throws ProcessingException {
    createStatementProcessor(s, bindBases, maxRowCount).processSelectInto(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectStreaming(String s, ISelectStreamHandler handler, Object... bindBases) throws ProcessingException {
    createStatementProcessor(s, bindBases, 0).processSelectStreaming(getTransaction(), getStatementCache(), handler);
  }

  @Override
  public void selectStreamingLimited(String s, ISelectStreamHandler handler, int maxRowCount, Object... bindBases) throws ProcessingException {
    createStatementProcessor(s, bindBases, maxRowCount).processSelectStreaming(getTransaction(), getStatementCache(), handler);
  }

  @Override
  public int insert(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public int update(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public int delete(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public boolean callStoredProcedure(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).processStoredProcedure(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void commit() throws ProcessingException {
    try {
      getTransaction().commit();
    }
    catch (SQLException e) {
      throw new ProcessingException("unexpected exception", e);
    }
  }

  @Override
  public String createPlainText(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).createPlainText();
  }

  @Override
  public WhereToken resolveSpecialConstraint(Object specialConstraint) throws ProcessingException {
    if (specialConstraint instanceof LegacySearchFilter.StringLikeConstraint) {
      LegacySearchFilter.StringLikeConstraint c = (LegacySearchFilter.StringLikeConstraint) specialConstraint;
      String s = getSqlStyle().toLikePattern(c.getValue());
      return new LegacySearchFilter.WhereToken(c.getTerm(), s, null);
    }
    else if (specialConstraint instanceof LegacySearchFilter.ComposerConstraint) {
      LegacySearchFilter.ComposerConstraint c = (LegacySearchFilter.ComposerConstraint) specialConstraint;
      HashMap<String, Object> map = new HashMap<String, Object>();
      String term = c.getTerm();
      for (Map.Entry<String, LegacySearchFilter.ComposerAttributeRef> e : c.getAttributeRefMap().entrySet()) {
        String key = e.getKey();
        LegacySearchFilter.ComposerAttributeRef att = e.getValue();
        LegacyStatementBuilder b = new LegacyStatementBuilder(getSqlStyle());
        String attValue = b.resolveComposerAttribute(att.getOp(), att.getAttribute(), att.getBindName(), att.getValue());
        map.putAll(b.getBindMap());
        term = term.replace(key, attValue);
      }
      return new LegacySearchFilter.WhereToken(term, null, map);
    }
    return null;
  }

  protected IStatementProcessor createStatementProcessor(String s, Object[] bindBases, int maxRowCount) throws ProcessingException {
    return new StatementProcessor(this, s, bindBases, maxRowCount, m_maxFetchMemorySize);
  }

  /**
   * When the service completes work with an exception, a xa rollback is done on
   * ALL used service request resources
   * 
   * @see commit
   */
  @Override
  public void rollback() throws ProcessingException {
    try {
      getTransaction().rollback();
    }
    catch (SQLException e) {
      throw new ProcessingException("unexpected exception", e);
    }
  }

  @Override
  public Long getSequenceNextval(String sequenceName) throws ProcessingException {
    String s = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL ";
    Object[][] ret = createStatementProcessor(s, null, 0).processSelect(getTransaction(), getStatementCache(), null);
    if (ret.length == 1) {
      return NumberUtility.toLong(NumberUtility.nvl((Number) ret[0][0], 0));
    }
    return 0L;
  }

  /**
   * @return the class loaded by the first bundle with a matching symbolic name
   *         Example: name "com.myapp.shared.core.security.ReadDataPermission" is loaded by the bundle with symbolic
   *         name "com.myapp.shared.core".
   */
  private Class loadBundleClassLenient(Map<String, List<BundleClassDescriptor>> map, String name) {
    String base = name;
    String suffix = "";
    while (base.length() > 0) {
      List<BundleClassDescriptor> list = map.get(base);
      if (list != null) {
        for (BundleClassDescriptor desc : list) {
          try {
            Class c = Platform.getBundle(desc.getBundleSymbolicName()).loadClass(desc.getClassName());
            if (suffix.length() > 0) {
              c = Platform.getBundle(desc.getBundleSymbolicName()).loadClass(desc.getClassName() + suffix.replace(".", "$"));
              return c;
            }
            else {
              return c;
            }
          }
          catch (Throwable t) {
            LOG.warn("Could not load class with lenient name '" + name + "' in bundle " + desc.getBundleSymbolicName());
          }
        }
      }
      //
      int i = base.lastIndexOf('.');
      if (i >= 0) {
        String seg = base.substring(i);
        base = base.substring(0, i);
        suffix = seg + suffix;
      }
      else {
        String seg = base;
        base = "";
        suffix = seg + suffix;
      }
    }
    LOG.warn("Could not find class with lenient name '" + name + "'");
    return null;
  }

  private class SqlTransactionMember extends AbstractSqlTransactionMember {
    private final Connection m_conn;

    public SqlTransactionMember(String transactionMemberId, Connection conn) {
      super(transactionMemberId);
      m_conn = conn;
    }

    public Connection getConnection() {
      return m_conn;
    }

    @Override
    public void commitPhase2() {
      try {
        // this is the end of the transaction
        try {
          setFinishingTransaction(true);
          execEndTransaction(false);
        }
        finally {
          setFinishingTransaction(false);
        }
        m_conn.commit();
      }
      catch (Exception e) {
        LOG.error(null, e);
      }
    }

    @Override
    public void rollback() {
      try {
        // this is the end of the transaction
        try {
          setFinishingTransaction(true);
          execEndTransaction(false);
        }
        finally {
          setFinishingTransaction(false);
        }
        m_conn.rollback();
      }
      catch (Exception e) {
        if (!ThreadContext.getTransaction().isCancelled()) {
          LOG.error(null, e);
        }
      }
    }

    @Override
    public void release() {
      releaseConnection(m_conn);
    }
  }// end private class

}
