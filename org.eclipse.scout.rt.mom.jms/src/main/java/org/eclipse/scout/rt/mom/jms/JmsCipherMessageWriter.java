package org.eclipse.scout.rt.mom.jms;

import static org.eclipse.scout.rt.mom.jms.IJmsMomProperties.PROP_ENCRYPTER_CONTEXT;
import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.mom.api.marshaller.IMarshaller;
import org.eclipse.scout.rt.platform.util.Base64Utility;

/**
 * Allows to write a JMS message, and to encrypt its payload and properties.
 *
 * @since 6.1
 * @see JmsMom
 */
public class JmsCipherMessageWriter extends JmsMessageWriter {

  protected IEncrypter m_encrypter;
  protected Map<String, String> m_encrypterContext;

  /**
   * Initializes this writer.
   */
  protected JmsMessageWriter init(final Session session, final IMarshaller marshaller, final IEncrypter encrypter) throws JMSException {
    m_encrypter = assertNotNull(encrypter, "Encrypter not specified");
    m_encrypterContext = encrypter.newContext();
    return super.init(session, marshaller);
  }

  @Override
  public JmsMessageWriter writeProperty(final String property, final String value, final boolean encrypt) throws JMSException, GeneralSecurityException {
    if (value == null) {
      return this;
    }

    String propertyValue = value;
    if (encrypt) {
      final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      propertyValue = Base64Utility.encode(m_encrypter.encryptProperty(property, bytes, m_encrypterContext));
    }

    return super.writeProperty(property, propertyValue, false);
  }

  @Override
  protected void writeTextMessage(final TextMessage message, final String text) throws JMSException, GeneralSecurityException {
    final byte[] clearTextBytes = (text != null ? text.getBytes(StandardCharsets.UTF_8) : new byte[0]);
    final byte[] encryptedBytes = m_encrypter.encrypt(clearTextBytes, m_encrypterContext);
    super.writeTextMessage(message, Base64Utility.encode(encryptedBytes));
  }

  @Override
  protected void writeBytesMessage(final BytesMessage message, final byte[] bytes) throws JMSException, GeneralSecurityException {
    final byte[] clearTextBytes = (bytes != null ? bytes : new byte[0]);
    final byte[] encryptedBytes = m_encrypter.encrypt(clearTextBytes, m_encrypterContext);
    super.writeBytesMessage(message, encryptedBytes);
  }

  @Override
  public Message build() throws JMSException, GeneralSecurityException {
    writeContext(PROP_ENCRYPTER_CONTEXT, m_encrypterContext, false);
    return super.build();
  }
}
