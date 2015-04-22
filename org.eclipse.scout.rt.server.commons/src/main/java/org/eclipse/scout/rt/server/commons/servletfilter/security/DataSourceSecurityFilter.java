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
package org.eclipse.scout.rt.server.commons.servletfilter.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.SecurityUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJdbcDriverNameProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJdbcMappingNameProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJdbcPasswordProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJdbcSelectUserPassProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJdbcUsernameProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJndiInitialContextFactoryProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJndiNameProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJndiProviderUrlProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterJndiUrlPkgPrefixesProperty;
import org.eclipse.scout.rt.server.commons.config.ServerCommonsConfigProperties.DataSourceFilterUseJndiProperty;

/**
 * <h4>DataSourceSecurityFilter</h4> The following properties can be set in the <code>web.xml</code> file:
 * <ul>
 * <li><code>realm=abcde</code> <b>default "Default"</b></li>
 * <li><code>failover=true/false</code> <b>default false</b></li>
 * <li>
 * <code>jdbcDriverName=[e.g. oracle.jdbc.OracleDriver]</code> <b>required for JDBC</b></li>
 * <li>
 * <code>jdbcMappingName=[e.g. jdbc:oracle:thin:@dbUrl:1521:DBNAME]</code> <b>required for JDBC</b></li>
 * <li><code>jdbcUsername=USER</code> <b>required for JDBC, optional JNDI</b></li>
 * <li><code>jdbcPassword=PASS</code> <b>required for JDBC, optional JNDI</b></li>
 * <li><code>selectUserPass=<br>
 * [e.g. SELECT LOWER(USERNAME), SALT FROM TAB_USER WHERE ACCOUNT_LOCKED=0 AND NVL(EVT_ACCOUNT_EXPIRY,SYSDATE)>=SYSDATE AND LOWER(USERNAME)=? AND PASSWORD=?]</code>
 * <b>required for JDBC</b></li>
 * <li>
 * <code>useJndiConnection=false</code> <b>default false</b></li>
 * <li>
 * <code>jndiName=[e.g. jdbc/jndiDbname]</code> <b>required for JNDI</b></li>
 * <li>
 * <code>jndiInitialContextFactory=</code></li>
 * <li><code>jndiProviderUrl=</code></li>
 * <li><code>jndiUrlPkgPrefixes=</code></li>
 * </ul>
 * <p>
 *
 * @author Michael Rudolf
 * @since 1.0.3 06.02.2009
 */
