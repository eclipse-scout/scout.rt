/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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

  public Connection createJdbcConnection(AbstractSqlService sqlService) throws ClassNotFoundException, SQLException {
    String user = sqlService.getUsername();
    String pass = sqlService.getPassword();
    Class.forName(sqlService.getJdbcDriverName());
    //
    Connection conn;
    if (user != null && pass != null) {
      conn = DriverManager.getConnection(sqlService.getJdbcMappingName(), user, pass); // NOSONAR
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
        conn = DriverManager.getConnection(sqlService.getJdbcMappingName(), p); // NOSONAR
      }
      else {
        conn = DriverManager.getConnection(sqlService.getJdbcMappingName()); // NOSONAR
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
      LOG.info("Opening rmi connection. jndiName: '{}', user: '{}', initialContextFactory: '{}', providerUrl: '{}'", jndiName, user, jndiInitialContextFactory, jndiProviderUrl);
      @SuppressWarnings("squid:S1149")
      Hashtable<String, String> ht = new Hashtable<>();
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
      if (!ht.isEmpty()) {
        initialContext = new InitialContext(ht);
      }
      else {
        initialContext = new InitialContext();
      }
      dataSource = (DataSource) initialContext.lookup(jndiName);
      // Grab a connection
      Connection conn;
      if (user != null && pass != null) {
        conn = dataSource.getConnection(user, pass); // NOSONAR
      }
      else {
        conn = dataSource.getConnection(); // NOSONAR
      }
      conn.setAutoCommit(false);
      return conn;
    }
    finally {
      if (initialContext != null) {
        try {
          initialContext.close();
        }
        catch (Exception e) { // NOSONAR
        }
      }
    }
  }

}
