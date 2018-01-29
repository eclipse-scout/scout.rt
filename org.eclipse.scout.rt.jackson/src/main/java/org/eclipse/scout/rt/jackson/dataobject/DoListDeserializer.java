/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.DoList;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Deserializer for {@link DoList}.
 */
public class DoListDeserializer extends StdDeserializer<DoList<?>> {
  private static final long serialVersionUID = 1L;

  private final JavaType m_listType;

  public DoListDeserializer(JavaType type) {
    super(type);
    m_listType = type;
  }

  @Override
  public DoList<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    DoList<Object> list = new DoList<>();
    for (JsonToken t = p.nextToken(); t != JsonToken.END_ARRAY; t = p.nextToken()) {
      ResolvedType elementType = resolveListElementType(p);
      Object element = p.getCodec().readValue(p, elementType);
      list.add(element);
    }
    return list;
  }

  protected ResolvedType resolveListElementType(JsonParser p) {
    if (p.getCurrentToken() == JsonToken.START_OBJECT) {
      // deserialize object-like JSON structure using specified type binding (DoList<T> generic parameter), fallback to generic DoEntity if no type information available
      return ObjectUtility.nvl(m_listType.getBindings().getBoundType(0), TypeFactory.defaultInstance().constructType(DoEntity.class));
    }
    else {
      // all JSON scalar values are deserialized as bound type (if available) and as fallback as raw object using default jackson typing
      return ObjectUtility.nvl(m_listType.getBindings().getBoundType(0), TypeFactory.unknownType());
    }
  }

  @Override
  public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserialize(p, ctxt);
  }
}
