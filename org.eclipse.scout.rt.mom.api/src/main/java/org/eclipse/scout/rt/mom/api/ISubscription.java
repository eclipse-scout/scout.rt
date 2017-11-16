package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.util.IDisposable;

/**
 * Represents a subscription to a destination.
 * <p>
 * Use this object to unsubscribe from the destination.
 *
 * @see IMom
 * @since 6.1
 */
public interface ISubscription extends IDisposable {

  /**
   * Returns the destination this subscription belongs to.
   */
  IDestination<?> getDestination();

  /**
   * {@inheritDoc}
   * <p>
   * In case of single threaded subscription, the call to this method blocks until any ongoing processing of this
   * subscription is finished.
   */
  @Override
  void dispose();
}
