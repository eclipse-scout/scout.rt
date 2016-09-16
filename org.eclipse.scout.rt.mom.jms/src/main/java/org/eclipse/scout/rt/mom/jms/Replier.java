package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.PROP_REPLY_ID;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.security.GeneralSecurityException;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Allows the subscription of a replier to respond to requests sent to a 'request-reply' destination.
 *
 * @since 6.1
 */
@Bean
public class Replier {

  protected JmsMomImplementor m_mom;

  public Replier init(final JmsMomImplementor mom) {
    m_mom = mom;
    return this;
  }

  /**
   * Registers a replier to respond to requests sent to the specified 'request-reply' destination.
   */
  public <REQUEST, REPLY> ISubscription subscribe(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final RunContext runContext) throws JMSException {
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

  protected <REQUEST, REPLY> void installMessageListener(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final RunContext runContext, final Session session) throws JMSException {
    final IMarshaller marshaller = m_mom.lookupMarshaller(destination);
    final IEncrypter encrypter = m_mom.lookupEncrypter(destination);

    final MessageConsumer consumer = session.createConsumer(m_mom.lookupJmsDestination(destination, session));
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsRequest) throws JMSException, GeneralSecurityException {
        final String replyId = assertNotNull(jmsRequest.getStringProperty(PROP_REPLY_ID), "missing 'replyId' [msg={}]", jmsRequest);

        // Read and process the message asynchronously because JMS session is single-threaded. This allows concurrent message processing.
        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            final JmsMessageReader<REQUEST> requestReader = JmsMessageReader.newInstance(jmsRequest, marshaller, encrypter);
            final IMessage<REQUEST> request = requestReader.readMessage();
            final Destination replyTopic = requestReader.readReplyTo();

            runContext.copy()
                .withCorrelationId(requestReader.readCorrelationId())
                .withThreadLocal(IMessage.CURRENT, request)
                .withTransactionScope(TransactionScope.REQUIRES_NEW)
                .withDiagnostics(BEANS.all(IJmsRunContextDiagnostics.class))
                .run(new IRunnable() {

                  @Override
                  public void run() throws Exception {
                    final Message replyMessage = handleRequest(listener, marshaller, encrypter, request, replyId);
                    m_mom.send(m_mom.getDefaultProducer(), replyTopic, replyMessage, jmsRequest.getJMSDeliveryMode(), jmsRequest.getJMSPriority(), Message.DEFAULT_TIME_TO_LIVE);
                  }
                });
          }
        }, Jobs.newInput()
            .withName("Receiving JMS request [dest={}]", destination)
            .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true)
            .withExecutionHint(replyId)); // Register for cancellation
      }
    });
  }

  /**
   * Delegates the request to the listener, and returns the message to be replied.
   */
  protected <REPLY, REQUEST> Message handleRequest(final IRequestListener<REQUEST, REPLY> listener, final IMarshaller marshaller, final IEncrypter encrypter, final IMessage<REQUEST> request, final String replyId)
      throws JMSException, GeneralSecurityException {
    try {
      final REPLY reply = listener.onRequest(request);
      return JmsMessageWriter.newInstance(m_mom.getDefaultSession(), marshaller, encrypter)
          .writeTransferObject(reply)
          .writeRequestReplySuccess(true)
          .writeReplyId(replyId)
          .writeCorrelationId(CorrelationId.CURRENT.get())
          .build();
    }
    catch (final Exception e) { // NOSONAR
      BEANS.get(ExceptionHandler.class).handle(e);
      return JmsMessageWriter.newInstance(m_mom.getDefaultSession(), marshaller, encrypter)
          .writeTransferObject(interceptRequestReplyException(e))
          .writeRequestReplySuccess(false)
          .writeReplyId(replyId)
          .writeCorrelationId(CorrelationId.CURRENT.get())
          .build();
    }
  }

  /**
   * Allows to intercept the exception if request processing failed.
   */
  protected Throwable interceptRequestReplyException(final Throwable t) {
    // Unset cause and stracktrace (security)
    if (t.getCause() == t) {
      t.initCause(null);
    }
    t.setStackTrace(new StackTraceElement[0]);

    // Unset cause in status to break recursion
    if (t instanceof ProcessingException) {
      ((ProcessingStatus) ((ProcessingException) t).getStatus()).setException(null);
    }

    return t;
  }
}
