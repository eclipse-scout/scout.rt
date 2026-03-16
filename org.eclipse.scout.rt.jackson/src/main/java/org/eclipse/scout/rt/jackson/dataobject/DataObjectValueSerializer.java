/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.io.Serial;

import org.eclipse.scout.rt.dataobject.IDataObjectValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link IDataObjectValue}.
 */
public class DataObjectValueSerializer extends StdSerializer<IDataObjectValue> {
  @Serial
  private static final long serialVersionUID = 1L;

  public DataObjectValueSerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(IDataObjectValue entity, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writePOJO(entity.getValue());
  }

  @Override
  public void serializeWithType(IDataObjectValue entity, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    gen.writePOJO(entity.getValue());
  }
}
