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
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.DoNode;
import org.eclipse.scout.rt.platform.dataobject.DoValue;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializer for {@link IDoEntity} and all sub-classes.
 */
public class DoEntitySerializer extends StdSerializer<IDoEntity> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  public DoEntitySerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(IDoEntity entity, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    serializeAttributes(entity, gen, provider);
    gen.writeEndObject();
  }

  @Override
  public void serializeWithType(IDoEntity entity, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
    WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(entity, JsonToken.START_OBJECT));
    serializeAttributes(entity, gen, provider);
    typeSer.writeTypeSuffix(gen, typeIdDef);
  }

  /**
   * Serialize all fields of specified {@link IDoEntity} sorted alphabetically.
   */
  protected void serializeAttributes(IDoEntity entity, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.setCurrentValue(entity);
    TreeMap<String, DoNode<?>> sortedMap = new TreeMap<>(entity.allNodes());
    for (Map.Entry<String, DoNode<?>> e : sortedMap.entrySet()) {
      serializeAttributes(e.getKey(), e.getValue(), gen, provider);
    }
  }

  protected void serializeAttributes(String attributeName, Object obj, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (obj instanceof DoValue) {
      // serialize DoValue value as unwrapped object
      obj = ((DoValue<?>) obj).get();
    }
    if (obj instanceof Collection) {
      serializeCollection(attributeName, (Collection<?>) obj, gen, provider);
    }
    else if (obj instanceof Map) {
      serializeMap(attributeName, (Map<?, ?>) obj, gen, provider);
    }
    else {
      gen.writeObjectField(attributeName, obj);
    }
  }

  /**
   * Serializes a collection attribute within {@link IDoEntity}
   */
  protected void serializeCollection(String attributeName, Collection<?> collection, JsonGenerator gen, SerializerProvider provider) throws IOException {
    Optional<JavaType> type = getJavaType(attributeName);
    if (type.isPresent()) {
      serializeTypedAttribute(attributeName, collection, gen, provider, type);
    }
    else {
      // If no type definition is available, serialize all collection-like types (e.g. all {@link List} and {@link Set} implementations) as array using default jackson serializer.
      // This "raw" array serialization forces Jackson to includes type information if necessary according to actual chosen serializer for each object type (see writeObject(...) call).
      gen.writeFieldName(attributeName);
      gen.writeStartArray();
      gen.setCurrentValue(collection);
      for (Object item : collection) {
        gen.writeObject(item);
      }
      gen.writeEndArray();
    }
  }

  /**
   * Serializes a map attribute within {@link IDoEntity}
   */
  protected void serializeMap(String attributeName, Map<?, ?> map, JsonGenerator gen, SerializerProvider provider) throws IOException {
    Optional<JavaType> type = getJavaType(attributeName);
    if (type.isPresent()) {
      serializeTypedAttribute(attributeName, map, gen, provider, type);
    }
    else {
      // If no type definition is available, serialize all {@link Map} types as object  using default jackson serializer
      // This "raw" map serialization forces Jackson to includes type information if necessary according to actual chosen serializer for each object type (see writeObject(...) call).
      gen.writeFieldName(attributeName);
      gen.writeStartObject();
      gen.setCurrentValue(map);
      for (Entry<?, ?> entry : map.entrySet()) {
        // serialize map key
        if (entry.getKey() == null) {
          provider.getDefaultNullKeySerializer().serialize(entry.getKey(), gen, provider);
        }
        else {
          provider.findKeySerializer(entry.getKey().getClass(), null).serialize(entry.getKey(), gen, provider);
        }
        // serialize map value
        gen.writeObject(entry.getValue());
      }
      gen.writeEndObject();
    }
  }

  /**
   * Serialize single attribute using appropriate typed value serializer
   */
  protected void serializeTypedAttribute(String attributeName, Object obj, JsonGenerator gen, SerializerProvider provider, Optional<JavaType> type) throws IOException {
    JsonSerializer<Object> ser = provider.findTypedValueSerializer(type.get(), true, null);
    gen.writeFieldName(attributeName);
    ser.serialize(obj, gen, provider);
  }

  protected Optional<JavaType> getJavaType(String attributeName) {
    return m_dataObjectInventory.get().getAttributeDescription(handledType(), attributeName)
        .map(a -> TypeFactoryUtility.toJavaType(a.getType()))
        .filter(t -> t.getRawClass() != Object.class); // filter completely unknown types, forcing to use the default behavior for unknown types
  }
}
