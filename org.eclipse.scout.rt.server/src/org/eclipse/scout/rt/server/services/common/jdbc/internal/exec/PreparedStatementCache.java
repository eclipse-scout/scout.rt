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
package org.eclipse.scout.rt.server.services.common.jdbc.internal.exec;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.scout.commons.LRUCache;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.jdbc.IStatementCache;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;

public class PreparedStatementCache implements ITransactionMember, IStatementCache {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PreparedStatementCache.class);

  public static final String TRANSACTION_MEMBER_ID = "PreparedStatementCache";

  private LRUCache<String, Integer> m_countCache;
  private LRUCache<String, PreparedStatement> m_statementCache;

  public PreparedStatementCache(int statementCacheSize) {
    m_countCache = new LRUCache<String, Integer>(200, 120000L);
    m_statementCache = new LRUCache<String, PreparedStatement>(statementCacheSize, 3600000L);
    m_statementCache.addDisposeListener(new LRUCache.DisposeListener() {
      @Override
      public void valueDisposed(Object key, Object value) {
        PreparedStatement ps = (PreparedStatement) value;
        try {
          ps.close();
        }
        catch (SQLException e) {
          LOG.warn("disposing prepared statement");
        }
      }
    });
  }

  @Override
  public String getMemberId() {
    return TRANSACTION_MEMBER_ID;
  }

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
  public void releasePreparedStatement(PreparedStatement ps) throws SQLException {
    // close statement when it is not cached
    if (ps != null) {
      if (!m_statementCache.containsValue(ps)) {
        ps.close();
      }
    }
  }

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
  public void releaseCallableStatement(CallableStatement cs) throws SQLException {
    // close statement when it is not cached
    if (cs != null) {
      if (!m_statementCache.containsValue(cs)) {
        cs.close();
      }
    }
  }

  @Override
  public boolean needsCommit() {
    return false;
  }

  @Override
  public boolean commitPhase1() {
    return true;
  }

  @Override
  public void commitPhase2() {
  }

  @Override
  public void rollback() {
  }

  @Override
  public void release() {
    m_statementCache.clear();// will call the dispose listener
    m_countCache.clear();
  }

}
