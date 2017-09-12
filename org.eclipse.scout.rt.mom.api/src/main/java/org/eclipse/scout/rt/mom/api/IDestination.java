package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.ObjectUtility;

/**
 * Represents the target for messages a client produces and the source of messages it consumes. In point-to-point
 * messaging, destinations are called <code>queues</code>, and in publish/subscribe messaging, destinations are called
 * <code>topics</code>.
 * <p>
 * This is a lightweight object with no physical messaging resources allocated, and which can be constructed even if not
 * connected to the network or broker, e.g. in static initialization sections.
 * <p>
 * Two destinations with the same <i>name</i> are considered 'equals'.
 * <p>
 * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
 *
 * @see IMom
 * @since 6.1
 */
public interface IDestination<DTO> {

  /**
   * Returns the symbolic name of this destination.
   */
  String getName();

  /**
   * Returns the type this destination represents.
   */
  IDestinationType getType();

  /**
   * Returns the method how this destination is resolved.
   */
  IResolveMethod getResolveMethod();

  /**
   * Represents the nature and the semantics of the destination.
   * <p>
   * By default, the following destination types are supported:
   * <ul>
   * <li>{@link DestinationType#QUEUE}
   * <li>{@link DestinationType#TOPIC}
   * </ul>
   */
  interface IDestinationType {
  }

  enum DestinationType implements IDestinationType {

    /**
     * Represents a queue for <i>point-to-point messaging</i>.
     * <p>
     * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
     */
    QUEUE,

    /**
     * Represents a topic for <i>publish/subscribe messaging</i>.
     * <p>
     * See {@link IMom} documentation for more information about the difference between topic and queue based messaging.
     */
    TOPIC
  }

  /**
   * Describes the method how the MOM resolves the actual destination from the light-weight {@link IDestination} object.
   * The exact method depends on the actual MOM implementor.
   * <p>
   * By default, the following resolve methods are supported:
   * <ul>
   * <li>{@link ResolveMethod#JNDI}
   * <li>{@link ResolveMethod#DEFINE}
   * </ul>
   */
  @FunctionalInterface
  interface IResolveMethod {

    String getIdentifier();
  }

  enum ResolveMethod implements IResolveMethod {

    /**
     * Destination is "defined" by the MOM implementor (e.g. created on-the-fly).
     */
    DEFINE("define"),

    /**
     * Destination is looked up via JNDI.
     */
    JNDI("jndi");

    private final String m_identifier;

    ResolveMethod(String identifier) {
      m_identifier = identifier;
    }

    @Override
    public String getIdentifier() {
      return m_identifier;
    }

    /**
     * @return the enum value with the given <code>identifier</code>, or <code>null</code> if the identifier is unknown.
     */
    public static IResolveMethod parse(String identifier) {
      for (ResolveMethod value : values()) {
        if (ObjectUtility.equals(identifier, value.getIdentifier())) {
          return value;
        }
      }
      return null;
    }
  }
}
