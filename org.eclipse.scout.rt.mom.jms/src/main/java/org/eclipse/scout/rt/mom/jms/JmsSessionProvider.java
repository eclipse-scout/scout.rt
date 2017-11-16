package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.Topic;

import org.eclipse.scout.rt.mom.api.SubscribeInput;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruption.IRestorer;

public class JmsSessionProvider implements IJmsSessionProvider {

  private final Session m_session;
  private final Destination m_destination; // may be null
  private MessageProducer m_producer;
  private MessageConsumer m_consumer;

  private TemporaryQueue m_temporaryQueue;

  private volatile boolean m_closing;

  public JmsSessionProvider(Session session, Destination destination) {
    m_session = assertNotNull(session);
    m_destination = destination;
  }

  @Override
  public Session getSession() {
    return m_session;
  }

  @Override
  public MessageProducer getProducer() throws JMSException {
    if (m_producer == null) {
      m_producer = createProducer();
    }
    return m_producer;
  }

  protected MessageProducer createProducer() throws JMSException {
    return m_session.createProducer(null);
  }

  @Override
  public MessageConsumer getConsumer(SubscribeInput input) throws JMSException {
    if (m_consumer == null) {
      m_consumer = createConsumer(input);
    }
    return m_consumer;
  }

  protected MessageConsumer createConsumer(SubscribeInput input) throws JMSException {
    boolean noLocal = !input.isLocalReceipt();
    if (m_destination instanceof Topic && input.getDurableSubscriptionName() != null) {
      return m_session.createDurableSubscriber((Topic) m_destination, input.getDurableSubscriptionName(), input.getSelector(), noLocal);
    }
    return m_session.createConsumer(m_destination, input.getSelector(), noLocal);
  }

  @Override
  public TemporaryQueue getTemporaryQueue() throws JMSException {
    if (m_temporaryQueue == null) {
      m_temporaryQueue = m_session.createTemporaryQueue();
    }
    return m_temporaryQueue;
  }

  @Override
  public void deleteTemporaryQueue() throws JMSException {
    Assertions.assertTrue(m_closing, "deleteTemporaryQueue can only be called on a closing session provider.");
    if (m_temporaryQueue != null) {
      m_temporaryQueue.delete();
    }
  }

  @Override
  public boolean isClosing() {
    return m_closing;
  }

  @Override
  public void close() {
    m_closing = true;
    IRestorer interruption = ThreadInterruption.clear(); // Temporarily clear the thread's interrupted status while closing
    try {
      closeImpl();
    }
    catch (JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
    finally {
      interruption.restore();
    }
  }

  protected void closeImpl() throws JMSException {
    if (m_session != null) {
      m_session.close();
    }
  }
}
