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
package org.eclipse.scout.rt.server.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class BasicTransaction implements ITransaction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BasicTransaction.class);

  private Object m_memberMapLock;
  private HashMap<String, Object> m_memberMap;
  private ArrayList<Throwable> m_failures = new ArrayList<Throwable>();

  public BasicTransaction() {
    m_memberMapLock = new Object();
  }

  @Override
  public void registerResource(ITransactionMember member) {
    synchronized (m_memberMapLock) {
      String memberId = member.getMemberId();
      if (LOG.isDebugEnabled()) {
        LOG.debug("" + memberId + "/" + member);
      }
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      // release existing
      ITransactionMember old = (ITransactionMember) m_memberMap.get(memberId);
      if (old != null) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("releasing overwritten " + memberId + "/" + old);
        }
        old.release();
      }
      m_memberMap.put(memberId, member);
    }
  }

  @Override
  public ITransactionMember getMember(String memberId) {
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      ITransactionMember res = (ITransactionMember) m_memberMap.get(memberId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("" + memberId + "->" + res);
      }
      return res;
    }
  }

  @Override
  public ITransactionMember[] getMembers() {
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      return m_memberMap.values().toArray(new ITransactionMember[0]);
    }
  }

  @Override
  public void unregisterMember(ITransactionMember member) {
    if (member != null) {
      synchronized (m_memberMapLock) {
        String memberId = member.getMemberId();
        if (m_memberMap != null) {
          Object o = m_memberMap.get(memberId);
          if (o == member) {
            m_memberMap.remove(memberId);
            if (LOG.isDebugEnabled()) {
              LOG.debug("" + memberId + "->" + o);
            }
          }
        }
      }
    }
  }

  @Override
  public boolean commitPhase1() {
    Collection xaList;
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      xaList = new ArrayList<Object>(m_memberMap.values());
    }
    boolean allSuccessful = true;
    for (Iterator it = xaList.iterator(); it.hasNext();) {
      ITransactionMember res = (ITransactionMember) it.next();
      try {
        if (res.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(" " + res);
          }
          boolean b = res.commitPhase1();
          allSuccessful = allSuccessful && b;
        }
      }
      catch (Throwable t) {
        LOG.error("commit phase 1" + res, t);
      }
    }
    return allSuccessful && !hasFailures();
  }

  @Override
  public void commitPhase2() {
    Collection xaList;
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      xaList = new ArrayList<Object>(m_memberMap.values());
    }
    for (Iterator it = xaList.iterator(); it.hasNext();) {
      ITransactionMember res = (ITransactionMember) it.next();
      try {
        if (res.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(" " + res);
          }
          res.commitPhase2();
        }
      }
      catch (Throwable t) {
        LOG.error("commit phase 2" + res, t);
      }
    }
  }

  @Override
  public void rollback() {
    Collection xaList;
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      xaList = new ArrayList<Object>(m_memberMap.values());
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("");
    }
    for (Iterator it = xaList.iterator(); it.hasNext();) {
      ITransactionMember res = (ITransactionMember) it.next();
      try {
        if (res.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(" " + res);
          }
          res.rollback();
        }
      }
      catch (Throwable t) {
        LOG.error("rollback " + res, t);
      }
    }
  }

  @Override
  public void release() {
    Collection xaList;
    synchronized (m_memberMapLock) {
      if (m_memberMap == null) {
        m_memberMap = new HashMap<String, Object>();
      }
      xaList = new ArrayList<Object>(m_memberMap.values());
      m_memberMap.clear();
    }
    if (LOG.isDebugEnabled()) {
      LOG.debug("");
    }
    for (Iterator it = xaList.iterator(); it.hasNext();) {
      ITransactionMember res = (ITransactionMember) it.next();
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug(" " + res);
        }
        res.release();
      }
      catch (Throwable t) {
        LOG.error("release " + res, t);
      }
    }
  }

  @Override
  public boolean hasFailures() {
    return m_failures.size() > 0;
  }

  @Override
  public Throwable[] getFailures() {
    return m_failures.toArray(new Throwable[m_failures.size()]);
  }

  @Override
  public void addFailure(Throwable t) {
    m_failures.add(t);
  }

}
