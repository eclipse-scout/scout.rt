/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.transaction.TransactionScope;

/**
 * Specifies how to subscribe for messages, like message acknowledgment, or in which {@link RunContext} to consume
 * messages.
 *
 * @see IMom
 * @since 6.1
 */
@Bean
public class SubscribeInput {

  /**
   * Subscription mode to acknowledge a message automatically upon its receipt. This mode dispatches message processing
   * to a separate thread to allow concurrent message consumption.
   * <p>
   * This is the default acknowledgment mode.
   */
  public static final int ACKNOWLEDGE_AUTO = 1;

  /**
   * Subscription mode to acknowledge a message automatically upon its receipt. This mode uses the <i>message receiving
   * thread</i> to process the message, meaning the subscription does not receive any other messages for the time of
   * processing a message.
   */
  public static final int ACKNOWLEDGE_AUTO_SINGLE_THREADED = 2;

  /**
   * Subscription mode to acknowledge a message upon successful commit of the receiving transaction. This mode uses the
   * <i>message receiving thread</i> to process the message, meaning the subscription does not receive any other
   * messages for the time of processing a message.
   * <p>
   * This mode is not supported for 'request-reply' messaging because processing failures are transported back to the
   * initiator.
   */
  public static final int ACKNOWLEDGE_TRANSACTED = 3;

  private int m_acknowledgementMode = ACKNOWLEDGE_AUTO;
  private RunContext m_runContext;
  private String m_selector;
  private boolean m_localReceipt = true;
  private String m_durableSubscriptionName;

  public int getAcknowledgementMode() {
    return m_acknowledgementMode;
  }

  /**
   * Specifies the mode how to acknowledge messages. Supported modes are {@link #ACKNOWLEDGE_AUTO},
   * {@link #ACKNOWLEDGE_AUTO_SINGLE_THREADED} and {@link #ACKNOWLEDGE_TRANSACTED}.
   */
  public SubscribeInput withAcknowledgementMode(final int acknowledgementMode) {
    m_acknowledgementMode = acknowledgementMode;
    return this;
  }

  public String getSelector() {
    return m_selector;
  }

  /**
   * Specifies a selector to filter messages, like to receive only messages with a specific property set. This selector
   * is implementor specific.
   * <p>
   * Example for JMS:<br>
   * <code>
   * selector = "user = 'anna'" // to receive only messages with the property 'user' set to 'anna'
   * </code><br>
   * See https://docs.oracle.com/cd/E19798-01/821-1841/bncer/index.html for more information.
   */
  public SubscribeInput withSelector(final String selector) {
    m_selector = selector;
    return this;
  }

  public RunContext getRunContext() {
    return m_runContext;
  }

  /**
   * Specifies the optional context in which to receive messages. If not specified, an empty context is created. In
   * either case, the transaction scope is set to {@link TransactionScope#REQUIRES_NEW}.
   */
  public SubscribeInput withRunContext(final RunContext runContext) {
    m_runContext = runContext;
    return this;
  }

  public boolean isLocalReceipt() {
    return m_localReceipt;
  }

  /**
   * Specifies if local delivery of messages is active for this subscription. The default is <code>true</code>.
   * <p>
   * All messages sent by a MOM instance are 'local' to that MOM instance. If a MOM instance subscribes to a destination
   * it also publishes messages to, it may receive its own messages. The subscription may be configured to ignore
   * messages from this instance by setting this flag to <code>false</code>.
   * <p>
   * This hint is implementor specific and may not have an effect at all. E.g. in JMS, it is only supported by topic
   * destinations.
   */
  public SubscribeInput withLocalReceipt(boolean localReceipt) {
    m_localReceipt = localReceipt;
    return this;
  }

  /**
   * @return the name of a <i>durable</i> subscription. A value of <code>null</code> indicates a non-durable
   *         subscription (this is the default). Any other value indicates a durable subscription.
   * @see {@link #withDurableSubscription(String)}
   */
  public String getDurableSubscriptionName() {
    return m_durableSubscriptionName;
  }

  /**
   * Specifies that the subscription should be <i>durable</i>. Durable subscriptions are kept by the network even if the
   * subscriber disconnects. After it reconnects, all missed messages (whose individual time-to-live has not expired)
   * are received.
   * <p>
   * A durable subscription is identified by a unique name. If the name is <code>null</code>, the subscription is
   * considered non-durable (this is the default value). All other values result in a durable subscription. Durable
   * subscriptions can be explicitly cancelled by calling {@link MOM#cancelDurableSubscription(Class, String)} with the
   * same name.
   * <p>
   * Note that not all destination types support durable subscriptions. For example, in JMS only topic make a difference
   * between durable and non-durable subscribers (queues are inherently durable).
   */
  public SubscribeInput withDurableSubscription(String durableSubscriptionName) {
    m_durableSubscriptionName = durableSubscriptionName;
    return this;
  }
}
