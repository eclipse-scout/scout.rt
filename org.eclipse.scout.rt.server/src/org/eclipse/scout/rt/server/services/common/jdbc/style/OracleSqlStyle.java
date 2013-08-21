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
package org.eclipse.scout.rt.server.services.common.jdbc.style;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.jdbc.SqlBind;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.ITransactionMember;

public class OracleSqlStyle extends AbstractSqlStyle {
  private static final long serialVersionUID = 1L;
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OracleSqlStyle.class);

  @Override
  public String getConcatOp() {
    return "||";
  }

  @Override
  public String getLikeWildcard() {
    return "%";
  }

  @Override
  protected int getMaxListSize() {
    return 1000;
  }

  @Override
  public boolean isLargeString(String s) {
    return (s.length() > 4000);
  }

  @Override
  public boolean isBlobEnabled() {
    return true;
  }

  @Override
  public boolean isClobEnabled() {
    return true;
  }

  @Override
  public void testConnection(Connection conn) throws SQLException {
    Statement testStatement = null;
    try {
      testStatement = conn.createStatement();
      testStatement.execute("SELECT 1 FROM DUAL");
    }
    finally {
      if (testStatement != null) {
        try {
          testStatement.close();
        }
        catch (Throwable t) {
        }
      }
    }
  }

  @Override
  public void writeBind(PreparedStatement ps, int jdbcBindIndex, SqlBind bind) throws SQLException {
    switch (bind.getSqlType()) {
      case Types.CLOB: {
        if (bind.getValue() instanceof Clob) {
          super.writeBind(ps, jdbcBindIndex, bind);
        }
        else {
          String s = (String) bind.getValue();
          /*
           * Reflect: CLOB
           * newClob=CLOB.createTemporary(conn,false,oracle.sql.CLOB
           * .DURATION_SESSION); (a) newClob.putString(1,s); (b)
           * newClob.setString(1,s);
           */
          Clob clob;
          try {
            Class<?> clobClass;
            clobClass = Class.forName("oracle.sql.CLOB", true, ps.getClass().getClassLoader());
            clob = (Clob) clobClass.getMethod("createTemporary", new Class[]{Connection.class, boolean.class, int.class}).invoke(null, new Object[]{ps.getConnection(), false, clobClass.getField("DURATION_SESSION").get(null)});
            registerClob(clob);
          }
          catch (Throwable t) {
            SQLException ex = new SQLException("bind clob on jdbcIndex " + jdbcBindIndex);
            ex.initCause(t);
            throw ex;
          }
          //
          try {
            clob.getClass().getMethod("putString", new Class[]{long.class, String.class}).invoke(clob, new Object[]{1, s});
          }
          catch (Throwable t) {
            clob.setString(1, s);
          }
          ps.setClob(jdbcBindIndex, clob);
        }
        break;
      }
      case Types.BLOB: {
        if (bind.getValue() instanceof Blob) {
          super.writeBind(ps, jdbcBindIndex, bind);
        }
        else {
          byte[] data = (byte[]) bind.getValue();
          /*
           * Reflect: BLOB
           * newBlob=BLOB.createTemporary(conn,false,oracle.sql.BLOB
           * .DURATION_SESSION); (a) newBlob.putBytes(1,data); (b)
           * newBlob.setBytes(1,data);
           */
          Blob blob;
          try {
            Class<?> blobClass = Class.forName("oracle.sql.BLOB", true, ps.getClass().getClassLoader());
            blob = (Blob) blobClass.getMethod("createTemporary", new Class[]{Connection.class, boolean.class, int.class}).invoke(null, new Object[]{ps.getConnection(), false, blobClass.getField("DURATION_SESSION").get(null)});
            registerBlob(blob);
          }
          catch (Throwable t) {
            SQLException ex = new SQLException("bind blob on jdbcIndex " + jdbcBindIndex);
            ex.initCause(t);
            throw ex;
          }
          //
          try {
            blob.getClass().getMethod("putBytes", new Class[]{long.class, byte[].class}).invoke(blob, new Object[]{1, data});
          }
          catch (Throwable t) {
            blob.setBytes(1, data);
          }
          ps.setBlob(jdbcBindIndex, blob);
        }
        break;
      }
      default: {
        super.writeBind(ps, jdbcBindIndex, bind);
      }
    }
  }

  protected void registerBlob(Blob blob) {
    if (blob == null) {
      return;
    }
    OracleLobTransactionMember txnMember = getOrCreateLobTransactionMember(true);
    if (txnMember != null) {
      txnMember.registerBlob(blob);
    }
  }

  protected void registerClob(Clob clob) {
    if (clob == null) {
      return;
    }
    OracleLobTransactionMember txnMember = getOrCreateLobTransactionMember(true);
    if (txnMember != null) {
      txnMember.registerClob(clob);
    }
  }

  private OracleLobTransactionMember getOrCreateLobTransactionMember(boolean autoCreate) {
    ITransaction reg = ThreadContext.getTransaction();
    if (reg == null) {
      LOG.warn("no ITransaction available, use ServerJob to run truncactions");
      return null;
    }
    OracleLobTransactionMember member = (OracleLobTransactionMember) reg.getMember(OracleLobTransactionMember.TRANSACTION_MEMBER_ID);
    if (member == null && autoCreate) {
      try {
        member = new OracleLobTransactionMember();
        reg.registerMember(member);
      }
      catch (Throwable t) {
        LOG.warn("Unexpected error while registering transaction member", t);
        return null;
      }
    }
    return member;
  }

  @Override
  public void commit() {
    OracleLobTransactionMember member = getOrCreateLobTransactionMember(false);
    if (member != null) {
      member.release();
    }
  }

  @Override
  public void rollback() {
    OracleLobTransactionMember member = getOrCreateLobTransactionMember(false);
    if (member != null) {
      member.release();
    }
  }

  /**
   * Scout transaction member that frees all LOBs which were created during the current transaction.
   * 
   * @since 3.8.3
   */
  private static class OracleLobTransactionMember implements ITransactionMember {

    private static final IScoutLogger LOG = ScoutLogManager.getLogger(OracleLobTransactionMember.class);
    public static final String TRANSACTION_MEMBER_ID = OracleLobTransactionMember.class.getName();

    private Set<Blob> m_temporaryBlobs;
    private Set<Clob> m_temporaryClobs;

    public void registerBlob(Blob blob) {
      if (!"oracle.sql.BLOB".equals(blob.getClass().getName())) {
        return;
      }
      if (m_temporaryBlobs == null) {
        m_temporaryBlobs = new HashSet<Blob>();
      }
      m_temporaryBlobs.add(blob);
    }

    public void registerClob(Clob clob) {
      if (!"oracle.sql.CLOB".equals(clob.getClass().getName())) {
        return;
      }
      if (m_temporaryClobs == null) {
        m_temporaryClobs = new HashSet<Clob>();
      }
      m_temporaryClobs.add(clob);
    }

    private void releaseLobs(Set<?> lobs) {
      if (lobs == null || lobs.isEmpty()) {
        return;
      }

      for (Object lob : lobs) {
        try {
          lob.getClass().getMethod("freeTemporary").invoke(lob);
        }
        catch (Throwable t) {
          // NOP: free whatever possible, otherwise go on
        }
      }
      lobs.clear();
    }

    @Override
    public String getMemberId() {
      return TRANSACTION_MEMBER_ID;
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
      releaseLobs(m_temporaryBlobs);
      releaseLobs(m_temporaryClobs);
    }

    @Override
    public void cancel() {
    }
  }
}
