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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.util.collection.ConcurrentExpiringMap;
import org.eclipse.scout.rt.server.jdbc.IStatementCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S1166")
public class PreparedStatementCache extends AbstractTransactionMember implements IStatementCache {
  private static final Logger LOG = LoggerFactory.getLogger(PreparedStatementCache.class);

  public static final String TRANSACTION_MEMBER_ID = "PreparedStatementCache";

  private ConcurrentExpiringMap<String, Integer> m_countCache;
  private ConcurrentExpiringMap<String, PreparedStatement> m_statementCache;

  public PreparedStatementCache(int statementCacheSize) {
    super(TRANSACTION_MEMBER_ID);
    m_countCache = new ConcurrentExpiringMap<String, Integer>(2L, TimeUnit.MINUTES, 200);
    m_statementCache = new ConcurrentExpiringMap<String, PreparedStatement>(1L, TimeUnit.HOURS, statementCacheSize) {
      @Override
      protected void execEntryEvicted(String key, PreparedStatement value) {
        closePreparedStatement(value);
      }
    };
  }

  @SuppressWarnings("resource")
  @Override
  public PreparedStatement getPreparedStatement(Connection conn, String s) throws SQLException {
    // a statement must be used at least 2 times within 2 minutes in order to be
    // cached
    PreparedStatement ps = m_statementCache.get(s);
    if (ps == null) {
      ps = conn.prepareStatement(s);
      Integer count = m_countCache.get(s);
      count = (count != null ? count : 0) + 1;
      m_countCache.put(s, count);
      if (count >= 2) {
        // second access, cache it
        m_statementCache.put(s, ps);
      }
    }
    else {
      ps.clearParameters();
      ps.clearWarnings();
    }
    return ps;
  }

  @Override
  public void releasePreparedStatement(PreparedStatement ps) {
    // close statement when it is not cached
    if (ps != null && !m_statementCache.containsValue(ps)) {
      try {
        ps.close();
      }
      catch (Exception e) {
        LOG.warn("Exception while closing PreparedStatement", e);
      }
    }
  }

  @SuppressWarnings("resource")
  @Override
  public CallableStatement getCallableStatement(Connection conn, String s) throws SQLException {
    // a statement must be used at least 2 times within 2 minutes in order to be
    // cached
    CallableStatement cs = (CallableStatement) m_statementCache.get(s);
    if (cs == null) {
      cs = conn.prepareCall(s);
      Integer count = m_countCache.get(s);
      count = (count != null ? count : 0) + 1;
      m_countCache.put(s, count);
      if (count >= 2) {
        // second access, cache it
        m_statementCache.put(s, cs);
      }
    }
    return cs;
  }

  @Override
  public void releaseCallableStatement(CallableStatement cs) {
    // close statement when it is not cached
    if (cs != null && !m_statementCache.containsValue(cs)) {
      try {
        cs.close();
      }
      catch (Exception e) {
        LOG.warn("Exception while closing CallableStatement", e);
      }
    }
  }

  @Override
  public void release() {
    for (Iterator<Entry<String, PreparedStatement>> iterator = m_statementCache.entrySet().iterator(); iterator.hasNext();) {
      Entry<String, PreparedStatement> entry = iterator.next();
      iterator.remove();
      closePreparedStatement(entry.getValue());
    }
    m_countCache.clear();
  }

  private void closePreparedStatement(Object value) {
    PreparedStatement ps = (PreparedStatement) value;
    try {
      ps.close();
    }
    catch (SQLException e) {
      LOG.warn("disposing prepared statement");
    }
  }
}
