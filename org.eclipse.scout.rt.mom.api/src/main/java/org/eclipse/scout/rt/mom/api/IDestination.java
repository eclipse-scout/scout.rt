package org.eclipse.scout.rt.mom.api;

/**
 * Represents the target for messages a client produces and the source of messages it consumes. In point-to-point
 * messaging, destinations are called <code>queues</code>, and in publish/subscribe messaging, destinations are called
 * <code>topics</code>.
 * <p>
 * A destination with the same <i>name</i> and <i>type</i> are considered 'equals'. This is a lightweight object with no
 * physical messaging resources allocated, and which can be constructed even if not connected to the network or broker,
 * e.g. in static initialization sections.
 * <p>
 * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
 *
 * @see IMom
 * @since 6.1
 */
public interface IDestination<DTO> {

  /**
   * Represents a topic for <i>publish/subscribe messaging</i>.
   * <p>
   * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
   */
  int TOPIC = 1;
  /**
   * Represents a queue for <i>point-to-point messaging</i>.
   * <p>
   * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
   */
  int QUEUE = 2;

  /**
   * Represents a destination which is looked up via JNDI.
   */
  int JNDI_LOOKUP = 3;

  /**
   * Returns the symbolic name of this destination.
   */
  String getName();

  /**
   * Returns the type this destination represents.
   * 
   * @see #TOPIC
   * @see #QUEUE
   * @see #JNDI_LOOKUP
   */
  int getType();
}
