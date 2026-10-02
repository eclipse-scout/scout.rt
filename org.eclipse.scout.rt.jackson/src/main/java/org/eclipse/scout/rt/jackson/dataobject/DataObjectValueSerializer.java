/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import org.eclipse.scout.rt.dataobject.IDataObjectValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link IDataObjectValue}.
 */
public class DataObjectValueSerializer extends StdSerializer<IDataObjectValue> {

  public DataObjectValueSerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(IDataObjectValue entity, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writePOJO(entity.getValue());
  }

  @Override
  public void serializeWithType(IDataObjectValue entity, JsonGenerator gen, SerializationContext ctxt, TypeSerializer typeSer) throws JacksonException {
    gen.writePOJO(entity.getValue());
  }
}
