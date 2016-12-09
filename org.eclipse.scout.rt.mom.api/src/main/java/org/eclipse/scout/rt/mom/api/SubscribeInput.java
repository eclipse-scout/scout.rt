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
}
