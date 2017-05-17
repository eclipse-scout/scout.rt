package org.eclipse.scout.rt.mom.api.marshaller;

import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller indicates that no payload is transfered and nothing has to be marshalled.
 * <p>
 * This marshaller does not support the serialization of exceptions.
 *
 * @see IMarshaller#MESSAGE_TYPE_NO_PAYLOAD
 * @since 7.0
 */
@Bean
public class NoPayloadMarshaller implements IMarshaller {

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    return null;
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return null;
  }

  @Override
  public int getMessageType() {
    return IMarshaller.MESSAGE_TYPE_NO_PAYLOAD;
  }
}
