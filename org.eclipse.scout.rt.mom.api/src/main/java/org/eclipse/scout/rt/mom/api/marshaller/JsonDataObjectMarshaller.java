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

import static org.eclipse.scout.rt.platform.util.Assertions.assertType;

import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * This marshaller allows to transport an {@link IDataObject} in its JSON form as textual data across the network. It
 * uses the Scout {@link IDataObjectMapper} to serialize/deserialize the content to/from a string representation.
 * <p>
 * <b>Important:</b> The content data <i>must</i> be an instance of {@link IDataObject} annotated with a
 * &#64;{@link TypeName}. For other object types, consider using {@link JsonMarshaller} instead.
 *
 * @see IMarshaller#MESSAGE_TYPE_TEXT
 * @see JsonMarshaller for simple type content objects (e.g. String or Boolean)
 * @since 8.0
 */
@Bean
public class JsonDataObjectMarshaller implements IMarshaller {

  protected final IDataObjectMapper m_dataObjectMapper;

  public JsonDataObjectMarshaller() {
    m_dataObjectMapper = createDataObjectMapper();
  }

  /**
   * @param transferObject
   *     object to marshal, must be of type {@code IDataObject} (or {@code null})
   * @throws AssertionException
   *     if the given object is not of the expected type
   */
  @Override
  public Object marshall(final Object transferObject, final Map<String, String> context) {
    return m_dataObjectMapper.writeValue(assertType(transferObject, IDataObject.class));
  }

  @Override
  public Object unmarshall(final Object data, final Map<String, String> context) {
    return m_dataObjectMapper.readValue(assertType(data, String.class), IDataObject.class);
  }

  @Override
  public int getMessageType() {
    return MESSAGE_TYPE_TEXT;
  }

  protected IDataObjectMapper createDataObjectMapper() {
    return BEANS.get(IDataObjectMapper.class);
  }
}
