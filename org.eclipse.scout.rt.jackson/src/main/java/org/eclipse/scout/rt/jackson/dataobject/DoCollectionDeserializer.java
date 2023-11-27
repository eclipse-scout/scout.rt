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
import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Supplier;

import org.eclipse.scout.rt.dataobject.DoCollection;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoSet;
import org.eclipse.scout.rt.dataobject.IDoCollection;
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
 * Deserializer for {@link IDoCollection} subclasses ({@link DoList}, {@link DoSet} and {@link DoCollection}).
 */
public class DoCollectionDeserializer<COLLECTION_NODE extends IDoCollection<?, ?>> extends StdDeserializer<COLLECTION_NODE> {

  private static final long serialVersionUID = 1L;

  private final JavaType m_collectionType;
  private final Supplier<IDoCollection<Object, ? extends Collection<Object>>> m_nodeSupplier;

  public DoCollectionDeserializer(JavaType type, Supplier<IDoCollection<Object, ? extends Collection<Object>>> nodeSupplier) {
    super(type);
    m_collectionType = type;
    m_nodeSupplier = nodeSupplier;
  }

  @Override
  public COLLECTION_NODE deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return deserializeInternal(p, ctxt, m_nodeSupplier.get());
  }

  protected COLLECTION_NODE deserializeInternal(JsonParser p, DeserializationContext ctxt, IDoCollection<Object, ? extends Collection<Object>> collection) throws IOException {
    p.setCurrentValue(collection);
    for (JsonToken t = p.nextToken(); t != JsonToken.END_ARRAY; t = p.nextToken()) {
      ResolvedType elementType = resolveListElementType(p);
      Object element = p.getCodec().readValue(p, elementType);
      //noinspection unchecked
      collection.add(element);
    }
    //noinspection unchecked
    return (COLLECTION_NODE) collection;
  }

  protected ResolvedType resolveListElementType(JsonParser p) {
    if (p.getCurrentToken() == JsonToken.START_OBJECT) {
      // deserialize object-like JSON structure using specified type binding (DoList<T>/DoSet<T>/DoCollection<T> generic parameter), fallback to generic DoEntity if no type information available
      JavaType collectionItemType = m_collectionType.getBindings().getBoundType(0);
      if (collectionItemType == null || collectionItemType.getRawClass() == Object.class) {
        // use DoEntity as default value for missing or unspecified object types
        return TypeFactory.defaultInstance().constructType(DoEntity.class);
      }
      return collectionItemType;
    }
    else {
      // all JSON scalar values are deserialized as bound type (if available) and as fallback as raw object using default jackson typing
      return ObjectUtility.nvl(m_collectionType.getBindings().getBoundType(0), resolveFallbackListElementType(p));
    }
  }

  protected ResolvedType resolveFallbackListElementType(JsonParser p) {
    if (p.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
      // deserialize floating point numbers as BigDecimal
      return TypeFactory.defaultInstance().constructType(BigDecimal.class);
    }
    // JSON scalar values are deserialized as raw object using default jackson typing
    return TypeFactory.unknownType();
  }

  @Override
  public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
    return deserialize(p, ctxt);
  }

  @Override
  public COLLECTION_NODE getNullValue(DeserializationContext ctxt) {
    //noinspection unchecked
    return (COLLECTION_NODE) m_nodeSupplier.get(); // create empty DO collection for null value
  }
}
