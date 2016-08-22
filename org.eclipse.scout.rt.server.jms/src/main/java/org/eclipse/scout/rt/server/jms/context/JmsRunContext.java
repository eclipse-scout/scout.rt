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
package org.eclipse.scout.rt.server.jms.context;

import java.util.Locale;
import java.util.Map;

import javax.jms.Message;
import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.transaction.ITransactionMember;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * The <code>JmsRunContext</code> facilitates propagation of the <i>JMS Java Message Service</i> state. This context is
 * not intended to be propagated across different threads.
 * <p>
 * A context typically represents a "snapshot" of the current calling state. This class facilitates propagation of that
 * state.
 *
 * @since 5.1
 * @see RunContext
 */
public class JmsRunContext extends RunContext {

  /**
   * The {@link Message} which is currently associated with the current thread.
   */
  public static final ThreadLocal<Message> CURRENT_JMS_MESSAGE = new ThreadLocal<>();

  protected Message m_jmsMessage;

  @Override
  protected <RESULT> void interceptCallableChain(CallableChain<RESULT> callableChain) {
    super.interceptCallableChain(callableChain);

    callableChain.add(new ThreadLocalProcessor<>(CURRENT_JMS_MESSAGE, m_jmsMessage));
  }

  @Override
  public JmsRunContext withRunMonitor(final RunMonitor runMonitor) {
    super.withRunMonitor(runMonitor);
    return this;
  }

  @Override
  public JmsRunContext withSubject(final Subject subject) {
    super.withSubject(subject);
    return this;
  }

  @Override
  public JmsRunContext withLocale(final Locale locale) {
    super.withLocale(locale);
    return this;
  }

  @Override
  public JmsRunContext withCorrelationId(final String correlationId) {
    super.withCorrelationId(correlationId);
    return this;
  }

  @Override
  public JmsRunContext withTransactionScope(final TransactionScope transactionScope) {
    super.withTransactionScope(transactionScope);
    return this;
  }

  @Override
  public JmsRunContext withTransaction(final ITransaction transaction) {
    super.withTransaction(transaction);
    return this;
  }

  @Override
  public JmsRunContext withTransactionMember(final ITransactionMember transactionMember) {
    super.withTransactionMember(transactionMember);
    return this;
  }

  @Override
  public JmsRunContext withoutTransactionMembers() {
    super.withoutTransactionMembers();
    return this;
  }

  @Override
  public <THREAD_LOCAL> JmsRunContext withThreadLocal(final ThreadLocal<THREAD_LOCAL> threadLocal, final THREAD_LOCAL value) {
    super.withThreadLocal(threadLocal, value);
    return this;
  }

  @Override
  public JmsRunContext withProperty(final Object key, final Object value) {
    super.withProperty(key, value);
    return this;
  }

  @Override
  public JmsRunContext withProperties(final Map<?, ?> properties) {
    super.withProperties(properties);
    return this;
  }

  @Override
  public JmsRunContext withIdentifier(String id) {
    super.withIdentifier(id);
    return this;
  }

  public Message getJmsMessage() {
    return m_jmsMessage;
  }

  public JmsRunContext withJmsMessage(final Message jmsMessage) {
    m_jmsMessage = jmsMessage;
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.attr("ids", CollectionUtility.format(getIdentifiers()));
    builder.ref("message", getJmsMessage());
    return builder.toString();
  }

  // === fill methods ===

  @Override
  protected void copyValues(final RunContext origin) {
    final JmsRunContext originRunContext = (JmsRunContext) origin;

    super.copyValues(originRunContext);
    m_jmsMessage = originRunContext.m_jmsMessage;
  }

  @Override
  protected void fillCurrentValues() {
    super.fillCurrentValues();
    m_jmsMessage = JmsRunContext.CURRENT_JMS_MESSAGE.get();
  }

  @Override
  protected void fillEmptyValues() {
    super.fillEmptyValues();
    m_jmsMessage = null;
  }

  @Override
  public JmsRunContext copy() {
    final JmsRunContext copy = BEANS.get(JmsRunContext.class);
    copy.copyValues(this);
    return copy;
  }
}
