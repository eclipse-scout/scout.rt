/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

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

  private volatile String m_memberId;
  private volatile Session m_transactedSession;
  private volatile MessageProducer m_transactedProducer;
  private volatile boolean m_autoCloseClose;

  @Override
  public String getMemberId() {
    return m_memberId;
  }

  public JmsTransactionMember withMemberId(final String memberId) {
    m_memberId = memberId;
    return this;
  }

  public Session getTransactedSession() {
    return m_transactedSession;
  }

  public JmsTransactionMember withTransactedSession(final Session transactedSession) throws JMSException {
    assertTrue(transactedSession.getTransacted(), "session must be transacted");
    m_transactedSession = transactedSession;
    return this;
  }

  public MessageProducer getTransactedProducer() {
    return m_transactedProducer;
  }

  public JmsTransactionMember withTransactedProducer(final MessageProducer transactedProducer) {
    m_transactedProducer = transactedProducer;
    return this;
  }

  public JmsTransactionMember withAutoClose(final boolean autoClose) {
    m_autoCloseClose = autoClose;
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
    if (!m_autoCloseClose) {
      return;
    }

    try {
      m_transactedSession.close();
    }
    catch (final JMSException e) {
      LOG.error("Failed to close transacted session [session={}]", m_transactedSession, e);
    }
  }

  @Override
  public void cancel() {
    // NOOP
  }
}
