package org.eclipse.scout.rt.mom.jms;

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
import org.eclipse.scout.rt.mom.jms.JmsMom.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Messages are acknowledged automatically upon their receipt. This strategy has less footprint than
 * {@link TransactedStrategy}, and allows for serial and concurrent message processing.
 *
 * @see IMom#ACKNOWLEDGE_AUTO
 * @see IMom#ACKNOWLEDGE_AUTO_SINGLE_THREADED
 * @since 6.1
 */
@Bean
public class AutoAcknowledgeStrategy implements ISubscriptionStrategy {

  protected JmsMom m_mom;
  protected boolean m_singleThreaded;

  public ISubscriptionStrategy init(final JmsMom mom, final boolean singleThreaded) {
    m_mom = mom;
    m_singleThreaded = singleThreaded;
    return this;
  }

  @Override
  public <DTO> ISubscription subscribe(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext) throws JMSException {
    final Session session = m_mom.getConnection().createSession(false /* non-transacted */, Session.AUTO_ACKNOWLEDGE);
    try {
      installMessageListener(destination, listener, runContext, session);
      return new JmsSubscription(session, destination);
    }
    catch (JMSException | RuntimeException e) {
      session.close();
      throw e;
    }
  }

  protected <DTO> void installMessageListener(final IDestination<DTO> destination, final IMessageListener<DTO> listener, final RunContext runContext, final Session session) throws JMSException {
    final IMarshaller marshaller = m_mom.lookupMarshaller(destination);
    final IEncrypter encrypter = m_mom.lookupEncrypter(destination);

    final MessageConsumer consumer = session.createConsumer(m_mom.lookupJmsDestination(destination, session));
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsMessage) throws Exception {
        final IRunnable runnable = new IRunnable() {

          @Override
          public void run() throws Exception {
            final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, marshaller, encrypter);
            final IMessage<DTO> message = messageReader.readMessage();

            runContext.copy()
                .withCorrelationId(messageReader.readCorrelationId())
                .withThreadLocal(IMessage.CURRENT, message)
                .withTransactionScope(TransactionScope.REQUIRES_NEW)
                .run(new IRunnable() {

              @Override
              public void run() throws Exception {
                handleMessage(listener, message);
              }
            });
          }
        };

        if (m_singleThreaded) {
          runnable.run();
        }
        else {
          Jobs.schedule(runnable, Jobs.newInput()
              .withName("Receiving JMS message [msg={}]", jmsMessage)
              .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true));
        }
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
