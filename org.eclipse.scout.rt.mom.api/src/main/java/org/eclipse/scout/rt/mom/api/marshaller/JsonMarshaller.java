/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api.marshaller;

import java.io.IOException;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This marshaller allows to transport an object's JSON representation as textual data across the network. It uses the
 * jackson-databind library.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @since 6.1
 */
@Bean
public class JsonMarshaller implements IMarshaller {

  public static final String CTX_PROP_OBJECT_TYPE = "x-scout.mom.json.objecttype";

  protected final ObjectMapper m_objectMapper;

  public JsonMarshaller() {
    m_objectMapper = createObjectMapper();
  }

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }

    try {
      context.put(CTX_PROP_OBJECT_TYPE, transferObject.getClass().getName());
      return m_objectMapper.writeValueAsString(transferObject);
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
      final Class<?> objectType = Class.forName(context.get(CTX_PROP_OBJECT_TYPE));
      return m_objectMapper.readValue(jsonText, objectType);
    }
    catch (final IOException | ClassNotFoundException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_TEXT;
  }

  /**
   * Create new {@link ObjectMapper} instance.<br/>
   * Override or extend this method to create a custom configured {@link ObjectMapper} instance.
   */
  // TODO [15.4] pbz,bsh: check if we can use org.eclipse.scout.rt.jackson.databind.ObjectMapperFactory, remove jackson dependency
  protected ObjectMapper createObjectMapper() {
    return new ObjectMapper();
  }
}
