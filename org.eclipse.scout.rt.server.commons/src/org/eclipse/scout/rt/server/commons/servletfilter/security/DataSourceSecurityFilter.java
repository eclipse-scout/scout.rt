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
import java.security.NoSuchAlgorithmException;
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
import org.eclipse.scout.commons.EncryptionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;

/**
 * <h4>DataSourceSecurityFilter</h4> The following properties can be set in the <code>config.ini</code> file:
 * <ul>
 * <li><code>&lt;fully qualified name of class&gt;#active=true/false</code> <b>might be set in the extension point</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#realm=abcde</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#failover=true/false</code> <b>default false</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#jdbcDriverName=[e.g. oracle.jdbc.OracleDriver]</code> <b>required for
 * JDBC</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#jdbcMappingName=[e.g. jdbc:oracle:thin:@dbUrl:1521:DBNAME]</code>
 * <b>required for JDBC</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#jdbcUsername=USER</code> <b>required for JDBC, optional JNDI</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#jdbcPassword=PASS</code> <b>required for JDBC, optional JNDI</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#selectUserPass=<br>
 * [e.g. SELECT LOWER(USERNAME) FROM ORS_WEB_USER WHERE ACCOUNT_LOCKED=0 AND NVL(EVT_ACCOUNT_EXPIRY,SYSDATE)>=SYSDATE AND LOWER(USERNAME)=? AND PASSWORD=?]</code>
 * <b>required</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#useJndiConnection=false</code> <b>default false</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#jndiName=[e.g. jdbc/jndiDbname]</code> <b>required for JNDI</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#jndiInitialContextFactory=</code></li>
 * <li><code>&lt;fully qualified name of class&gt;#jndiProviderUrl=</code></li>
 * <li><code>&lt;fully qualified name of class&gt;#jndiUrlPkgPrefixes=</code></li>
 * </ul>
 * <p>
 * , Michael Rudolf
 * 
 * @since 1.0.3 06.02.2009
 */
public class DataSourceSecurityFilter extends AbstractChainableSecurityFilter {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DataSourceSecurityFilter.class);
  public static final String PROP_BASIC_ATTEMPT = "DataSourceSecurityFilter.basicAttempt";

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
  public void init(FilterConfig config0) throws ServletException {
    super.init(config0);
    FilterConfigInjection.FilterConfig config = new FilterConfigInjection(config0, getClass()).getAnyConfig();

    String useJndiConnectionString = config.getInitParameter("useJndiConnection");
    m_useJndiConnection = Boolean.parseBoolean(useJndiConnectionString);

    m_jdbcDriverName = getInitParam(config, "jdbcDriverName", !m_useJndiConnection);
    m_jdbcMappingName = getInitParam(config, "jdbcMappingName", !m_useJndiConnection);
    m_jdbcUserName = getInitParam(config, "jdbcUsername", false);
    m_jdbcPassword = getInitParam(config, "jdbcPassword", false);
    m_selectStatement = getInitParam(config, "selectUserPass", !m_useJndiConnection);
    m_jndiName = getInitParam(config, "jndiName", m_useJndiConnection);
    m_jndiInitialContextFactory = config.getInitParameter("jndiInitialContextFactory");
    m_jndiProviderUrl = config.getInitParameter("jndiProviderUrl");
    m_jndiUrlPkgPrefixes = config.getInitParameter("jndiUrlPkgPrefixes");
  }

  private String getInitParam(FilterConfig filterConfig, String paramName, boolean requierd) throws ServletException {
    String paramValue = filterConfig.getInitParameter(paramName);
    if (requierd && paramValue == null) {
      throw new ServletException("Missing init-param with name '" + paramName + "'.");
    }
    return paramValue;
  }

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.matches("Basic .*")) {
      String[] a = new String(Base64Utility.decode(h.substring(6)), "ISO-8859-1").split(":", 2);
      String user = a[0].toLowerCase();
      String pass = a[1];
      if (user != null && pass != null) {
        String passEncrypted = encryptPass(pass);
        if (isValidUser(user, passEncrypted)) {
          holder.setPrincipal(new SimplePrincipal(user));
          return STATUS_CONTINUE_WITH_PRINCIPAL;
        }
      }
    }
    int attempts = getBasicAttempt(req);
    if (attempts > 2) {
      return STATUS_CONTINUE_CHAIN;
    }
    else {
      setBasicAttept(req, attempts + 1);
      resp.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
      return STATUS_CONTINUE_CHAIN;
    }
  }

  private int getBasicAttempt(HttpServletRequest req) {
    int basicAtttempt = 0;
    Object attribute = req.getSession().getAttribute(PROP_BASIC_ATTEMPT);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, int attempts) {
    req.getSession().setAttribute(PROP_BASIC_ATTEMPT, attempts);
  }

  protected boolean isValidUser(String username, String password) throws ServletException {
    Connection databaseConnection = null;
    try {

      if (m_useJndiConnection) {
        databaseConnection = createJndiConnection();
      }
      else {
        databaseConnection = createJdbcDirectConnection();
      }
      return isValidUser(username, password, databaseConnection);

    }
    catch (Exception e) {
      LOG.error("Cannot SELECT user/pass.", e);
      throw new ServletException(e.getMessage(), e);
    }
    finally {
      try {
        if (databaseConnection != null) {
          databaseConnection.close();
          databaseConnection = null;
        }
      }
      catch (SQLException e) {
        LOG.warn("Exception in close connection!", e);
      }
    }
  }

  protected boolean isValidUser(String username, String password, Connection connection) throws SQLException {
    PreparedStatement stmt = null;
    try {
      stmt = connection.prepareStatement(m_selectStatement);
      stmt.setString(1, username);
      stmt.setString(2, password);
      stmt.execute();
      ResultSet resultSet = stmt.getResultSet();
      return (resultSet.next() && resultSet.getString(1).equals(username));
    }
    finally {
      try {
        if (stmt != null) {
          stmt.close();
          stmt = null;
        }
      }
      catch (SQLException e) {
        LOG.warn("Exception in close stmt!", e);
      }
    }
  }

  protected String encryptPass(String pass) throws ServletException {
    String passEncrypted = null;
    if (pass != null) {
      try {
        passEncrypted = Base64Utility.encode(EncryptionUtility.signMD5(pass.getBytes()));
      }
      catch (NoSuchAlgorithmException e) {
        LOG.error("couldn't create the password", e);
        throw new ServletException("couldn't create the password", e);
      }
    }
    return passEncrypted;
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
