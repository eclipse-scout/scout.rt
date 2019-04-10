package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

public class MessageConsumerJob<DTO> extends AbstractMessageConsumerJob<DTO> {
  protected final IMessageListener<DTO> m_listener;

  public MessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) {
    this(mom, sessionProvider, destination, listener, input, 0L);
  }

  public MessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input, long receiveTimeout) {
    super(mom, sessionProvider, destination, input, receiveTimeout);
    m_listener = listener;
  }

  @Override
  protected void onJmsMessage(final Message jmsMessage) throws JMSException {
    if (isSingleThreaded() || isTransacted()) {
      handleMessageInRunContext(jmsMessage);
    }
    else {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          handleMessageInRunContext(jmsMessage);
        }
      }, m_mom.newJobInput().withName("Receiving JMS message [dest={}]", m_destination));
    }
  }

  protected void handleMessageInRunContext(final Message jmsMessage) throws JMSException {
    final JmsMessageReader<DTO> messageReader = JmsMessageReader.newInstance(jmsMessage, m_marshaller);
    final IMessage<DTO> message = messageReader.readMessage();
    final String correlationId = messageReader.readCorrelationId();

    createRunContext()
        .withCorrelationId(correlationId)
        .withThreadLocal(IMessage.CURRENT, message)
        .run(new IRunnable() {

          @Override
          public void run() throws Exception {
            try {
              m_listener.onMessage(message);
            }
            catch (Exception e) {
              throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
                  .withContextInfo("correlationId", correlationId);
            }
          }
        });
  }
}