public class DataSourceSecurityFilter extends AbstractChainableSecurityFilter {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DataSourceSecurityFilter.class);
  public static final String PROP_BASIC_ATTEMPT = "DataSourceSecurityFilter.basicAttempt";

  /**
   * Default random salt that will be used to hash the passwords. Should be replaced by projects with an implementation
   * that uses a separate salt for each password.
   *
   * @see #isValidUser(String, String, Connection)
   */
  private static final byte[] DEFAULT_SALT = Base64Utility.decode("X89TeeW9tSB0KQkYex3/LQ==");

  // init params
  private String m_jdbcUserName;
  private String m_jdbcPassword;
  private String m_jdbcDriverName;
  private String m_jdbcMappingName;
  private String m_selectStatement;

  private boolean m_useJndiConnection;
  private String m_jndiName;
  private String m_jndiInitialContextFactory;
  private String m_jndiProviderUrl;
  private String m_jndiUrlPkgPrefixes;

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);

    // read config
    m_useJndiConnection = getConfigManager().getPropertyValue(DataSourceFilterUseJndiProperty.class);
    m_jdbcDriverName = getConfigManager().getPropertyValue(DataSourceFilterJdbcDriverNameProperty.class);
    m_jdbcMappingName = getConfigManager().getPropertyValue(DataSourceFilterJdbcMappingNameProperty.class);
    m_jdbcUserName = getConfigManager().getPropertyValue(DataSourceFilterJdbcUsernameProperty.class);
    m_jdbcPassword = getConfigManager().getPropertyValue(DataSourceFilterJdbcPasswordProperty.class);
    m_selectStatement = getConfigManager().getPropertyValue(DataSourceFilterJdbcSelectUserPassProperty.class);
    m_jndiName = getConfigManager().getPropertyValue(DataSourceFilterJndiNameProperty.class);
    m_jndiInitialContextFactory = getConfigManager().getPropertyValue(DataSourceFilterJndiInitialContextFactoryProperty.class);
    m_jndiProviderUrl = getConfigManager().getPropertyValue(DataSourceFilterJndiProviderUrlProperty.class);
    m_jndiUrlPkgPrefixes = getConfigManager().getPropertyValue(DataSourceFilterJndiUrlPkgPrefixesProperty.class);
  }

  @Override
  public void destroy() {
  }

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.matches("Basic .*")) {
      String[] a = new String(Base64Utility.decode(h.substring(6)), "ISO-8859-1").split(":", 2);
      String user = a[0].toLowerCase();
      String pass = a[1];
      if (user != null && pass != null) {
        if (isValidUser(user, pass)) {
          holder.setPrincipal(new SimplePrincipal(user));
          return STATUS_CONTINUE_WITH_PRINCIPAL;
        }
      }
    }
    int attempts = getBasicAttempt(req, resp);
    if (attempts > 2) {
      return STATUS_CONTINUE_CHAIN;
    }
    else {
      setBasicAttept(req, resp, attempts + 1);
      resp.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
      return STATUS_CONTINUE_CHAIN;
    }
  }

  private int getBasicAttempt(HttpServletRequest req, HttpServletResponse res) {
    int basicAtttempt = 0;
    Object attribute = BEANS.get(IHttpSessionCacheService.class).getAndTouch(PROP_BASIC_ATTEMPT, req, res);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, HttpServletResponse res, int attempts) {
    BEANS.get(IHttpSessionCacheService.class).put(PROP_BASIC_ATTEMPT, attempts, req, res);
  }

  protected Connection createConnection() throws Exception {
    if (m_useJndiConnection) {
      return createJndiConnection();
    }
    else {
      return createJdbcDirectConnection();
    }
  }

  protected boolean isValidUser(String username, String password) throws ServletException {
    try (Connection connection = createConnection()) {
      return isValidUser(username, password, connection);
    }
    catch (Exception e) {
      LOG.error("Cannot SELECT user/pass.", e);
      throw new ServletException(e.getMessage(), e);
    }
  }

  /**
   * This method can be overwritten by projects to use a custom salt per user instead of the pre-defined one of this
   * filter.
   *
   * @param username
   *          The username
   * @param password
   *          The clear text password
   * @param connection
   *          The connection to use to verify the username and password
   * @return <code>true</code> if it is valid. <code>false</code> otherwise.
   * @throws Exception
   */
  protected boolean isValidUser(String username, String password, Connection connection) throws Exception {
    password = encryptPass(password);

    try (PreparedStatement stmt = connection.prepareStatement(m_selectStatement)) {
      stmt.setString(1, username);
      stmt.setString(2, password);
      stmt.execute();
      ResultSet resultSet = stmt.getResultSet();
      return (resultSet.next() && resultSet.getString(1).equals(username));
    }
  }

  protected String encryptPass(String pass) throws ServletException {
    if (pass == null) {
      return null;
    }

    try {
      return Base64Utility.encode(SecurityUtility.hash(pass.getBytes("UTF-8"), DEFAULT_SALT));
    }
    catch (ProcessingException | UnsupportedEncodingException e) {
      throw new ServletException("Unable to hash password", e);
    }
  }

  protected Connection createJdbcDirectConnection() throws ClassNotFoundException, SQLException {
    Class.forName(m_jdbcDriverName);
    return DriverManager.getConnection(m_jdbcMappingName, m_jdbcUserName, m_jdbcPassword);
  }

  protected Connection createJndiConnection() throws NamingException, SQLException {
    InitialContext initialContext = null;

    String jndiName = m_jndiName;
    String jndiInitialContextFactory = m_jndiInitialContextFactory;
    String jndiProviderUrl = m_jndiProviderUrl;
    String jndiUrlPkgPrefixes = m_jndiUrlPkgPrefixes;
    if (LOG.isInfoEnabled()) {
      LOG.info("Opening rmi connection to: " + jndiName + "," + m_jdbcUserName);
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("  using initial context factory: " + jndiInitialContextFactory);
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("  using provider url: " + jndiProviderUrl);
    }
    Hashtable<String, String> ht = new Hashtable<String, String>();
    if (jndiInitialContextFactory != null) {
      ht.put(Context.INITIAL_CONTEXT_FACTORY, jndiInitialContextFactory);
    }
    if (jndiProviderUrl != null) {
      ht.put(Context.PROVIDER_URL, jndiProviderUrl);
    }
    if (jndiUrlPkgPrefixes != null) {
      ht.put(Context.URL_PKG_PREFIXES, jndiUrlPkgPrefixes);
    }
    DataSource dataSource = null;
    if (ht.size() > 0) {
      initialContext = new InitialContext(ht);
    }
    else {
      initialContext = new InitialContext();
    }
    dataSource = (DataSource) initialContext.lookup(jndiName);
    // Grab a connection
    Connection conn;
    if (m_jdbcUserName != null && m_jdbcPassword != null) {
      conn = dataSource.getConnection(m_jdbcUserName, m_jdbcPassword);
    }
    else {
      conn = dataSource.getConnection();
    }
    conn.setAutoCommit(false);
    return conn;
  }
}
