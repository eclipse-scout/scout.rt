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
package org.eclipse.scout.rt.server.jdbc;

import java.lang.reflect.Method;
import java.security.Permission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigProperty;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.IHolder;
import org.eclipse.scout.rt.platform.holders.LongHolder;
import org.eclipse.scout.rt.platform.holders.StringHolder;
import org.eclipse.scout.rt.platform.service.IServiceInventory;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlDirectJdbcConnectionProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcDriverNameProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcDriverUnloadProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcMappingNameProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcPoolConnectionBusyTimeoutProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcPoolConnectionLifetimeProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcPoolSizeProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcPropertiesProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJdbcStatementCacheSizeProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJndiInitialContextFactoryProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJndiNameProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJndiProviderUrlProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlJndiUrlPkgPrefixesProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlPasswordProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlTransactionMemberIdProperty;
import org.eclipse.scout.rt.server.jdbc.SqlConfigProperties.SqlUsernameProperty;
import org.eclipse.scout.rt.server.jdbc.internal.exec.PreparedStatementCache;
import org.eclipse.scout.rt.server.jdbc.internal.exec.StatementProcessor;
import org.eclipse.scout.rt.server.jdbc.internal.pool.SqlConnectionBuilder;
import org.eclipse.scout.rt.server.jdbc.internal.pool.SqlConnectionPool;
import org.eclipse.scout.rt.server.jdbc.oracle.OracleSqlStyle;
import org.eclipse.scout.rt.server.jdbc.style.ISqlStyle;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.code.ICodeService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSqlService implements ISqlService, IServiceInventory {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractSqlService.class);
  public static final int DEFAULT_MEMORY_PREFETCH_SIZE = 1024 * 1024; // = 1MB default

  private volatile SqlConnectionPool m_pool;
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

  private volatile boolean m_destroyed;

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
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Failed to create instance of class '{}'.", new Object[]{styleClass.getName(), e}));
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
      for (Class<?> d : csvc.getAllCodeTypeClasses()) {
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
  protected void execAfterConnectionCreated(Connection conn) {
  }

  /**
   * called just after the transaction has started
   */
  @ConfigOperation
  @Order(20)
  protected void execBeginTransaction() {
  }

  @ConfigOperation
  @Order(30)
  protected Connection execCreateConnection() {
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
  protected Object execCustomBindFunction(String functionName, String[] args, Object[] bindBases) {
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
        throw new ProcessingException("Cannot find class for code '{}", new Object[]{args[0]});
      }
      try {
        Object ret = codeClass.getField("ID").get(null);
        return ret != null ? ret : new LongHolder();
      }
      catch (Exception t) {
        throw new ProcessingException("ID of code '{}'", new Object[]{args[0], t});
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
      catch (Exception t) {
        throw new ProcessingException("unknown function in DynamicNls, check 'getConfiguredNlsProvider' / 'getNlsProvider': get", t);
      }
    }
    else {
      throw new IllegalArgumentException("undefined function '" + functionName + "'");
    }
  }

  private Object tryGetPermissionLevel(Class permissionClass, String levelField, IAccessControlService accessControlService) {
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
      throw new ProcessingException("getLevel of permission '{}'.", new Object[]{permissionClass.getName(), e});
    }
  }

  /**
   * Called just before the transaction is being committed or rollbacked.<br>
   * Do not call commit here, the flag is just meant as a hint. Statements are executed, even if the transaction is
   * canceled.
   */
  @ConfigOperation
  @Order(50)
  protected void execEndTransaction(boolean willBeCommitted) {
  }

  /*
   * Runtime
   */
  public void callbackAfterConnectionCreated(Connection conn) {
    execAfterConnectionCreated(conn);
  }

  public void callbackTestConnection(Connection conn) throws Throwable {
    execTestConnection(conn);
  }

  public Object callbackCustomBindFunction(String functionName, String[] args, Object[] bindBases) {
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
    final SqlConnectionPool pool = m_pool;
    if (pool != null) {
      return pool.getInventory();
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
  private Connection leaseConnection() {
    Connection conn = execCreateConnection();
    return conn;
  }

  private Connection leaseConnectionInternal() {
    try {
      if (isDirectJdbcConnection()) {
        // get connection from internal pool
        return getSqlConnectionPool().leaseConnection(this);
      }
      else {
        // do not call execAfterConnectionCreated(conn) because jndi connections
        // are normally pooled
        return new SqlConnectionBuilder().createJndiConnection(this);
      }
    }
    catch (Exception e) {
      PlatformException pe = (e instanceof PlatformException ? (PlatformException) e : new PlatformException("Failed to lease connection", e));

      if (isDirectJdbcConnection()) {
        pe.withContextInfo("jdbcDriverName", getJdbcDriverName())
            .withContextInfo("jdbcMappingName", getJdbcMappingName());
      }
      else {
        pe.withContextInfo("jndiName", getJndiName());
      }
      throw pe;
    }
  }

  private void releaseConnection(Connection conn) {
    try {
      execReleaseConnection(conn);
    }
    catch (Throwable e) {
      LOG.error("Could not release connection", e);
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
    Assertions.assertFalse(isDestroyed(), "{} not available because the platform has been shut down.", getClass().getSimpleName());
    if (m_pool == null) {
      m_pool = BEANS.get(SqlConnectionPool.class);
      m_pool.initialize(getClass().getName(), getJdbcPoolSize(), getJdbcPoolConnectionLifetime(), getJdbcPoolConnectionBusyTimeout());
    }
    return m_pool;
  }

  /**
   * Destroys the current connection pool (created lazy upon releasing conneciton)
   */
  public synchronized void destroySqlConnectionPool() {
    if (m_pool != null) {
      m_pool.destroy();
      m_pool = null;
    }
  }

  @Override
  public Connection getConnection() {
    return getTransaction();
  }

  protected Connection getTransaction() {
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");

    SqlTransactionMember member = (SqlTransactionMember) tx.getMember(getTransactionMemberId());
    if (member == null) {
      @SuppressWarnings("resource")
      Connection connection = leaseConnection();
      member = new SqlTransactionMember(getTransactionMemberId(), connection);
      tx.registerMember(member);
      // this is the start of the transaction
      execBeginTransaction();
    }
    return member.getConnection();
  }

  /**
   * @return the statement cache used for this {@link ITransaction} transaction
   */
  protected final IStatementCache getStatementCache() {
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
  public Object[][] select(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processSelect(getTransaction(), getStatementCache(), null);
  }

  @Override
  public Object[][] selectLimited(String s, int maxRowCount, Object... bindBases) {
    return createStatementProcessor(s, bindBases, maxRowCount).processSelect(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectInto(String s, Object... bindBases) {
    createStatementProcessor(s, bindBases, 0).processSelectInto(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectIntoLimited(String s, int maxRowCount, Object... bindBases) {
    createStatementProcessor(s, bindBases, maxRowCount).processSelectInto(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void selectStreaming(String s, ISelectStreamHandler handler, Object... bindBases) {
    createStatementProcessor(s, bindBases, 0).processSelectStreaming(getTransaction(), getStatementCache(), handler);
  }

  @Override
  public void selectStreamingLimited(String s, ISelectStreamHandler handler, int maxRowCount, Object... bindBases) {
    createStatementProcessor(s, bindBases, maxRowCount).processSelectStreaming(getTransaction(), getStatementCache(), handler);
  }

  @Override
  public int insert(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public int update(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public int delete(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processModification(getTransaction(), getStatementCache(), null);
  }

  @Override
  public boolean callStoredProcedure(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).processStoredProcedure(getTransaction(), getStatementCache(), null);
  }

  @Override
  public void commit() {
    try {
      getTransaction().commit();
      ISqlStyle style = getSqlStyle();
      if (style != null) {
        style.commit();
      }
    }
    catch (SQLException e) {
      throw new ProcessingException("Failed to commit", e);
    }
  }

  @Override
  public String createPlainText(String s, Object... bindBases) {
    return createStatementProcessor(s, bindBases, 0).createPlainText();
  }

  protected IStatementProcessor createStatementProcessor(String s, Object[] bindBases, int maxRowCount) {
    return new StatementProcessor(this, s, bindBases, maxRowCount, m_maxFetchMemorySize);
  }

  /**
   * When the service completes work with an exception, a xa rollback is done on ALL used service request resources
   *
   * @see AbstractSqlService#commit()
   */
  @Override
  public void rollback() {
    try {
      getTransaction().rollback();
      ISqlStyle style = getSqlStyle();
      if (style != null) {
        style.rollback();
      }
    }
    catch (SQLException e) {
      throw new ProcessingException("Failed to rollback", e);
    }
  }

  @Override
  public Long getSequenceNextval(String sequenceName) {
    String s = getSequenceNextvalStatement(sequenceName);
    Object[][] ret = createStatementProcessor(s, null, 0).processSelect(getTransaction(), getStatementCache(), null);
    if (ret.length == 1) {
      return NumberUtility.toLong(NumberUtility.nvl((Number) ret[0][0], 0));
    }
    return 0L;
  }

  protected String getSequenceNextvalStatement(String sequenceName) {
    return "SELECT " + sequenceName + ".NEXTVAL FROM DUAL ";
  }

  /**
   * @return the class loaded by the first bundle with a matching symbolic name Example: name
   *         "com.myapp.shared.core.security.ReadDataPermission" is loaded by the bundle with symbolic name
   *         "com.myapp.shared.core".
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
          catch (ClassNotFoundException t) { // NOSONAR
            LOG.warn("Could not load class with lenient name '{}'", new Object[]{name});
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
    LOG.warn("Could not find class with lenient name '{}'", new Object[]{name});
    return null;
  }

  /**
   * Returns whether this SQL service was destroyed, and cannot be used anymore.
   */
  public boolean isDestroyed() {
    return m_destroyed;
  }

  /**
   * Method invoked once the platform is shutting down.
   */
  protected void destroy() {
    if (isDestroyed()) {
      return;
    }

    synchronized (this) {
      if (isDestroyed()) {
        return; // double-checked locking
      }
      m_destroyed = true;
    }

    if (isDirectJdbcConnection()) {
      destroySqlConnectionPool();

      // Destroy JDBC driver
      if (CONFIG.getPropertyValue(SqlJdbcDriverUnloadProperty.class)) {
        try {
          DriverManager.deregisterDriver(DriverManager.getDriver(getJdbcMappingName()));
        }
        catch (final SQLException e) {
          LOG.warn("Failed to deregister JDBC driver [driver={}, url={}]", getJdbcDriverName(), getJdbcMappingName(), e);
        }
      }
    }
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
  } // end private class

  /**
   * {@link IPlatformListener} to shutdown this SQL service upon platform shutdown.
   */
  @Order(ISqlService.DESTROY_ORDER)
  public static class PlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (State.PlatformStopping.equals(event.getState())) {
        for (final AbstractSqlService sqlService : BEANS.all(AbstractSqlService.class)) {
          sqlService.destroy();
        }
      }
    }
  }
}
