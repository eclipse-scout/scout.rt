/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.util.ObjectUtility;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DoMapDeserializer<MAP extends Map<?, ?>> extends StdDeserializer<MAP> {

  private static final long serialVersionUID = 1L;

  private final MapType m_mapType;
//  private final Supplier<IDoCollection<Object, ? extends Collection<Object>>> m_nodeSupplier;

  public DoMapDeserializer(MapType type) {
    super(type);
    m_mapType = type;
  }

  @Override
  public MAP deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeInternal(p, ctxt);
  }

  protected MAP deserializeInternal(JsonParser p, DeserializationContext ctxt) throws IOException {
    Map<Object, Object> map = new HashMap<>();
    p.setCurrentValue(map);

//    String key;
//    if (p.isExpectedStartObjectToken()) {
//      key = p.nextFieldName();
//    } else {
//      JsonToken t = p.currentToken();
//      if (t == JsonToken.END_OBJECT) {
//        return (MAP) map;
//      }
//      if (t != JsonToken.FIELD_NAME) {
//        ctxt.reportWrongTokenException(this, JsonToken.FIELD_NAME, null);
//      }
//      key = p.currentName();
//    }

    String keyStr;
    if (p.isExpectedStartObjectToken()) {
      keyStr = p.nextFieldName();
    }
    else {
      JsonToken t = p.currentToken();
      if (t != JsonToken.FIELD_NAME) {
        if (t == JsonToken.END_OBJECT) {
          return (MAP) map;
        }
        ctxt.reportWrongTokenException(this, JsonToken.FIELD_NAME, null);
      }
      keyStr = p.currentName();
    }

    for (; keyStr != null; keyStr = p.nextFieldName()) {

      KeyDeserializer keyDes = ctxt.findKeyDeserializer(m_mapType.getKeyType(), null);
      Object key = keyDes.deserializeKey(keyStr, ctxt);

      JsonToken t = p.nextToken();
      ResolvedType elementType = resolveMapElementType(p);
      Object element = p.getCodec().readValue(p, elementType);
      map.put(key, element);
    }

//      for (JsonToken t = p.nextToken(); t != JsonToken.END_ARRAY; t = p.nextToken()) {
//      ResolvedType elementType = resolveListElementType(p);
//      Object element = p.getCodec().readValue(p, elementType);
//      //noinspection unchecked
//      collection.add(element);
//    }
    //noinspection unchecked
    return (MAP) map;
  }

  protected ResolvedType resolveMapElementType(JsonParser p) {
    if (p.getCurrentToken() == JsonToken.START_OBJECT) {
      // deserialize object-like JSON structure using specified type binding (DoList<T>/DoSet<T>/DoCollection<T> generic parameter), fallback to generic DoEntity if no type information available
      //JavaType collectionItemType = m_mapType.getBindings().getBoundType(0);
      JavaType contentType = m_mapType.getContentType();
      if (contentType == null || contentType.getRawClass() == Object.class) {
        // use DoEntity as default value for missing or unspecified object types
        return TypeFactory.defaultInstance().constructType(DoEntity.class);
      }
      return contentType;
    }
    else {
      // all JSON scalar values are deserialized as bound type (if available) and as fallback as raw object using default jackson typing
      return ObjectUtility.nvl(m_mapType.getContentType(), TypeFactory.unknownType());
    }
  }

  @Override
  public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserialize(p, ctxt);
  }

  @Override
  public MAP getNullValue(DeserializationContext ctxt) {
    //noinspection unchecked
//    return (MAP) new HashMap<>();
    return null;
  }
}
