/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.mom.api.marshaller;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;

/**
 * This marshaler allows to transport an object's JSON representation as textual data across the network. It uses the
 * Scout {@link IDataObjectMapper} to serialize the content to a string representation.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @since 6.1
 */
@Bean
public class JsonMarshaller implements IMarshaller {

  public static final String CTX_PROP_OBJECT_TYPE = "x-scout.mom.json.objecttype";

  protected final IDataObjectMapper m_dataObjectMapper;

  public JsonMarshaller() {
    m_dataObjectMapper = createDataObjectMapper();
  }

  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    if (transferObject == null) {
      return null;
    }
    context.put(CTX_PROP_OBJECT_TYPE, transferObject.getClass().getName());
    return m_dataObjectMapper.writeValue(transferObject);
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    final String jsonText = (String) data;
    if (jsonText == null) {
      return null;
    }

    try {
      final Class<?> objectType = Class.forName(context.get(CTX_PROP_OBJECT_TYPE));
      return m_dataObjectMapper.readValue(jsonText, objectType);
    }
    catch (final ClassNotFoundException e) {
      throw BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(e);
    }
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_TEXT;
  }

  /**
   * Resolves {@link IDataObjectMapper} instance.
   */
  protected IDataObjectMapper createDataObjectMapper() {
    return BEANS.get(IDataObjectMapper.class);
  }
}
