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
import org.eclipse.scout.rt.dataobject.IDataObject;
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
      if (attributeType.isPresent()
          && (!m_context.isLenientMode() || attributeType.get().getJavaType().isTypeOrSuperTypeOf(obj.getClass()))
          && !(attributeType.get().getJavaType().isTypeOrSubTypeOf(IDataObject.class))) {
        // Use serialization by typed attribute if:
        // (1) an attribute type is present
        // (2) if lenient mode, only if attribute type matches (data object might have an invalid structure, e.g. string instead of an enum if deserialized lenient)
        // (3) if attribute type is not a data object type (favor value based serialization for attributes declared as data object or subclasses/subinterfaces)
        serializeTypedAttribute(attributeName, obj, gen, provider, attributeType.get().getJavaType());
      }
      else {
        // use serialization by value
        gen.writeObjectField(attributeName, obj);
      }
    }
  }

  /**
   * Serializes a map attribute within {@link IDoEntity}
   */
  protected void serializeMap(String attributeName, Map<?, ?> map, JsonGenerator gen, SerializerProvider provider) throws IOException {
    Optional<AttributeType> typeOpt = getAttributeType(attributeName);
    JavaType keyType = null;
    JsonSerializer<Object> keySerializer = null;
    JavaType valueType = null;
    JsonSerializer<Object> valueSerializer = null;
    if (typeOpt.isPresent()) {
      MapType mapType = (MapType) typeOpt.get().getJavaType();

      // A data object (e.g. DoValue<Map<TestItemDo, String>>) or a pojo (e.g. DoValue<Map<Pojo, String>>) should never be used as a key type of a map,
      // because SdtKeySerializers.Default will be used which would trigger toString on the given object (not really useful).
      keyType = mapType.getKeyType();
      keySerializer = provider.findKeySerializer(keyType, null);

      // Check for != Object is required because findTypedValueSerializer would otherwise return UnknownSerializer.
      // By not setting a serializer here, JsonGenerator#writeObject will be called further below, which will result in a value-based serialization.
      valueType = mapType.getContentType();
      if (valueType.getRawClass() != Object.class) {
        valueSerializer = provider.findTypedValueSerializer(valueType, true, null);
      }
    }

    // This "raw" map serialization forces Jackson to include type information by using the appropriate serializer if a type is available
    // or use the default serialization via key serializer/JsonGenerator#writeObject otherwise.
    gen.writeFieldName(attributeName);
    gen.writeStartObject();
    gen.setCurrentValue(map);
    for (Entry<?, ?> entry : map.entrySet()) {
      // serialize map key
      Object key = entry.getKey();
      if (key == null) {
        provider.getDefaultNullKeySerializer().serialize(key, gen, provider);
      }
      else {
        JsonSerializer<Object> ser;
        if (keySerializer == null || (m_context.isLenientMode() && !keyType.isTypeOrSuperTypeOf(key.getClass()))) {
          // use serialization by value either:
          // - if no type information is available (key serializer is null)
          // - if lenient mode and declared key type is not equals/not a super type of the given value
          ser = provider.findKeySerializer(key.getClass(), null);
        }
        else {
          ser = keySerializer;
        }
        ser.serialize(key, gen, provider);
      }

      // serialize map value
      Object value = entry.getValue();
      if (valueSerializer == null || value == null || (m_context.isLenientMode() && !valueType.isTypeOrSuperTypeOf(value.getClass()))) {
        // use serialization by value either:
        // - if no type information is available (value serializer is null)
        // - if value is null (JsonSerializer#serializer must not be called will a null value)
        // - if lenient mode and declared value type is not equals/not a super type of the given value
        gen.writeObject(value);
      }
      else {
        valueSerializer.serialize(value, gen, provider);
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
