package org.eclipse.scout.rt.mom.jms;

import javax.jms.JMSException;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.platform.context.RunContext;

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
   * @param runContext
   *          specifies the context in which to receive and process the messages; must not be <code>null</code>.
   */
  <TRANSFER_OBJECT> ISubscription subscribe(IDestination destination, IMessageListener<TRANSFER_OBJECT> listener, RunContext runContext) throws JMSException;
}
