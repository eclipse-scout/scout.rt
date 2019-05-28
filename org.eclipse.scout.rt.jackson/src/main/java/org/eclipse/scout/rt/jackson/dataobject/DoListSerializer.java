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
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.DoList;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link DoList}
 */
public class DoListSerializer extends StdSerializer<DoList<?>> {
  private static final long serialVersionUID = 1L;

  public DoListSerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(DoList<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    serializeList(value, gen);
  }

  @Override
  public void serializeWithType(DoList<?> value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
    serializeList(value, gen);
  }

  protected void serializeList(DoList<?> value, JsonGenerator gen) throws IOException {
    // serialize DoList as array using default jackson serializer (includes types if necessary according to actual chosen serializer for each object type)
    gen.writeStartArray();
    gen.setCurrentValue(value);
    for (Object item : value.get()) {
      gen.writeObject(item);
    }
    gen.writeEndArray();
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, DoList<?> value) {
    return value.get().isEmpty();
  }
}
