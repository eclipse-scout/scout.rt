/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.jms;

import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMessageConsumerJob<DTO> implements IRunnable {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractMessageConsumerJob.class);

  protected final JmsMomImplementor m_mom;
  protected final IJmsSessionProvider m_sessionProvider;
  protected final IDestination<DTO> m_destination;
  protected final SubscribeInput m_subscribeInput;
  protected final MessageConsumer m_consumer;
  protected final IMarshaller m_marshaller;

  public AbstractMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input) throws JMSException {
    m_mom = mom;
    m_sessionProvider = sessionProvider;
    m_destination = destination;
    m_subscribeInput = input;
    m_consumer = sessionProvider.getConsumer(input);
    m_marshaller = mom.resolveMarshaller(destination);
  }

  protected boolean isSingleThreaded() {
    return SubscribeInput.ACKNOWLEDGE_AUTO_SINGLE_THREADED == m_subscribeInput.getAcknowledgementMode();
  }

  protected boolean isTransacted() {
    return SubscribeInput.ACKNOWLEDGE_TRANSACTED == m_subscribeInput.getAcknowledgementMode();
  }

  @Override
  public void run() throws Exception {
    while (true) {
      if (m_sessionProvider.isClosing() || IFuture.CURRENT.get().isCancelled()) {
        LOG.debug("JMS MessageConsumer for {} was closed", m_destination);
        break;
      }
      Message message = m_consumer.receive();
      if (message == null) {
        // consumer closed
        break;
      }

      m_mom.getMessageHandler().handleIncoming(m_destination, message, m_marshaller);
      try {
        onJmsMessage(message);
      }
      catch (Exception e) {
        BEANS.get(MomExceptionHandler.class).handle(e);
      }
    }

    LOG.debug("JMS MessageConsumer for {} was closed", m_destination);
  }

  protected RunContext createRunContext() throws JMSException {
    RunContext runContext = (m_subscribeInput.getRunContext() != null ? m_subscribeInput.getRunContext().copy() : RunContexts.empty());
    if (isTransacted()) {
      runContext.withTransactionMember(BEANS.get(JmsTransactionMember.class)
          .withMemberId(UUID.randomUUID().toString())
          .withSessionProvider(m_sessionProvider)
          .withAutoClose(false));
    }
    return runContext
        .withTransactionScope(TransactionScope.REQUIRES_NEW)
        .withDiagnostics(BEANS.all(IJmsRunContextDiagnostics.class));
  }

  protected abstract void onJmsMessage(Message jmsMessage) throws JMSException;
}
