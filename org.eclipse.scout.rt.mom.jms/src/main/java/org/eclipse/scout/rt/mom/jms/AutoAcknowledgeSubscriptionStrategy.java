package org.eclipse.scout.rt.mom.jms;

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
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Messages are acknowledged automatically upon their receipt. This strategy has less footprint than
 * {@link TransactedSubscriptionStrategy}, and allows for serial and concurrent message processing.
 *
 * @see IMom#ACKNOWLEDGE_AUTO
 * @see IMom#ACKNOWLEDGE_AUTO_SINGLE_THREADED
 * @since 6.1
 */
@Bean
public class AutoAcknowledgeSubscriptionStrategy implements ISubscriptionStrategy {

  protected JmsMomImplementor m_mom;
  protected boolean m_singleThreaded;

  public ISubscriptionStrategy init(final JmsMomImplementor mom, final boolean singleThreaded) {
    m_mom = mom;
    m_singleThreaded = singleThreaded;
    return this;
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final SubscribeInput input) throws JMSException {
    final Session session = m_mom.getConnection().createSession(false /* non-transacted */, Session.AUTO_ACKNOWLEDGE);
    try {
      installMessageConsumer(destination, listener, session, input);
      return new JmsSubscription(session, destination);
    }
    catch (JMSException | RuntimeException e) {
      session.close();
      throw e;
    }
  }

  protected <DTO> void installMessageConsumer(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final Session session, final SubscribeInput input) throws JMSException {
    final IMarshaller marshaller = m_mom.resolveMarshaller(destination);
    final RunContext runContext = (input.getRunContext() != null ? input.getRunContext() : RunContexts.empty());

    final MessageConsumer consumer = createConsumer(session, destination, input);
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsMessage) throws Exception {
        final IRunnable runnable = () -> {
          final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, marshaller);
          final IMessage<DTO> message = messageReader.readMessage();

          runContext.copy()
              .withCorrelationId(messageReader.readCorrelationId())
              .withThreadLocal(IMessage.CURRENT, message)
              .withTransactionScope(TransactionScope.REQUIRES_NEW)
              .withDiagnostics(BEANS.all(IJmsRunContextDiagnostics.class))
              .run(() -> handleMessage(listener, message));
        };

        if (m_singleThreaded) {
          runnable.run();
        }
        else {
          Jobs.schedule(runnable, Jobs.newInput()
              .withName("Receiving JMS message [dest={}]", destination)
              .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true));
        }
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
