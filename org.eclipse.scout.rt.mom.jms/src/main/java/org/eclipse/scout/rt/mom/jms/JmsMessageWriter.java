package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.api.marshaller.IMarshaller.MESSAGE_TYPE_BYTES;
import static org.eclipse.scout.rt.mom.api.marshaller.IMarshaller.MESSAGE_TYPE_NO_PAYLOAD;
import static org.eclipse.scout.rt.mom.api.marshaller.IMarshaller.MESSAGE_TYPE_TEXT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.CTX_PROP_NULL_OBJECT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.CTX_PROP_REQUEST_REPLY_SUCCESS;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_MARSHALLER_CONTEXT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_REPLY_ID;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * Allows to write a JMS message.
 *
 * @since 6.1
 * @see JmsMomImplementor
 */
@Bean
public class JmsMessageWriter {

  protected Message m_message;

  protected IMarshaller m_marshaller;
  protected Map<String, String> m_marshallerContext;

  /**
   * Initializes this writer.
   */
  protected JmsMessageWriter init(final Session session, final IMarshaller marshaller) throws JMSException {
    assertNotNull(session, "Session not specified");
    m_message = createMessage(marshaller.getMessageType(), session);
    m_marshaller = assertNotNull(marshaller, "Marshaller not specified");
    m_marshallerContext = new HashMap<>();
    return this;
  }

  /**
   * Creates the message of the given type.
   */
  protected Message createMessage(final int messageType, final Session session) throws JMSException {
    switch (messageType) {
      case MESSAGE_TYPE_TEXT:
        return session.createTextMessage();
      case MESSAGE_TYPE_BYTES:
        return session.createBytesMessage();
      case MESSAGE_TYPE_NO_PAYLOAD:
        return session.createMessage();
      default:
        throw new PlatformException("Unsupported message type '{}'", messageType);
    }
  }

  /**
   * Writes the given transfer object, and uses the writer's {@link IMarshaller} to transform the object into its
   * transport type.
   *
   * @see JmsMessageReader#readTransferObject()
   */
  public JmsMessageWriter writeTransferObject(final Object transferObject) throws JMSException {
    final Object transportObject = m_marshaller.marshall(transferObject, m_marshallerContext);
    m_marshallerContext.put(CTX_PROP_NULL_OBJECT, Boolean.valueOf(transferObject == null).toString());

    switch (m_marshaller.getMessageType()) {
      case MESSAGE_TYPE_TEXT:
        writeTextMessage((TextMessage) m_message, (String) transportObject);
        break;
      case MESSAGE_TYPE_BYTES:
        writeBytesMessage((BytesMessage) m_message, (byte[]) transportObject);
        break;
      case MESSAGE_TYPE_NO_PAYLOAD:
        break;
      default:
        throw new PlatformException("Unsupported transport type '{}'", m_marshaller.getMessageType());
    }

    return this;
  }

  /**
   * Writes the given property.
   *
   * @see JmsMessageReader#readProperty(String)
   */
  public JmsMessageWriter writeProperty(final String property, final String value) throws JMSException {
    if (value == null) {
      return this;
    }

    m_message.setStringProperty(property, value);
    return this;
  }

  /**
   * Convenience method for {@link #writeProperty(String, String)} to write multiple properties.
   */
  public JmsMessageWriter writeProperties(final Map<String, String> properties) throws JMSException {
    for (final Entry<String, String> property : properties.entrySet()) {
      writeProperty(property.getKey(), property.getValue());
    }
    return this;
  }

  /**
   * Writes the correlation ID to uniquely track message processing across multiple systems.
   */
  public JmsMessageWriter writeCorrelationId(final String correlationId) throws JMSException {
    m_message.setJMSCorrelationID(correlationId);
    return this;
  }

  /**
   * Writes the reply-destination used in 'request-reply' communication.
   *
   * @see JmsMessageReader#readReplyTo()
   */
  public JmsMessageWriter writeReplyTo(final Destination destination) throws JMSException {
    m_message.setJMSReplyTo(destination);
    return this;
  }

  /**
   * Writes the reply id used to follow a 'request-reply' communication, and must be unique for every communication
   * initiated.
   */
  public JmsMessageWriter writeReplyId(final String replyId) throws JMSException {
    return writeProperty(JMS_PROP_REPLY_ID, replyId);
  }

  /**
   * Writes whether 'request-reply' communication returned without a failure.
   *
   * @see JmsMessageReader#readReplyCode()
   */
  public JmsMessageWriter writeRequestReplySuccess(final boolean success) {
    m_marshallerContext.put(CTX_PROP_REQUEST_REPLY_SUCCESS, Boolean.toString(success));
    return this;
  }

  /**
   * @see JmsMessageReader#readTextMessage(TextMessage)
   */
  protected void writeTextMessage(final TextMessage message, final String text) throws JMSException {
    message.setText(text);
  }

  /**
   * @see JmsMessageReader#readBytesMessage(BytesMessage)
   */
  protected void writeBytesMessage(final BytesMessage message, final byte[] data) throws JMSException {
    final byte[] bytes = (data != null ? data : new byte[0]);
    message.writeBytes(bytes);
  }

  /**
   * Writes the given {@link Map} as message properties.
   *
   * @see JmsMessageReader#readContext(String)
   */
  protected JmsMessageWriter writeContext(final String property, final Map<String, String> context) throws JMSException {
    if (context.isEmpty()) {
      return this;
    }

    final String json = (String) BEANS.get(JsonMarshaller.class).marshall(context, new HashMap<String, String>());
    writeProperty(property, json);
    return this;
  }

  /**
   * Finish writing and get the message.
   */
  public Message build() throws JMSException {
    writeContext(JMS_PROP_MARSHALLER_CONTEXT, m_marshallerContext);
    return m_message;
  }

  /**
   * Creates a new writer instance.
   */
  public static JmsMessageWriter newInstance(final Session session, final IMarshaller marshaller) throws JMSException {
    return BEANS.get(JmsMessageWriter.class).init(session, marshaller);
  }
}
