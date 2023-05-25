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

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.dataobject.DoEntity;
import org.eclipse.scout.rt.dataobject.DoNode;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDoCollection;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.IDoEntityContribution;
import org.eclipse.scout.rt.platform.namespace.NamespaceVersion;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;

/**
 * Serializer for {@link IDoEntity} and all sub-classes.
 */
public class DoEntitySerializer extends StdSerializer<IDoEntity> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  protected final ScoutDataObjectModuleContext m_context;

  public DoEntitySerializer(ScoutDataObjectModuleContext context, JavaType type) {
    super(type);
    m_context = context;
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
    serializeTypeVersion(gen, entity);
    TreeMap<String, DoNode<?>> sortedMap = new TreeMap<>(m_context.getComparator());
    sortedMap.putAll(entity.allNodes());
    for (Map.Entry<String, DoNode<?>> e : sortedMap.entrySet()) {
      gen.setCurrentValue(entity);
      serializeAttribute(e.getKey(), e.getValue(), gen, provider);
    }
    serializeContributions(gen, entity, provider);
  }

  protected void serializeTypeVersion(JsonGenerator gen, IDoEntity entity) throws IOException {
    NamespaceVersion typeVersion = m_dataObjectInventory.get().getTypeVersion(entity.getClass());
    if (typeVersion != null) {
      gen.writeFieldName(m_context.getTypeVersionAttributeName());
      gen.writeString(typeVersion.unwrap());
    }
  }

  protected void serializeContributions(JsonGenerator gen, IDoEntity entity, SerializerProvider provider) throws IOException {
    if (entity.hasContributions()) {
      Collection<IDoEntityContribution> contributions = entity.getContributions();
      validateContributions(entity, contributions);
      gen.writeObjectField(m_context.getContributionsAttributeName(), contributions);
    }
  }

  protected void serializeAttribute(String attributeName, Object obj, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (obj instanceof DoValue) {
      // serialize DoValue value as unwrapped object
      obj = ((DoValue<?>) obj).get();
    }

    if (obj == null) {
      gen.writeObjectField(attributeName, null);
    }
    else if (obj instanceof Collection || obj instanceof IDoCollection) {
      gen.writeObjectField(attributeName, obj);
    }
    else if (obj instanceof Map) {
      serializeMap(attributeName, (Map<?, ?>) obj, gen, provider);
    }
    else if (obj.getClass() == DoEntity.class) {
      // DoEntity exclusion: in special circumstances (e.g. migration scenarios) where a typed DO entity might contain an untyped DO entity,
      // the typed DoEntitySerializer must not be used because expecting different instances of the attributes
      // (e.g. an attribute with type IId results in QualifiedIdSerializer, expecting a IId and not a String as present in the untyped DO entity).
      gen.writeObjectField(attributeName, obj);
    }
    else {
      Optional<AttributeType> attributeType = getAttributeType(attributeName);
      if (attributeType.isPresent()) {
        serializeTypedAttribute(attributeName, obj, gen, provider, attributeType.get().getJavaType());
      }
      else {
        gen.writeObjectField(attributeName, obj);
      }
    }
  }

  /**
   * Serializes a map attribute within {@link IDoEntity}
   */
  protected void serializeMap(String attributeName, Map<?, ?> map, JsonGenerator gen, SerializerProvider provider) throws IOException {
    Optional<AttributeType> typeOpt = getAttributeType(attributeName);
    JsonSerializer<Object> keySerializer = null;
    JsonSerializer<Object> valueSerializer = null;
    if (typeOpt.isPresent()) {
      MapType mapType = (MapType) typeOpt.get().getJavaType();

      // A data object (e.g. DoValue<Map<TestItemDo, String>>) or a pojo (e.g. DoValue<Map<Pojo, String>>) should never be used as a key type of a map,
      // because SdtKeySerializers.Default will be used which would trigger toString on the given object (not really useful).
      keySerializer = provider.findKeySerializer(mapType.getKeyType(), null);

      // Check for != Object is required because findTypedValueSerializer would otherwise return UnknownSerializer.
      // By not setting a serializer here, JsonGenerator#writeObject will be called further below, which will result in a value-based serialization.
      if (mapType.getContentType().getRawClass() != Object.class) {
        valueSerializer = provider.findTypedValueSerializer(mapType.getContentType(), true, null);
      }
    }

    // This "raw" map serialization forces Jackson to includes type information by using the appropriate serializer if a type is available
    // or use the default serialization via key serializer/JsonGenerator#writeObject otherwise.
    gen.writeFieldName(attributeName);
    gen.writeStartObject();
    gen.setCurrentValue(map);
    for (Entry<?, ?> entry : map.entrySet()) {
      // serialize map key
      if (entry.getKey() == null) {
        provider.getDefaultNullKeySerializer().serialize(entry.getKey(), gen, provider);
      }
      else {
        JsonSerializer<Object> ser = keySerializer == null ? provider.findKeySerializer(entry.getKey().getClass(), null) : keySerializer;
        ser.serialize(entry.getKey(), gen, provider);
      }

      // serialize map value
      if (valueSerializer == null || entry.getValue() == null) { // JsonSerializer#serializer must not be called will a null value
        gen.writeObject(entry.getValue());
      }
      else {
        valueSerializer.serialize(entry.getValue(), gen, provider);
      }
    }
    gen.writeEndObject();
  }

  /**
   * Serialize single attribute using appropriate typed value serializer
   */
  protected void serializeTypedAttribute(String attributeName, Object obj, JsonGenerator gen, SerializerProvider provider, JavaType type) throws IOException {
    JsonSerializer<Object> ser = provider.findTypedValueSerializer(type, true, null);
    gen.writeFieldName(attributeName);
    ser.serialize(obj, gen, provider);
  }

  protected Optional<AttributeType> getAttributeType(String attributeName) {
    return m_dataObjectInventory.get().getAttributeDescription(handledType(), attributeName)
        .map(a -> TypeFactoryUtility.toAttributeType(a.getType()))
        .filter(AttributeType::isKnown); // filter completely unknown types, forcing to use the default behavior for unknown types
  }

  protected void validateContributions(IDoEntity doEntity, Collection<IDoEntityContribution> contributions) {
    for (IDoEntityContribution contribution : contributions) {
      Set<Class<? extends IDoEntity>> containerClasses = m_dataObjectInventory.get().getContributionContainers(contribution.getClass());
      Class<? extends IDoEntityContribution> contributionClass = contribution.getClass();
      assertTrue(containerClasses.stream().anyMatch(containerClass -> containerClass.isInstance(doEntity)), "{} is not a valid container class of {}", doEntity.getClass().getSimpleName(), contributionClass.getSimpleName());
    }
  }
}
