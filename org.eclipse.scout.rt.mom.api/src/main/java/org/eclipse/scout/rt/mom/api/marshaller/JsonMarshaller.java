/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;

/**
 * This marshaller allows to transport any object's JSON representation as textual data across the network. It uses the
 * Scout {@link IDataObjectMapper} to serialize/deserialize the content to/from a string representation.
 * <p>
 * <b>Important:</b> If the object to be transferred is an {@link IDataObject}s annotated with a &#64;{@link TypeName},
 * use the {@link JsonDataObjectMarshaller} instead. The {@link JsonMarshaller} is only intended to transport objects as
 * JSON that do not conform to the {@link IDataObject} interface and should only be necessary in special cases.
 * <p>
 * Unlike {@link JsonDataObjectMarshaller}, this marshaller accepts any value that can be handled by the
 * {@link IDataObjectMapper}. This includes literals ({@code String}, {@code Boolean} etc.) and objects following the
 * <i>JavaBeans</i> convention. The full name of the source class is transferred in a custom context property
 * {@link #CTX_PROP_OBJECT_TYPE} along the the marshalled data. When unmarshalling the JSON back into its object form,
 * the exact same class with that name must exist and be resolvable by the current class loader.
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

  protected IDataObjectMapper createDataObjectMapper() {
    return BEANS.get(IDataObjectMapper.class);
  }
}
