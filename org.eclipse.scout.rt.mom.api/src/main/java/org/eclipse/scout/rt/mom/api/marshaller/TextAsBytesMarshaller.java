package org.eclipse.scout.rt.mom.api.marshaller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller allows to transport an object's {@link #toString()} representation as binary data across the network.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 * 
 * @see IMarshaller#MESSAGE_TYPE_BYTES
 * @since 6.1
 */
@Bean
public class TextAsBytesMarshaller extends TextMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    final String plainText = (String) super.marshall(transferObject, context);
    if (plainText == null) {
      return null;
    }

    return plainText.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final byte[] plainBytes = (byte[]) data;
    if (plainBytes == null) {
      return null;
    }

    final String plainText = new String(plainBytes, StandardCharsets.UTF_8);
    return super.unmarshall(plainText, context);
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_BYTES;
  }
}
