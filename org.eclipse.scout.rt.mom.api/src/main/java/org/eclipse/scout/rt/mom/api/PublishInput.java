package org.eclipse.scout.rt.mom.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * Specifies how to publish a message.
 *
 * @see IMom
 * @since 6.1
 */
@Bean
public class PublishInput {

  /**
   * Indicates an infinite timeout.
   */
  public static final long INFINITELY = -1;

  /**
   * Indicates the default messaging priority.
   */
  public static final int PRIORITY_NORMAL = 0;

  /**
   * Instructs the MOM implementor to not persist the message to stable storage, which may result in a message loss in
   * case the dispatcher crashes.
   */
  public static final int DELIVERY_MODE_NON_PERSISTENT = 1;

  /**
   * Instructs the MOM implementor to temporarily persist the message to stable storage until acknowledged by the
   * consumer. This guarantees the message to be delivered even if the dispatcher crashes.
   */
  public static final int DELIVERY_MODE_PERSISTENT = 2;

  private final Map<String, String> m_properties = new HashMap<>();

  private int m_deliveryMode = PublishInput.DELIVERY_MODE_PERSISTENT;
  private int m_priority = PublishInput.PRIORITY_NORMAL;
  private long m_timeToLive = PublishInput.INFINITELY;
  private long m_requestReplyTimeout = PublishInput.INFINITELY;
  private boolean m_transactional = false;

  public int getDeliveryMode() {
    return m_deliveryMode;
  }

  /**
   * Specifies the message delivery mode.
   *
   * @see #DELIVERY_MODE_PERSISTENT
   * @see #DELIVERY_MODE_NON_PERSISTENT
   */
  public PublishInput withDeliveryMode(final int deliveryMode) {
    m_deliveryMode = deliveryMode;
    return this;
  }

  public Integer getPriority() {
    return m_priority;
  }

  /**
   * Specifies the priority level for this message. A higher values indicates a higher precedence.
   *
   * @see #PRIORITY_NORMAL
   */
  public PublishInput withPriority(final int priority) {
    m_priority = priority;
    return this;
  }

  public long getTimeToLive() {
    return m_timeToLive;
  }

  /**
   * Specifies the maximal time-to-live until a message is discarded if not consumed yet. By default, a message never
   * expires. A time-to-live is only useful for queue-based messaging.
   */
  public PublishInput withTimeToLive(final long timeToLive, final TimeUnit unit) {
    m_timeToLive = unit.toMillis(timeToLive);
    return this;
  }

  /**
   * Returns the timeout in milliseconds.
   */
  public long getRequestReplyTimeout() {
    return m_requestReplyTimeout;
  }

  /**
   * Specifies the maximal time to block the requester in synchronous 'request-reply' communication until a reply is
   * received. If elapsed, a {@link TimedOutError} is thrown and a cancellation request sent to the consumer.
   */
  public PublishInput withRequestReplyTimeout(final long timeout, final TimeUnit unit) {
    m_requestReplyTimeout = unit.toMillis(timeout);
    return this;
  }

  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(m_properties);
  }

  /**
   * Sets a property to be included as message property.
   */
  public PublishInput withProperty(final String name, final String value) {
    m_properties.put(name, value);
    return this;
  }

  public boolean isTransactional() {
    return m_transactional;
  }

  /**
   * Instruments the MOM implementor to publish the message only upon successful commit of the current transaction. The
   * current transaction is defined as the current transaction when publishing the message. In case of a transaction
   * rollback, the message is not sent and discarded instead.
   * <p>
   * This mode is not applicable for 'request-reply' communication.
   */
  public PublishInput withTransactional(final boolean transactional) {
    m_transactional = transactional;
    return this;
  }
}
