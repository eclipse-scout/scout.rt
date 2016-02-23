/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.jms;

import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.serialization.IObjectSerializer;
import org.eclipse.scout.rt.platform.serialization.SerializationUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsMessageSerializer<T> implements IJmsMessageSerializer<T> {
  private static Logger LOG = LoggerFactory.getLogger(JmsMessageSerializer.class);
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

  @Override
  public Message createMessage(T message, Session session) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("creating JMS message: msgContent={}", message);
    }
    try {
      BytesMessage jmsMessage = session.createBytesMessage();
      jmsMessage.setJMSCorrelationID(CorrelationId.CURRENT.get());
      jmsMessage.writeBytes(getObjectSerializer().serialize(message));
      if (LOG.isTraceEnabled()) {
        jmsMessage.setStringProperty(JMS_PROPERTY_TRACE_MESSAGE_CONTENT, message.toString());
      }
      return jmsMessage;
    }
    catch (JMSException | IOException e) {
      throw new ProcessingException("Unexpected Exception", e);
    }
  }

  @Override
  public T extractMessage(Message jmsMessage) {
    if (LOG.isTraceEnabled()) {
      try {
        LOG.trace("extracting JMS message: jmsMessageId={}, messageContent={}", jmsMessage.getJMSMessageID(), jmsMessage.getStringProperty(JMS_PROPERTY_TRACE_MESSAGE_CONTENT));
      }
      catch (JMSException e) {
        LOG.trace("extracting JMS message: jmsMessageId=?, messageContent=?", e);
      }
    }
    try {
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
        LOG.warn("received BytesMessage is too large (length={})", bodyLength);
      }
      else {
        byte[] buffer = new byte[(int) bodyLength];
        bm.readBytes(buffer);
        return getObjectSerializer().deserialize(buffer, getMessageType());
      }
      return null;
    }
    catch (ClassNotFoundException | JMSException | IOException e) {
      throw new ProcessingException("Unexpected Exception", e);
    }
  }
}
