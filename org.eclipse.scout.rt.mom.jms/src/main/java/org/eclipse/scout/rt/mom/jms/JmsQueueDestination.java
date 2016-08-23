package org.eclipse.scout.rt.mom.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.FinalValue;

/**
 * Represents a {@link Queue} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
@Bean
public class JmsQueueDestination implements IDestination {

  protected final FinalValue<Queue> m_queue = new FinalValue<>();

  public JmsQueueDestination init(final Queue queue) {
    m_queue.set(Assertions.assertNotNull(queue, "queue must not be null"));
    return this;
  }

  @Override
  public String getName() {
    try {
      return m_queue.get().getQueueName();
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (Destination.class.isAssignableFrom(type)) {
      return (T) m_queue.get();
    }
    return null;
  }

  @Override
  public int hashCode() {
    return m_queue.get().hashCode(); // queue is not null upon initialization
  }

  @Override
  public boolean equals(final Object obj) {
    return m_queue.get().equals(obj); // queue is not null upon initialization
  }

  @Override
  public String toString() {
    return m_queue.get().toString();
  }
}
