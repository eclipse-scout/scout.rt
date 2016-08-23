package org.eclipse.scout.rt.mom.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Topic;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMom;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.FinalValue;

/**
 * Represents a {@link Topic} in the JMS messaging standard.
 *
 * @see IMom
 * @since 6.1
 */
@Bean
public class JmsTopicDestination implements IDestination {

  protected final FinalValue<Topic> m_topic = new FinalValue<>();

  public JmsTopicDestination init(final Topic topic) {
    m_topic.set(Assertions.assertNotNull(topic, "topic must not be null"));
    return this;
  }

  @Override
  public String getName() {
    try {
      return m_topic.get().getTopicName();
    }
    catch (final JMSException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (Destination.class.isAssignableFrom(type)) {
      return (T) m_topic.get();
    }
    return null;
  }

  @Override
  public int hashCode() {
    return m_topic.get().hashCode(); // topic is not null upon initialization
  }

  @Override
  public boolean equals(final Object obj) {
    return m_topic.get().equals(obj); // topic is not null upon initialization
  }

  @Override
  public String toString() {
    return m_topic.get().toString();
  }
}
