package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a {@link MessageConsumer} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
public class JmsSubscription implements ISubscription {

  private static final Logger LOG = LoggerFactory.getLogger(JmsSubscription.class);

  private final Session m_session;
  private final MessageConsumer m_consumer;
  private final IDestination m_destination;

  public JmsSubscription(final MessageConsumer consumer, final IDestination destination) {
    m_consumer = consumer;
    m_destination = destination;
    m_session = null;
  }

  public JmsSubscription(final Session session, final IDestination destination) {
    m_session = session;
    m_destination = destination;
    m_consumer = null;
  }

  @Override
  public IDestination getDestination() {
    return m_destination;
  }

  @Override
  public void dispose() {
    if (m_consumer != null) {
      try {
        m_consumer.close();
      }
      catch (final JMSException e) {
        LOG.warn("Failed to close consumer", m_consumer, e);
      }
    }

    if (m_session != null) {
      try {
        m_session.close();
      }
      catch (final JMSException e) {
        LOG.warn("Failed to close session", m_consumer, e);
      }
    }
  }
}
