package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.PROP_ENCRYPTER_CONTEXT;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.util.Base64Utility;

/**
 * Allows to read a JMS message, and to decrypt its payload and properties.
 *
 * @since 6.1
 * @see JmsMomImplementor
 */
public class JmsCipherMessageReader<DTO> extends JmsMessageReader<DTO> {

  protected IEncrypter m_encrypter;
  protected Map<String, String> m_encrypterContext;

  /**
   * Initializes this reader.
   */
  protected JmsMessageReader init(final Message message, final IMarshaller marshaller, final IEncrypter encrypter) throws JMSException, GeneralSecurityException {
    m_encrypter = encrypter;
    return super.init(message, marshaller);
  }

  @Override
  protected void initContext() throws JMSException, GeneralSecurityException {
    m_encrypterContext = readContext(PROP_ENCRYPTER_CONTEXT, false);
    super.initContext();
  }

  @Override
  public String readProperty(final String property, final boolean decrypt) throws JMSException, GeneralSecurityException {
    final String value = super.readProperty(property, false);
    if (value == null || !decrypt) {
      return value;
    }

    final byte[] clearTextBytes = m_encrypter.decryptProperty(property, Base64Utility.decode(value), m_encrypterContext);
    return new String(clearTextBytes, StandardCharsets.UTF_8);
  }

  @Override
  protected String readTextMessage(final TextMessage message) throws JMSException, GeneralSecurityException {
    final String encryptedText = super.readTextMessage(message);
    final byte[] clearTextBytes = m_encrypter.decrypt(Base64Utility.decode(encryptedText), m_encrypterContext);
    return new String(clearTextBytes, StandardCharsets.UTF_8);
  }

  @Override
  protected byte[] readBytesMessage(final BytesMessage message) throws JMSException, GeneralSecurityException {
    final byte[] encryptedBytes = new byte[(int) message.getBodyLength()];
    message.readBytes(encryptedBytes);
    return m_encrypter.decrypt(encryptedBytes, m_encrypterContext);
  }
}
