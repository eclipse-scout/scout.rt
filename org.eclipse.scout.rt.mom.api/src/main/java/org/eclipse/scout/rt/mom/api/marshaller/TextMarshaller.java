package org.eclipse.scout.rt.mom.api.marshaller;

import java.util.Map;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller allows to transport an object's {@link #toString()} representation as textual data across the
 * network.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 * <p>
 * If using an {@link IEncrypter}, consider the usage of {@link TextAsBytesMarshaller}, because encrypter produces
 * binary data. If still using this marshaller, data is additionally encoded into Base64 format.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @since 6.1
 */
@Bean
public class TextMarshaller implements IMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }

    return transferObject.toString();
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return (String) data;
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_TEXT;
  }
}
