/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_REPLY_ID;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;

import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;

public class ReplyMessageConsumerJob<REQUEST, REPLY> extends AbstractMessageConsumerJob<REQUEST> {
  protected final IRequestListener<REQUEST, REPLY> m_listener;

  public ReplyMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input) {
    this(mom, sessionProvider, destination, listener, input, 0L);
  }

  public ReplyMessageConsumerJob(JmsMomImplementor mom, IJmsSessionProvider sessionProvider, IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input, long receiveTimeout) {
    super(mom, sessionProvider, destination, input, receiveTimeout);
    m_listener = listener;
  }

  @Override
  protected void onJmsMessage(final Message jmsRequest) throws JMSException {
    final String replyId = assertNotNull(jmsRequest.getStringProperty(JMS_PROP_REPLY_ID), "missing 'replyId' [msg={}]", jmsRequest);

    // Read and process the message asynchronously because JMS session is single-threaded. This allows concurrent message processing.
    // Unlike AutoAcknowledgeSubscriptionStrategy, a job is scheduled for 'single-threaded' mode to support cancellation (execution hint).
    final IFuture<Void> future = Jobs.schedule(() -> handleMessageInRunContext(jmsRequest, replyId), m_mom.newJobInput()
        .withName("Receiving JMS message [dest={}]", m_destination)
        .withExecutionHint(replyId)); // Register for cancellation

    if (isSingleThreaded()) {
      future.awaitDone();
    }
  }

  @Override
  protected RunContext createRunContext() throws JMSException {
    return super.createRunContext()
        .withRunMonitor(RunMonitor.CURRENT.get()); // associate with the calling monitor to propagate cancellation
  }

  protected void handleMessageInRunContext(final Message jmsRequest, final String replyId) throws JMSException {
    final JmsMessageReader<REQUEST> messageReader = JmsMessageReader.newInstance(jmsRequest, m_marshaller);
    final IMessage<REQUEST> request = messageReader.readMessage();
    final Destination replyTopic = messageReader.readReplyTo();
    final String correlationId = messageReader.readCorrelationId();

    createRunContext()
        .withCorrelationId(correlationId)
        .withThreadLocal(IMessage.CURRENT, request)
        .run(() -> {
          try {
            handleRequest(jmsRequest, request, replyId, replyTopic);
          }
          catch (Exception e) {
            throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
                .withContextInfo("correlationId", correlationId);
          } finally {
            onMessageConsumptionComplete();
          }
        });
  }

  /**
   * Delegates the request to the listener, and returns the message to be replied.
   */
  protected void handleRequest(Message jmsRequest, IMessage<REQUEST> request, String replyId, Destination replyTopic) throws JMSException {
    Object transferObject = null;
    boolean success = true;
    try {
      transferObject = m_listener.onRequest(request);
    }
    catch (Throwable t) { // NOSONAR (Always send a response, even if a PlatformError is thrown. Otherwise the caller might wait forever.)
      BEANS.get(ExceptionHandler.class).handle(t);

      transferObject = interceptRequestReplyException(t);
      success = false;
    }

    if (IFuture.CURRENT.get().isCancelled()) {
      return;
    }

    IRestorer interruption = ThreadInterruption.clear(); // Temporarily clear the thread's interrupted status while sending a message.
    IJmsSessionProvider sessionProvider = m_mom.createSessionProvider();
    try {
      JmsMessageWriter replyMessageWriter = JmsMessageWriter.newInstance(sessionProvider.getSession(), m_marshaller)
          .writeReplyId(replyId)
          .writeTransferObject(transferObject)
          .writeRequestReplySuccess(success);
      m_mom.send(sessionProvider.getProducer(), replyTopic, replyMessageWriter, jmsRequest.getJMSDeliveryMode(), jmsRequest.getJMSPriority(), Message.DEFAULT_TIME_TO_LIVE);
    }
    finally {
      sessionProvider.close();
      interruption.restore();
    }
  }

  /**
   * Allows to intercept the exception if request processing failed.
   */
  protected Throwable interceptRequestReplyException(Throwable t) {
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
