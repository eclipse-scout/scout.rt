package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.PROP_REPLY_ID;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.jms.JmsMomImplementor.MomExceptionHandler;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Allows the subscription of a replier to respond to requests sent to a 'request-reply' destination. Requests are
 * acknowledged automatically upon their receipt.
 *
 * @since 6.1
 */
@Bean
public class AutoAcknowledgeReplierStrategy implements IReplierStrategy {

  protected JmsMomImplementor m_mom;
  protected boolean m_singleThreaded;

  public AutoAcknowledgeReplierStrategy init(final JmsMomImplementor mom, final boolean singleThreaded) {
    m_mom = mom;
    m_singleThreaded = singleThreaded;
    return this;
  }

  @Override
  public <REQUEST, REPLY> ISubscription subscribe(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final SubscribeInput input) throws JMSException {
    final Session session = m_mom.getConnection().createSession(false /* non-transacted */, Session.AUTO_ACKNOWLEDGE);
    try {
      installMessageListener(destination, listener, session, input);
      return new JmsSubscription(session, destination);
    }
    catch (JMSException | RuntimeException e) {
      session.close();
      throw e;
    }
  }

  protected <REQUEST, REPLY> void installMessageListener(final IBiDestination<REQUEST, REPLY> destination, final IRequestListener<REQUEST, REPLY> listener, final Session session, final SubscribeInput input) throws JMSException {
    final IMarshaller marshaller = m_mom.lookupMarshaller(destination);
    final RunContext runContext = (input.getRunContext() != null ? input.getRunContext() : RunContexts.empty());

    final MessageConsumer consumer = session.createConsumer(m_mom.lookupJmsDestination(destination, session), input.getSelector());
    consumer.setMessageListener(new JmsMessageListener() {

      @Override
      public void onJmsMessage(final Message jmsRequest) throws JMSException {
        final String replyId = assertNotNull(jmsRequest.getStringProperty(PROP_REPLY_ID), "missing 'replyId' [msg={}]", jmsRequest);

        // Read and process the message asynchronously because JMS session is single-threaded. This allows concurrent message processing.
        // Unlike AutoAcknowledgeSubscriptionStrategy, a job is scheduled for 'single-threaded' mode to support cancellation (execution hint).
        final IFuture<Void> future = Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            final JmsMessageReader<REQUEST> requestReader = JmsMessageReader.newInstance(jmsRequest, marshaller);
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
                    final Message replyMessage = handleRequest(listener, marshaller, request, replyId);
                    m_mom.send(m_mom.getDefaultProducer(), replyTopic, replyMessage, jmsRequest.getJMSDeliveryMode(), jmsRequest.getJMSPriority(), Message.DEFAULT_TIME_TO_LIVE);
                  }
                });
          }
        }, Jobs.newInput()
            .withName("Receiving JMS request [dest={}]", destination)
            .withExceptionHandling(BEANS.get(MomExceptionHandler.class), true)
            .withExecutionHint(replyId)); // Register for cancellation

        if (m_singleThreaded) {
          future.awaitDone();
        }
      }
    });
  }

  /**
   * Delegates the request to the listener, and returns the message to be replied.
   */
  protected <REPLY, REQUEST> Message handleRequest(final IRequestListener<REQUEST, REPLY> listener, final IMarshaller marshaller, final IMessage<REQUEST> request, final String replyId) throws JMSException {
    try {
      final REPLY reply = listener.onRequest(request);
      return JmsMessageWriter.newInstance(m_mom.getDefaultSession(), marshaller)
          .writeTransferObject(reply)
          .writeRequestReplySuccess(true)
          .writeReplyId(replyId)
          .writeCorrelationId(CorrelationId.CURRENT.get())
          .build();
    }
    catch (final Exception e) { // NOSONAR
      BEANS.get(ExceptionHandler.class).handle(e);
      return JmsMessageWriter.newInstance(m_mom.getDefaultSession(), marshaller)
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
    Throwable interceptedThrowable = t;

    // Replace PlatformException to ensure serialization
    if (t instanceof PlatformException) {
      interceptedThrowable = new RuntimeException(t.getMessage());
    }

    // Unset cause and stracktrace (security)
    if (interceptedThrowable.getCause() == t) {
      interceptedThrowable.initCause(null);
    }
    interceptedThrowable.setStackTrace(new StackTraceElement[0]);

    return interceptedThrowable;
  }
}
