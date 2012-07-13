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
import java.util.HashMap;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

public class BasicTransaction implements ITransaction {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BasicTransaction.class);

  private final long m_transactionSequence;
  private final Object m_memberMapLock = new Object();
  private final HashMap<String, ITransactionMember> m_memberMap = new HashMap<String, ITransactionMember>();
  private ArrayList<Throwable> m_failures = new ArrayList<Throwable>();
  private boolean m_commitPhase;
  private boolean m_cancelled;

  public BasicTransaction() {
    this(0L);
  }

  /**
   * @param transactionSequence
   *          see {@link ITransaction#getTransactionSequence()}
   */
  public BasicTransaction(long transactionSequence) {
    m_transactionSequence = transactionSequence;
  }

  @Override
  public long getTransactionSequence() {
    return m_transactionSequence;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void registerResource(ITransactionMember member) {
    try {
      registerMember(member);
    }
    catch (ProcessingException e) {
      throw new IllegalStateException("Interrupted");
    }
  }

  @Override
  public void registerMember(ITransactionMember member) throws ProcessingException {
    synchronized (m_memberMapLock) {
      String memberId = member.getMemberId();
      if (LOG.isDebugEnabled()) {
        LOG.debug("register transaction member {0}", memberId);
      }
      // release existing
      ITransactionMember old = (ITransactionMember) m_memberMap.get(memberId);
      if (old != null) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("releasing overwritten transaction member {0} / {1}.", memberId, old.getMemberId());
        }
        old.release();
      }
      m_memberMap.put(memberId, member);
      //throw AFTER registering the resource in order to correctly release it later-on, bug 383736.
      if (m_cancelled) {
        throw new ProcessingException("Interrupted", new InterruptedException());
      }
    }
  }

  @Override
  public ITransactionMember getMember(String memberId) {
    synchronized (m_memberMapLock) {
      ITransactionMember res = (ITransactionMember) m_memberMap.get(memberId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("get transaction member '{0}' -> '{1}'.", memberId, res);
      }
      return res;
    }
  }

  @Override
  public ITransactionMember[] getMembers() {
    synchronized (m_memberMapLock) {
      return getMembersNoLocking();
    }
  }

  private ITransactionMember[] getMembersNoLocking() {
    return m_memberMap.values().toArray(new ITransactionMember[m_memberMap.size()]);
  }

  @Override
  public void unregisterMember(ITransactionMember member) {
    if (member == null) {
      return;
    }
    unregisterMember(member.getMemberId());
  }

  @Override
  public void unregisterMember(String memberId) {
    synchronized (m_memberMapLock) {
      m_memberMap.remove(memberId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("unregister transaction member '{0}'.", memberId);
      }
    }
  }

  @Override
  public boolean commitPhase1() throws ProcessingException {
    synchronized (m_memberMapLock) {
      if (m_cancelled) {
        throw new ProcessingException("Interrupted", new InterruptedException());
      }
      m_commitPhase = true;
    }
    boolean allSuccessful = true;
    for (ITransactionMember mem : getMembers()) {
      try {
        if (mem.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("commit phase 1 of transaction member '{0}'.", mem.getMemberId());
          }
          boolean b = mem.commitPhase1();
          allSuccessful = allSuccessful && b;
          if (!allSuccessful) {
            LOG.error("commit phase 1 failed for transaction member '{}'.", mem.getMemberId());
            break;
          }
        }
      }
      catch (Throwable t) {
        addFailure(t);
        LOG.error("commit phase 1 failed with exception for transaction member '" + mem.getMemberId() + "'.", t);
        break;
      }
    }
    return allSuccessful && !hasFailures();
  }

  @Override
  public void commitPhase2() {
    for (ITransactionMember mem : getMembers()) {
      try {
        if (mem.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(" " + mem);
          }
          mem.commitPhase2();
        }
      }
      catch (Throwable t) {
        addFailure(t);
        LOG.error("commit phase 2 failed for transaction member '" + mem.getMemberId() + "'.", t);
      }
    }
  }

  @Override
  public void rollback() {
    for (ITransactionMember mem : getMembers()) {
      try {
        if (mem.needsCommit()) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(" " + mem);
          }
          mem.rollback();
        }
      }
      catch (Throwable t) {
        addFailure(t);
        LOG.error("rollback failed for transaction member '" + mem.getMemberId() + "'.", t);
      }
    }
  }

  @Override
  public void release() {
    ITransactionMember[] a;
    synchronized (m_memberMapLock) {
      a = getMembersNoLocking();
      m_memberMap.clear();
    }
    for (ITransactionMember mem : a) {
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug(" " + mem);
        }
        mem.release();
      }
      catch (Throwable t) {
        LOG.error("release " + mem, t);
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

  @Override
  public synchronized boolean cancel() {
    synchronized (m_memberMapLock) {
      if (m_commitPhase) {
        return false;
      }
      if (m_cancelled) {
        return true;
      }
      m_cancelled = true;
      addFailure(new InterruptedException());
    }
    for (ITransactionMember mem : getMembers()) {
      try {
        mem.cancel();
      }
      catch (Throwable t) {
        LOG.error("cancel " + mem, t);
      }
    }
    return true;
  }

  @Override
  public boolean isCancelled() {
    synchronized (m_memberMapLock) {
      return m_cancelled;
    }
  }
}
