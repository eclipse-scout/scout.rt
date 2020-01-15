/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.mom.api.marshaller;

import static org.eclipse.scout.rt.platform.util.Assertions.assertType;

import java.util.Map;

import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDataObjectMapper;
import org.eclipse.scout.rt.dataobject.TypeName;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * This marshaller allows to transport an object's JSON representation as textual data across the network. It uses the
 * Scout {@link IDataObjectMapper} to serialize the content to a string representation. <b>The content data must be an
 * instance of {@link IDataObject} annotated with a {@link TypeName}.</b>
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

  /**
   * Resolves {@link IDataObjectMapper} instance.
   */
  protected IDataObjectMapper createDataObjectMapper() {
    return BEANS.get(IDataObjectMapper.class);
  }
}
