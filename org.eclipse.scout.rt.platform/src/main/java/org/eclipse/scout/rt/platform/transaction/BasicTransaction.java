/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.transaction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bean
@SuppressWarnings("squid:S1181")
public class BasicTransaction implements ITransaction {
  private static final Logger LOG = LoggerFactory.getLogger(BasicTransaction.class);

  private final Object m_memberMapLock = new Object();
  private final Map<String, ITransactionMember> m_memberMap = new LinkedHashMap<>();
  private final List<Throwable> m_failures = new ArrayList<>();
  private boolean m_commitPhase;
  private boolean m_cancelled;

  @Override
  public void registerMember(ITransactionMember member) {
    synchronized (m_memberMapLock) {
      String memberId = member.getMemberId();
      if (LOG.isDebugEnabled()) {
        LOG.debug("register transaction member {}", memberId);
      }
      // release existing
      ITransactionMember old = m_memberMap.get(memberId);
      if (old != null) {
        if (LOG.isWarnEnabled()) {
          LOG.warn("releasing overwritten transaction member {} / {}.", memberId, old.getMemberId());
        }
        old.release();
      }
      m_memberMap.put(memberId, member);
      //throw AFTER registering the resource in order to correctly release it later-on, bug 383736.
      if (m_cancelled) {
        throw new FutureCancelledError("Transaction cancelled");
      }
    }
  }

  @Override
  public <TRANSACTION_MEMBER extends ITransactionMember> TRANSACTION_MEMBER registerMemberIfAbsent(final String memberId, final Function<String, TRANSACTION_MEMBER> producer) {
    return registerMemberIfAbsent(memberId, producer, true);
  }

  @Override
  public <TRANSACTION_MEMBER extends ITransactionMember> TRANSACTION_MEMBER registerMemberIfAbsentAndNotCancelled(final String memberId, final Function<String, TRANSACTION_MEMBER> producer) {
    return registerMemberIfAbsent(memberId, producer, false);
  }

  protected <TRANSACTION_MEMBER extends ITransactionMember> TRANSACTION_MEMBER registerMemberIfAbsent(final String memberId, final Function<String, TRANSACTION_MEMBER> producer, boolean throwIfCancelled) {
    synchronized (m_memberMapLock) {
      @SuppressWarnings("unchecked")
      TRANSACTION_MEMBER member = (TRANSACTION_MEMBER) getMember(memberId);
      if (member == null) {
        if (!throwIfCancelled && m_cancelled) {
          return null;
        }
        member = producer.apply(memberId);
        registerMember(member);
      }
      return member;
    }
  }

  @Override
  public ITransactionMember getMember(String memberId) {
    synchronized (m_memberMapLock) {
      ITransactionMember res = m_memberMap.get(memberId);
      if (LOG.isDebugEnabled()) {
        LOG.debug("get transaction member '{}' -> '{}'.", memberId, res);
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
    return m_memberMap.values().toArray(new ITransactionMember[0]);
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
        LOG.debug("unregister transaction member '{}'.", memberId);
      }
    }
  }

  @Override
  public boolean commitPhase1() {
    synchronized (m_memberMapLock) {
      if (m_cancelled) {
        throw new FutureCancelledError("Transaction cancelled");
      }
      m_commitPhase = true;
    }
    for (ITransactionMember mem : getMembers()) {
      if (mem.needsCommit()) {
        LOG.debug("commit phase 1 of transaction member '{}'.", mem.getMemberId());

        if (!mem.commitPhase1()) {
          LOG.error("commit phase 1 failed for transaction member '{}'.", mem.getMemberId());
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public void commitPhase2() {
    for (ITransactionMember mem : getMembers()) {
      try {
        if (mem.needsCommit()) {
          LOG.debug("commit phase 2 of transaction member '{}'.", mem);
          mem.commitPhase2();
        }
      }
      catch (Throwable t) {
        addFailure(t);
        LOG.error("commit phase 2 failed for transaction member '{}'.", mem.getMemberId(), t);
      }
    }
  }

  @Override
  public void rollback() {
    for (ITransactionMember mem : getMembers()) {
      try {
        if (mem.needsCommit()) {
          LOG.debug("rollback of transaction membmer '{}'.", mem);
          mem.rollback();
        }
      }
      catch (Throwable t) {
        addFailure(t);
        LOG.error("rollback failed for transaction member '{}'.", mem.getMemberId(), t);
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
        LOG.debug("release of transaction member '{}'. ", mem);
        mem.release();
      }
      catch (Throwable t) {
        LOG.error("release member {}", mem, t);
      }
    }
  }

  @Override
  public boolean hasFailures() {
    return !m_failures.isEmpty();
  }

  @Override
  public Throwable[] getFailures() {
    return m_failures.toArray(new Throwable[0]);
  }

  @Override
  public void addFailure(Throwable t) {
    if (!m_failures.contains(t)) {
      m_failures.add(t);
    }
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    synchronized (m_memberMapLock) {
      if (m_commitPhase) {
        return false;
      }
      if (m_cancelled) {
        return true;
      }
      m_cancelled = true;
      addFailure(new FutureCancelledError("Transaction cancelled"));
    }
    for (ITransactionMember mem : getMembers()) {
      try {
        mem.cancel();
      }
      catch (Throwable t) {
        LOG.error("cancel member {}", mem, t);
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
