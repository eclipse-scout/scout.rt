package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.api.marshaller.IMarshaller.MESSAGE_TYPE_BYTES;
import static org.eclipse.scout.rt.mom.api.marshaller.IMarshaller.MESSAGE_TYPE_TEXT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.JMS_PROP_MARSHALLER_CONTEXT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.CTX_PROP_NULL_OBJECT;
import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.CTX_PROP_REQUEST_REPLY_SUCCESS;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.mom.api.marshaller.JsonMarshaller;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to read a JMS message.
 *
 * @since 6.1
 * @see JmsMomImplementor
 */
@Bean
public class JmsMessageReader<DTO> {

  private static final Logger LOG = LoggerFactory.getLogger(JmsMessageReader.class);

  protected Message m_message;

  protected IMarshaller m_marshaller;
  protected Map<String, String> m_marshallerContext;

  /**
   * Initializes this reader.
   */
  protected JmsMessageReader init(final Message message, final IMarshaller marshaller) throws JMSException {
    m_message = assertNotNull(message, "Message not specified");
    m_marshaller = assertNotNull(marshaller, "Marshaller not specified");
    initContext();
    return this;
  }

  protected void initContext() throws JMSException {
    m_marshallerContext = readContext(JMS_PROP_MARSHALLER_CONTEXT);
  }

  /**
   * Reads the transfer object by using the readers's {@link IMarshaller}.
   *
   * @see JmsMessageWriter#writeTransferObject(Object)
   */
  @SuppressWarnings("unchecked")
  public DTO readTransferObject() throws JMSException {
    if (Boolean.valueOf(m_marshallerContext.get(CTX_PROP_NULL_OBJECT))) {
      return null;
    }

    Object transferData;
    switch (m_marshaller.getMessageType()) {
      case MESSAGE_TYPE_TEXT:
        transferData = readTextMessage((TextMessage) m_message);
        break;
      case MESSAGE_TYPE_BYTES:
        transferData = readBytesMessage((BytesMessage) m_message);
        break;
      default:
        throw new PlatformException("Unsupported message type '{}'", m_marshaller.getMessageType());
    }

    return (DTO) m_marshaller.unmarshall(transferData, m_marshallerContext);
  }

  /**
   * Reads the given property.
   */
  public String readProperty(final String property) throws JMSException {
    return m_message.getStringProperty(property);
  }

  /**
   * Returns the message's correlation ID, or a random via {@link CorrelationId} if not set.
   */
  protected String readCorrelationId() {
    try {
      final String cid = m_message.getJMSCorrelationID();
      if (cid != null) {
        return cid;
      }
    }
    catch (final JMSException e) {
      LOG.warn("Failed to read correlation-ID from JMS message", e);
    }
    return BEANS.get(CorrelationId.class).newCorrelationId();
  }

  /**
   * Reads the reply-destination used in 'request-reply' communication, or throws {@link AssertionException} if not set.
   *
   * @see JmsMessageWriter#writeReplyTo(Destination)
   */
  public Destination readReplyTo() throws JMSException {
    final Destination replyTo = m_message.getJMSReplyTo();
    return assertNotNull(replyTo, "missing 'replyTo' [msg={}]", m_message);
  }

  /**
   * Reads whether 'request-reply' communication returned without a failure.
   *
   * @see JmsMessageWriter#writeReplySuccess(String)
   */
  public boolean readRequestReplySuccess() {
    return Boolean.valueOf(m_marshallerContext.get(CTX_PROP_REQUEST_REPLY_SUCCESS));
  }

  public IMessage<DTO> readMessage() throws JMSException {
    final DTO transferObject = readTransferObject();
    return new IMessage<DTO>() {

      @Override
      public DTO getTransferObject() {
        return transferObject;
      }

      @Override
      public String getProperty(final String property) {
        try {
          return readProperty(property);
        }
        catch (JMSException e) {
          throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
        }
      }

      @SuppressWarnings("unchecked")
      @Override
      public <T> T getAdapter(final Class<T> type) {
        if (Message.class.equals(type)) {
          return (T) m_message;
        }
        return null;
      }
    };
  }

  /**
   * Reads the given {@link Map} from message properties.
   *
   * @see JmsMessageWriter#writeContext(String, Map)
   */
  @SuppressWarnings("unchecked")
  protected Map<String, String> readContext(final String property) throws JMSException {
    final String json = readProperty(property);
    return (Map<String, String>) BEANS.get(JsonMarshaller.class).unmarshall(json, Collections.singletonMap(JsonMarshaller.CTX_PROP_OBJECT_TYPE, HashMap.class.getName()));
  }

  /**
   * @see JmsMessageWriter#writeTextMessage(TextMessage, String)
   */
  protected String readTextMessage(final TextMessage message) throws JMSException {
    return message.getText();
  }

  /**
   * @see JmsMessageWriter#writeBytesMessage(BytesMessage, byte[])
   */
  protected byte[] readBytesMessage(final BytesMessage message) throws JMSException {
    long length = message.getBodyLength();
    if (length == Integer.MAX_VALUE) {
      LOG.warn("BytesMessage received is empty");
      return null;
    }
    else if (length > Integer.MAX_VALUE) {
      LOG.warn("BytesMessage received is too large [length={}]", length);
      return null;
    }

    final byte[] bytes = new byte[(int) length];
    message.readBytes(bytes);
    return bytes;
  }

  /**
   * Creates a new reader instance.
   */
  @SuppressWarnings("unchecked")
  public static <DTO> JmsMessageReader<DTO> newInstance(final Message message, final IMarshaller marshaller) throws JMSException {
    return BEANS.get(JmsMessageReader.class).init(message, marshaller);
  }
}
