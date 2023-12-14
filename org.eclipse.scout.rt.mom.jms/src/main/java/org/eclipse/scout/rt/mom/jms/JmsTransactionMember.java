/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;
import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import jakarta.jms.JMSException;
import jakarta.jms.Session;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction member used for transacted messaging.
 *
 * @since 6.1
 */
@Bean
public class JmsTransactionMember implements ITransactionMember {

  private static final Logger LOG = LoggerFactory.getLogger(JmsTransactionMember.class);

  private String m_memberId;
  private IJmsSessionProvider m_sessionProvider;
  private Session m_transactedSession;
  private boolean m_autoClose;

  @Override
  public String getMemberId() {
    return m_memberId;
  }

  public JmsTransactionMember withMemberId(final String memberId) {
    m_memberId = memberId;
    return this;
  }

  public IJmsSessionProvider getSessionProvider() {
    return m_sessionProvider;
  }

  public JmsTransactionMember withSessionProvider(IJmsSessionProvider sessionProvider) throws JMSException {
    m_sessionProvider = sessionProvider;
    m_transactedSession = sessionProvider.getSession();
    assertNotNull(m_transactedSession);
    assertTrue(m_transactedSession.getTransacted());
    return this;
  }

  public Session getTransactedSession() {
    return m_transactedSession;
  }

  public JmsTransactionMember withAutoClose(final boolean autoClose) {
    m_autoClose = autoClose;
    return this;
  }

  @Override
  public boolean needsCommit() {
    return true;
  }

  @Override
  public boolean commitPhase1() {
    return true;
  }

  @Override
  public void commitPhase2() {
    try {
      m_transactedSession.commit();
    }
    catch (final JMSException e) {
      LOG.error("Failed to commit transacted session [session={}]", m_transactedSession, e);
    }
  }

  @Override
  public void rollback() {
    try {
      m_transactedSession.rollback();
    }
    catch (final JMSException e) {
      LOG.error("Failed to rollback transacted session [session={}]", m_transactedSession, e);
    }
  }

  @Override
  public void release() {
    if (!m_autoClose) {
      return;
    }

    getSessionProvider().close();
  }

  @Override
  public void cancel() {
    // NOOP
  }
}
