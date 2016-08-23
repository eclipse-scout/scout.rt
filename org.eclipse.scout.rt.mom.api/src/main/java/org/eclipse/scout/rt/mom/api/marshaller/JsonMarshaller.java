package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.eclipse.scout.rt.mom.api.encrypter.IEncrypter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * This marshaller allows to transport an object's JSON representation as textual data across the network.
 * <p>
 * If using an {@link IEncrypter}, consider the usage of {@link JsonAsBytesMarshaller}, because encrypter produces
 * binary data. If still using this marshaller, data is additionally encoded into Base64 format.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @since 6.1
 */
@Bean
public class JsonMarshaller implements IMarshaller {

  public static final String PROP_OBJECT_TYPE = "x-scout.mom.json.objecttype";

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }

    try (StringWriter writer = new StringWriter()) {
      JSON.std.write(transferObject, writer);

      context.put(PROP_OBJECT_TYPE, transferObject.getClass().getName());
      return writer.getBuffer().toString();
    }
    catch (final IOException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final String jsonText = (String) data;
    if (jsonText == null) {
      return null;
    }

    try {
      final Class<?> objectType = Class.forName(context.get(PROP_OBJECT_TYPE));
      return JSON.std.beanFrom(objectType, jsonText);
    }
    catch (final IOException | ClassNotFoundException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_TEXT;
  }
}
