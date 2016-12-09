package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;

/**
 * Strategy to subscribe for messages.
 *
 * @since 6.1
 */
public interface ISubscriptionStrategy {

  /**
   * Subscribes for messages sent to the given destination.
   *
   * @param destination
   *          specifies the target of the subscription; must not be <code>null</code>.
   * @param listener
   *          specifies the listener to receive messages; must not be <code>null</code>.
   * @param input
   *          specifies how to subscribe for messages.; must not be <code>null</code>.
   */
  <DTO> ISubscription subscribe(IDestination<DTO> destination, IMessageListener<DTO> listener, SubscribeInput input) throws JMSException;
}
