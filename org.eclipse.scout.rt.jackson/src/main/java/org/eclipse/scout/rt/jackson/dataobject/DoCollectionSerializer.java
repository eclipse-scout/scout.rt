/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDoCollection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link IDoCollection} subclasses ({@link DoList}, {@link DoSet} and {@link DoCollection}).
 */
public class DoCollectionSerializer<COLLECTION_NODE extends IDoCollection<?, ?>> extends StdSerializer<COLLECTION_NODE> {
  private static final long serialVersionUID = 1L;

  public DoCollectionSerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(COLLECTION_NODE value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    serializeList(value, gen);
  }

  @Override
  public void serializeWithType(COLLECTION_NODE value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
    serializeList(value, gen);
  }

  protected void serializeList(IDoCollection<?, ?> value, JsonGenerator gen) throws IOException {
    // serialize AbstractDoCollection as array using default jackson serializer (includes types if necessary according to actual chosen serializer for each object type)
    gen.writeStartArray();
    gen.setCurrentValue(value);
    for (Object item : value.get()) {
      gen.writeObject(item);
    }
    gen.writeEndArray();
  }

  @Override
  public boolean isEmpty(SerializerProvider provider, COLLECTION_NODE value) {
    return value.get().isEmpty();
  }
}
