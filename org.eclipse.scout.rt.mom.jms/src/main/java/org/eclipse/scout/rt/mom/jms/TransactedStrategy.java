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
 * <p>
 *
 * @see IMom#ACKNOWLEDGE_TRANSACTED
 * @since 6.1
 */
@Bean
public class TransactedStrategy implements ISubscriptionStrategy {

  private volatile JmsMom m_mom;

  public ISubscriptionStrategy init(final JmsMom mom) {
    m_mom = mom;
    return this;
  }

  @Override
  public <TRANSFER_OBJECT> ISubscription subscribe(final IDestination destination, final IMessageListener<TRANSFER_OBJECT> listener, final RunContext runContext) throws JMSException {
    final IMarshaller marshaller = m_mom.lookupMarshaller(destination);
    final IEncrypter encrypter = m_mom.lookupEncrypter(destination);

    final Session transactedSession = m_mom.getConnection().createSession(true, Session.SESSION_TRANSACTED);
    final MessageConsumer consumer = transactedSession.createConsumer(m_mom.toJmsDestination(destination));
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsMessage) throws JMSException, GeneralSecurityException {
        final JmsMessageReader<TRANSFER_OBJECT> messageReader = JmsMessageReader.newInstance(jmsMessage, marshaller, encrypter);
        final IMessage<TRANSFER_OBJECT> message = messageReader.readMessage();

        runContext
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
            listener.onMessage(message);
          }
        });
      }
    });
    return new JmsSubscription(transactedSession, destination);
  }
}
