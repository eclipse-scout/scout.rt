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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.IHolder;
import org.eclipse.scout.commons.holders.LongHolder;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.platform.service.IServiceInventory;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlDirectJdbcConnectionProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcDriverNameProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcMappingNameProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcPoolConnectionBusyTimeoutProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcPoolConnectionLifetimeProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcPoolSizeProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcPropertiesProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJdbcStatementCacheSizeProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJndiInitialContextFactoryProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJndiNameProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJndiProviderUrlProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlJndiUrlPkgPrefixesProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlPasswordProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlTransactionMemberIdProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.SqlUsernameProperty;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.exec.PreparedStatementCache;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.exec.StatementProcessor;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.pool.SqlConnectionBuilder;
import org.eclipse.scout.rt.server.services.common.jdbc.internal.pool.SqlConnectionPool;
import org.eclipse.scout.rt.server.services.common.jdbc.oracle.OracleSqlStyle;
import org.eclipse.scout.rt.server.services.common.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;

public abstract class AbstractSqlService extends AbstractService implements ISqlService, IServiceInventory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractSqlService.class);
  public static final int DEFAULT_MEMORY_PREFETCH_SIZE = 1024 * 1024; // = 1MB default

  private SqlConnectionPool m_pool;
  private final String m_transactionMemberId;
  private final boolean m_directJdbcConnection;
  private final String m_jndiName;
  private final String m_jndiInitialContextFactory;
  private final String m_jndiProviderUrl;
  private final String m_jndiUrlPkgPrefixes;
  private final String m_jdbcMappingName;
  private final String m_jdbcDriverName;
  private final String m_jdbcProps;
  private final int m_jdbcPoolSize;
  private final long m_jdbcPoolConnectionLifetime;
  private final long m_jdbcPoolConnectionBusyTimeout;
  private final String m_defaultUser;
  private final String m_defaultPass;
  private final int m_queryCacheSize;
  private final int m_maxFetchMemorySize = DEFAULT_MEMORY_PREFETCH_SIZE;
  private final Class<? extends ScoutTexts> m_nlsProvider;
  private final ISqlStyle m_sqlStyle;

  private final Map<String, List<Class<?>>> m_permissionNameToDescriptor;
  private final Map<String, List<Class<?>>> m_codeNameToDescriptor;

  public AbstractSqlService() {
    // load config
    String tid = getConfiguredTransactionMemberId();
    if (tid == null) {
      tid = getClass().getSimpleName() + "." + "transaction";
    }
    m_transactionMemberId = getPropertyValue(SqlTransactionMemberIdProperty.class, tid);
    m_directJdbcConnection = getPropertyValue(SqlDirectJdbcConnectionProperty.class, getConfiguredDirectJdbcConnection());
    m_defaultUser = getPropertyValue(SqlUsernameProperty.class, getConfiguredUsername());
    m_defaultPass = getPropertyValue(SqlPasswordProperty.class, getConfiguredPassword());
    m_jndiName = getPropertyValue(SqlJndiNameProperty.class, getConfiguredJndiName());
    m_jndiInitialContextFactory = getPropertyValue(SqlJndiInitialContextFactoryProperty.class, getConfiguredJndiInitialContextFactory());
    m_jndiProviderUrl = getPropertyValue(SqlJndiProviderUrlProperty.class, getConfiguredJndiProviderUrl());
    m_jndiUrlPkgPrefixes = getPropertyValue(SqlJndiUrlPkgPrefixesProperty.class, getConfiguredJndiUrlPkgPrefixes());
    m_jdbcMappingName = getPropertyValue(SqlJdbcMappingNameProperty.class, getConfiguredJdbcMappingName());
    m_jdbcDriverName = getPropertyValue(SqlJdbcDriverNameProperty.class, getConfiguredJdbcDriverName());
    m_jdbcProps = getPropertyValue(SqlJdbcPropertiesProperty.class, getConfiguredJdbcProperties());
    m_queryCacheSize = getPropertyValue(SqlJdbcStatementCacheSizeProperty.class, getConfiguredJdbcStatementCacheSize());
    m_jdbcPoolSize = getPropertyValue(SqlJdbcPoolSizeProperty.class, getConfiguredJdbcPoolSize());
    m_jdbcPoolConnectionBusyTimeout = getPropertyValue(SqlJdbcPoolConnectionBusyTimeoutProperty.class, getConfiguredJdbcPoolConnectionBusyTimeout());
    m_jdbcPoolConnectionLifetime = getPropertyValue(SqlJdbcPoolConnectionLifetimeProperty.class, getConfiguredJdbcPoolConnectionLifetime());
    m_nlsProvider = getConfiguredNlsProvider();

    // load sql style
    Class<? extends ISqlStyle> styleClass = getConfiguredSqlStyle();
    ISqlStyle style = null;
    if (styleClass != null) {
      try {
        style = styleClass.newInstance();
      }
      catch (Exception e) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("error creating instance of class '" + styleClass.getName() + "'.", e));
      }
    }
    else {
      style = new OracleSqlStyle();
    }
    m_sqlStyle = style;

    // load code and permission names
    m_permissionNameToDescriptor = new HashMap<>();
    IPermissionService psvc = BEANS.opt(IPermissionService.class);
    if (psvc != null) {
      for (Class<? extends Permission> d : psvc.getAllPermissionClasses()) {
        List<Class<?>> list = m_permissionNameToDescriptor.get(d.getSimpleName());
        if (list == null) {
          list = new ArrayList<>();
          m_permissionNameToDescriptor.put(d.getSimpleName(), list);
        }
        list.add(d);

        list = m_permissionNameToDescriptor.get(d.getName());
        if (list == null) {
          list = new ArrayList<>();
          m_permissionNameToDescriptor.put(d.getName(), list);
        }
        list.add(d);
      }
    }

    m_codeNameToDescriptor = new HashMap<>();
    ICodeService csvc = BEANS.opt(ICodeService.class);
    if (csvc != null) {
      for (Class<?> d : csvc.getAllCodeTypeClasses("")) {
        List<Class<?>> list = m_codeNameToDescriptor.get(d.getSimpleName());
        if (list == null) {
          list = new ArrayList<>();
          m_codeNameToDescriptor.put(d.getSimpleName(), list);
        }
        list.add(d);

        list = m_codeNameToDescriptor.get(d.getName());
        if (list == null) {
          list = new ArrayList<>();
          m_codeNameToDescriptor.put(d.getName(), list);
        }
        list.add(d);
      }
    }
  }

  protected <DATA_TYPE> DATA_TYPE getPropertyValue(Class<? extends IConfigProperty<DATA_TYPE>> clazz, DATA_TYPE defaultValue) {
    DATA_TYPE value = CONFIG.getPropertyValue(clazz);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredDirectJdbcConnection() {
    return true;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(20)
  protected String getConfiguredUsername() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(30)
  protected String getConfiguredPassword() {
    return null;
  }

  @ConfigProperty(ConfigProperty.NLS_PROVIDER)
  @Order(70)
  protected Class<? extends ScoutTexts> getConfiguredNlsProvider() {
    return null;
  }

  @ConfigProperty(ConfigProperty.SQL_STYLE)
  @Order(80)
  protected Class<? extends ISqlStyle> getConfiguredSqlStyle() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(90)
  protected String getConfiguredTransactionMemberId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(100)
  protected String getConfiguredJdbcDriverName() {
    return "oracle.jdbc.OracleDriver";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  protected String getConfiguredJdbcMappingName() {
    return "jdbc:oracle:thin:@localhost:1521:ORCL";
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(120)
  protected String getConfiguredJdbcProperties() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(130)
  protected int getConfiguredJdbcPoolSize() {
    return 25;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(140)
  protected long getConfiguredJdbcPoolConnectionLifetime() {
    return 300000L;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(150)
  protected long getConfiguredJdbcPoolConnectionBusyTimeout() {
    return 21600000L;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  protected int getConfiguredJdbcStatementCacheSize() {
    return 25;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(170)
  protected String getConfiguredJndiName() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(180)
  protected String getConfiguredJndiInitialContextFactory() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(190)
  protected String getConfiguredJndiProviderUrl() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(200)
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
    if ("level".equals(functionName)) {
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
      IAccessControlService accessControlService = BEANS.get(IAccessControlService.class);
      Object ret = tryGetPermissionLevel(permissionClass, levelField, accessControlService);
      return ret != null ? ret : new LongHolder();
    }
    else if ("code".equals(functionName)) {
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
    else if ("text".equals(functionName)) {
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
      throw new ProcessingException("getLevel of permission '" + permissionClass.getName() + "'.", e);
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

  @Override
  public String getInventory() {
    if (m_pool != null) {
      return m_pool.getInventory();
    }
    return null;
  }

  public Class<? extends ScoutTexts> getNlsProvider() {
    return m_nlsProvider;
  }

  @Override
  public ISqlStyle getSqlStyle() {
    return m_sqlStyle;
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
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");

    SqlTransactionMember member = (SqlTransactionMember) tx.getMember(getTransactionMemberId());
    if (member == null) {
      try {
        Connection connection = leaseConnection();
        member = new SqlTransactionMember(getTransactionMemberId(), connection);
        tx.registerMember(member);
        // this is the start of the transaction
        execBeginTransaction();
      }
      catch (ProcessingException e) {
        throw e;
      }
      catch (Throwable e) {
        throw new ProcessingException("Failed to get SQL connection", e);
      }
    }
    return member.getConnection();
  }

  /**
   * @return the statement cache used for this {@link ITransaction} transaction
   */
  protected final IStatementCache getStatementCache() throws ProcessingException {
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");
    IStatementCache res = (IStatementCache) tx.getMember(PreparedStatementCache.TRANSACTION_MEMBER_ID);
    if (res == null) {
      res = new PreparedStatementCache(getJdbcStatementCacheSize());
      tx.registerMember((ITransactionMember) res);
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
      ISqlStyle style = getSqlStyle();
      if (style != null) {
        style.commit();
      }
    }
    catch (SQLException e) {
      throw new ProcessingException("unexpected exception", e);
    }
  }

  @Override
  public String createPlainText(String s, Object... bindBases) throws ProcessingException {
    return createStatementProcessor(s, bindBases, 0).createPlainText();
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
      ISqlStyle style = getSqlStyle();
      if (style != null) {
        style.rollback();
      }
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
  private Class loadBundleClassLenient(Map<String, List<Class<?>>> map, String name) {
    String base = name;
    String suffix = "";
    ClassLoader classLoader = getClass().getClassLoader();
    while (base.length() > 0) {
      List<Class<?>> list = map.get(base);
      if (list != null) {
        for (Class<?> desc : list) {
          try {
            Class c = classLoader.loadClass(desc.getName());
            if (suffix.length() > 0) {
              c = classLoader.loadClass(desc.getName() + suffix.replace('.', '$'));
              return c;
            }
            else {
              return c;
            }
          }
          catch (Throwable t) {
            LOG.warn("Could not load class with lenient name '" + name + "'.");
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
        LOG.error("Failed to commit transaction", e);
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
        if (!ITransaction.CURRENT.get().isCancelled()) {
          LOG.error("Failed to rollback transaction", e);
        }
      }
    }

    @Override
    public void release() {
      releaseConnection(m_conn);
    }
  }// end private class

}
