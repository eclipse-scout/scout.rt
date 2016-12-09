package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IBiDestination;
import org.eclipse.scout.rt.mom.api.IRequestListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.SubscribeInput;

/**
 * Strategy to reply to requests in 'request-reply' messaging.
 *
 * @since 6.1
 */
public interface IReplierStrategy {

  /**
   * Subscribes for requests sent to the given destination.
   *
   * @param destination
   *          specifies the target of the subscription; must not be <code>null</code>.
   * @param listener
   *          specifies the listener to reply to requests; must not be <code>null</code>.
   * @param input
   *          specifies how to subscribe for messages.; must not be <code>null</code>.
   */
  <REQUEST, REPLY> ISubscription subscribe(IBiDestination<REQUEST, REPLY> destination, IRequestListener<REQUEST, REPLY> listener, SubscribeInput input) throws JMSException;
}
