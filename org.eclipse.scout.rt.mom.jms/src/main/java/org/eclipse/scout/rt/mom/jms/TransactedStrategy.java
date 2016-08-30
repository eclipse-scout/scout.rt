package org.eclipse.scout.rt.mom.jms;

import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Messages are acknowledged upon successful commit of the receiving transaction. While processing a message, this
 * subscription cannot process another message sent to its destination.
 *
 * @see IMom#ACKNOWLEDGE_TRANSACTED
 * @since 6.1
 */
@Bean
public class TransactedStrategy implements ISubscriptionStrategy {

  protected JmsMom m_mom;

  public ISubscriptionStrategy init(final JmsMom mom) {
    m_mom = mom;
    return this;
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext) throws JMSException {
    final Session transactedSession = m_mom.getConnection().createSession(true, Session.SESSION_TRANSACTED);
    try {
      installMessageListener(destination, listener, runContext, transactedSession);
      return new JmsSubscription(transactedSession, destination);
    }
    catch (JMSException | RuntimeException e) {
      transactedSession.close();
      throw e;
    }
  }

  protected <DTO> void installMessageListener(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext, final Session transactedSession) throws JMSException {
    final IMarshaller marshaller = m_mom.lookupMarshaller(destination);
    final IEncrypter encrypter = m_mom.lookupEncrypter(destination);

    final MessageConsumer consumer = transactedSession.createConsumer(m_mom.lookupJmsDestination(destination, transactedSession));
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsMessage) throws JMSException, GeneralSecurityException {
        // Do not process asynchronously due to transacted acknowledgment.
        // This guarantees that messages do not arrive concurrently, which is required to commit or rollback a single message.

        final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, marshaller, encrypter);
        final IMessage<DTO> message = messageReader.readMessage();

        runContext.copy()
            .withCorrelationId(messageReader.readCorrelationId())
            .withThreadLocal(IMessage.CURRENT, message)
            .withTransactionScope(TransactionScope.REQUIRES_NEW)
            .withTransactionMember(BEANS.get(JmsTransactionMember.class)
                .withMemberId(UUID.randomUUID().toString())
                .withTransactedSession(transactedSession)
                .withAutoClose(false))
            .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            handleMessage(listener, message);
          }
        });
      }
    });
  }

  /**
   * Method invoked upon the receipt of a message.
   */
  protected <DTO> void handleMessage(final IMessageListener<DTO> listener, final IMessage<DTO> message) {
    listener.onMessage(message);
  }
}
