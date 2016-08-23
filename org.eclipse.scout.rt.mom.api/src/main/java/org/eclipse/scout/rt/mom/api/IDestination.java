package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Represents the target for messages a client produces and the source of messages it consumes. In point-to-point
 * messaging, destinations are called <code>queues</code>, and in publish/subscribe messaging, destinations are called
 * <code>topics</code>.
 * <p>
 * See {@link IMom} documentation for more information about the difference between topic and queue based messaging
 *
 * @see IMom
 * @since 6.1
 */
public interface IDestination extends IAdaptable {

  /**
   * The physical name of this destination.
   */
  String getName();
}
