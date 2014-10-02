/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.serialization.IObjectSerializer;
import org.eclipse.scout.commons.serialization.SerializationUtility;

public class JmsMessageSerializer<T> {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(JmsMessageSerializer.class);
  // Property name to save the message content on a JMS message - only used for logging/debug reasons.
  private static final String JMS_PROPERTY_TRACE_MESSAGE_CONTENT = "traceMessageContent";

  private final IObjectSerializer m_objectSerializer;
  private final Class<T> m_messageType;

  public JmsMessageSerializer(Class<T> messageType) {
    this(SerializationUtility.createObjectSerializer(), messageType);
  }

  protected JmsMessageSerializer(IObjectSerializer objectSerializer, Class<T> messageType) {
    super();
    m_objectSerializer = objectSerializer;
    m_messageType = messageType;
  }

  protected Class<T> getMessageType() {
    return m_messageType;
  }

  protected IObjectSerializer getObjectSerializer() {
    return m_objectSerializer;
  }

  public Message createMessage(T message, Session session) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("creating JMS message: msgContent={0}", message);
    }
    BytesMessage jmsMessage = session.createBytesMessage();
    jmsMessage.writeBytes(getObjectSerializer().serialize(message));
    if (LOG.isTraceEnabled()) {
      jmsMessage.setStringProperty(JMS_PROPERTY_TRACE_MESSAGE_CONTENT, message.toString());
    }
    return jmsMessage;
  }

  public T extractMessage(Message jmsMessage) throws Exception {
    if (LOG.isTraceEnabled()) {
      LOG.trace("extracting JMS message: jmsMessageId={0}, messageContent={1}", jmsMessage.getJMSMessageID(), jmsMessage.getStringProperty(JMS_PROPERTY_TRACE_MESSAGE_CONTENT));
    }
    if (!(jmsMessage instanceof BytesMessage)) {
      LOG.warn("Received unexpect message content. Ignored.");
      return null;
    }

    BytesMessage bm = (BytesMessage) jmsMessage;
    long bodyLength = bm.getBodyLength();
    if (bodyLength == Integer.MAX_VALUE) {
      LOG.warn("received empty BytesMessage");
    }
    else if (bodyLength > Integer.MAX_VALUE) {
      LOG.warn("received BytesMessage is too large (length = " + bodyLength + ")");
    }
    else {
      byte[] buffer = new byte[(int) bodyLength];
      bm.readBytes(buffer);
      byte[] data = new byte[(int) bm.getBodyLength()];
      return getObjectSerializer().deserialize(data, getMessageType());
    }
    return null;
  }
}
