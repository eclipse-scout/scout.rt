/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
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
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.mom.jms.internal.JmsSessionProviderMigration;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
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
  protected final IMarshaller m_marshaller;
  protected final long m_receiveTimeoutMillis;

  /**
   * @param mom
   * @param sessionProvider
   * @param destination
   * @param input
   */
  public AbstractMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input) {
    this(mom, sessionProvider, destination, input, 0L);
  }

  /**
   * @param mom
   * @param sessionProvider
   * @param destination
   * @param input
   * @param receiveTimeoutMillis
   *          in milliseconds, 0 for no timeout
   */
  public AbstractMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, SubscribeInput input, long receiveTimeoutMillis) {
    m_mom = mom;
    m_sessionProvider = sessionProvider;
    m_destination = destination;
    m_subscribeInput = input;
    m_marshaller = mom.resolveMarshaller(destination);
    m_receiveTimeoutMillis = receiveTimeoutMillis;
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
      if (IFuture.CURRENT.get().isCancelled() || m_sessionProvider.isClosing()) {
        LOG.debug("JMS MessageConsumer for {} was closed", m_destination);
        break;
      }

      Message message;
      try {
        message = JmsSessionProviderMigration.receive(m_sessionProvider, m_subscribeInput, m_receiveTimeoutMillis);
        if (message == null) {
          // consumer closed or connection failure, go to start of while loop
          continue;
        }
      }
      catch (Exception e) {
        if (IFuture.CURRENT.get().isCancelled() || m_sessionProvider.isClosing()) {
          LOG.debug("JMS MessageConsumer for {} was closed", m_destination);
          break;
        }
        BEANS.get(MomExceptionHandler.class).handle(e);
        continue;
      }

      try {
        LOG.debug("Receiving JMS message [message={}]", message);
        onJmsMessage(message);
      }
      catch (Exception e) {
        if (isRollbackNecessary(e)) {
          Session transactedSession = null;
          try {
            transactedSession = m_sessionProvider.getSession();
            transactedSession.rollback();
          }
          catch (final JMSException ex) {
            LOG.error("Failed to rollback transacted session [session={}]", transactedSession, ex);
          }
        }
        BEANS.get(MomExceptionHandler.class).handle(e);
      }
    }
    LOG.debug("JMS MessageConsumer for {} was closed", m_destination);
  }

  /**
   * Make sure that a scout JMS-Transaction-Member that is being initialized and not yet attached to the
   * {@link ITransaction} is rollbacked in case of error.
   * <p>
   * Once the scout JMS-Transaction-Member is registered it will safely be rollbacked on errors.
   */
  protected boolean isRollbackNecessary(Exception e) {
    return isTransacted() && !(e instanceof PlatformException);
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
