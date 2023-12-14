/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.jms;

import java.util.Map;

import jakarta.jms.Message;

import org.eclipse.scout.rt.mom.api.IDestination;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.Bean;

/**
 * <i>Handler</i> interface for handling incoming and outgoing JMS messages.
 * <p>
 * A possible purpose is to log JMS messages.
 * <p>
 * The handler is associated with a {@link org.eclipse.scout.rt.mom.api.IMomTransport} via the MOM environment, see
 * {@link org.eclipse.scout.rt.mom.jms.JmsMomImplementor#JMS_MESSAGE_HANDLER}. The handler itself might also be
 * configured via the MOM environment, see {@link #init(Map)}.
 */
@Bean
public interface IJmsMessageHandler {

  /**
   * Initializes this handler, e.g. to setup a message content formatter as specified by the given properties.
   *
   * @param properties
   *          the MOM environment properties provided to the JMS implementor
   * @see org.eclipse.scout.rt.mom.api.IMomImplementor.init(Map<Object, Object>)
   */
  void init(Map<Object, Object> properties);

  /**
   * Handles JMS messages consumed by a {@link jakarta.jms.MessageConsumer}.
   * <p>
   * This method is called directly after a JMS message has been received.
   * <p>
   * The message has not yet been processed (unmarshalled) by the MOM framework.
   */
  void handleIncoming(IDestination<?> destination, Message message, IMarshaller marshaller);

  /**
   * Handles JMS messages being sent by a {@link jakarta.jms.MessageProducer}.
   * <p>
   * This method is called directly before a JMS message is "sent" by the <i>MessageProducer</i>. "Sent" means that the
   * <i>send</i> method of the message producer is called. Therefore it is not guaranteed that the time, at which this
   * method is called, is the <i>sent time</i> of the JMS message (e.g. in a transactional context).
   * <p>
   * The message has already been processed (marshalled) by the MOM framework.
   *
   * @param destination
   *          the MOM destination this message is being sent to. <b>Attention:</b> This might be <code>null</code> in
   *          case of a 'request-reply' communication, where the reply message is only sent back through the JMS
   *          destination defined by {@link Message#getJMSReplyTo()} (and not through a MOM destination)
   */
  void handleOutgoing(IDestination<?> destination, Message message, IMarshaller marshaller);
}
