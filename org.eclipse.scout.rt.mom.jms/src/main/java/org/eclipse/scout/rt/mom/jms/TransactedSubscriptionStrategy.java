package org.eclipse.scout.rt.mom.jms;

import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;

/**
 * Messages are acknowledged upon successful commit of the receiving transaction. While processing a message, this
 * subscription cannot process another message sent to its destination.
 *
 * @see IMom#ACKNOWLEDGE_TRANSACTED
 * @since 6.1
 */
@Bean
public class TransactedSubscriptionStrategy implements ISubscriptionStrategy {

  protected JmsMomImplementor m_mom;

  public ISubscriptionStrategy init(final JmsMomImplementor mom) {
    m_mom = mom;
    return this;
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) throws JMSException {
    final Session transactedSession = m_mom.getConnection().createSession(true, Session.SESSION_TRANSACTED);
    try {
      installMessageConsumer(destination, listener, transactedSession, input);
      return new JmsSubscription(transactedSession, destination);
    }
    catch (JMSException | RuntimeException e) {
      transactedSession.close();
      throw e;
    }
  }

  protected <DTO> void installMessageConsumer(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final Session transactedSession, final SubscribeInput input) throws JMSException {
    final IMarshaller marshaller = m_mom.resolveMarshaller(destination);
    final RunContext runContext = (input.getRunContext() != null ? input.getRunContext() : RunContexts.empty());

    final MessageConsumer consumer = createConsumer(transactedSession, destination, input);
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsMessage) throws JMSException {
        // Do not process asynchronously due to transacted acknowledgment.
        // This guarantees that messages do not arrive concurrently, which is required to commit or rollback a single message.

        final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, marshaller);
        final IMessage<DTO> message = messageReader.readMessage();

        runContext.copy()
            .withCorrelationId(messageReader.readCorrelationId())
            .withThreadLocal(IMessage.CURRENT, message)
            .withTransactionScope(TransactionScope.REQUIRES_NEW)
            .withTransactionMember(BEANS.get(JmsTransactionMember.class)
                .withMemberId(UUID.randomUUID().toString())
                .withTransactedSession(transactedSession)
                .withAutoClose(false))
            .withDiagnostics(BEANS.all(IJmsRunContextDiagnostics.class))
            .run(() -> handleMessage(listener, message));
      }
    });
  }

  protected <DTO> MessageConsumer createConsumer(final Session session, final IDestination<DTO> destination, final SubscribeInput input) throws JMSException {
    final Destination jmsDestination = m_mom.resolveJmsDestination(destination, session);
    final boolean noLocal = !input.isLocalReceipt();
    if (jmsDestination instanceof Topic && input.getDurableSubscriptionName() != null) {
      return session.createDurableSubscriber((Topic) jmsDestination, input.getDurableSubscriptionName(), input.getSelector(), noLocal);
    }
    return session.createConsumer(jmsDestination, input.getSelector(), noLocal);
  }

  /**
   * Method invoked upon the receipt of a message.
   */
  protected <DTO> void handleMessage(final IMessageListener<DTO> listener, final IMessage<DTO> message) {
    listener.onMessage(message);
  }
}
