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
package org.eclipse.scout.rt.server.jdbc.internal.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.jdbc.AbstractSqlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlConnectionBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(SqlConnectionBuilder.class);

  public Connection createJdbcConnection(AbstractSqlService sqlService) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
    String user = sqlService.getUsername();
    String pass = sqlService.getPassword();
    Class.forName(sqlService.getJdbcDriverName());
    //
    Connection conn;
    if (user != null && pass != null) {
      conn = DriverManager.getConnection(sqlService.getJdbcMappingName(), user, pass);
    }
    else {
      Properties p = null;
      String s = sqlService.getJdbcProperties();
      if (!StringUtility.hasText(s)) {
        s = null;
      }
      if (s != null) {
        p = new Properties();
        StringTokenizer tok = new StringTokenizer(s, ";");
        while (tok.hasMoreTokens()) {
          String t = tok.nextToken();
          StringTokenizer tok2 = new StringTokenizer(t, "=");
          String n = tok2.nextToken();
          String v = tok2.nextToken();
          p.setProperty(n, v);
        }
      }
      if (p != null) {
        conn = DriverManager.getConnection(sqlService.getJdbcMappingName(), p);
      }
      else {
        conn = DriverManager.getConnection(sqlService.getJdbcMappingName());
      }
    }
    conn.setAutoCommit(false);
    return conn;
  }

  public Connection createJndiConnection(AbstractSqlService sqlService) throws NamingException, SQLException {
    InitialContext initialContext = null;
    try {
      String user = sqlService.getUsername();
      String pass = sqlService.getPassword();
      String jndiName = sqlService.getJndiName();
      String jndiInitialContextFactory = sqlService.getJndiInitialContextFactory();
      String jndiProviderUrl = sqlService.getJndiProviderUrl();
      String jndiUrlPkgPrefixes = sqlService.getJndiUrlPkgPrefixes();
      if (LOG.isInfoEnabled()) {
        LOG.info("Opening rmi connection to: " + jndiName + "," + user);
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
      if (user != null && pass != null) {
        conn = dataSource.getConnection(user, pass);
      }
      else {
        conn = dataSource.getConnection();
      }
      conn.setAutoCommit(false);
      return conn;
    }
    finally {
      if (initialContext != null) {
        try {
          initialContext.close();
        }
        catch (Exception e) {
        }
      }
    }
  }

}
