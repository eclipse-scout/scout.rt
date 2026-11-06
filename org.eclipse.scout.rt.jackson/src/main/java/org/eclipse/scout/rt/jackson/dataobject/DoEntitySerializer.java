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

import static org.eclipse.scout.rt.platform.util.Assertions.assertTrue;

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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.WritableTypeId;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.type.MapType;

/**
 * Serializer for {@link IDoEntity} and all sub-classes.
 */
public class DoEntitySerializer extends StdSerializer<IDoEntity> {

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  protected final ScoutDataObjectModuleContext m_context;

  public DoEntitySerializer(ScoutDataObjectModuleContext context, JavaType type) {
    super(type);
    m_context = context;
  }

  @Override
  public void serialize(IDoEntity entity, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writeStartObject();
    serializeAttributes(entity, gen, ctxt);
    gen.writeEndObject();
  }

  @Override
  public void serializeWithType(IDoEntity entity, JsonGenerator gen, SerializationContext ctxt, TypeSerializer typeSer) throws JacksonException {
    WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, ctxt, typeSer.typeId(entity, JsonToken.START_OBJECT));
    serializeAttributes(entity, gen, ctxt);
    typeSer.writeTypeSuffix(gen, ctxt, typeIdDef);
  }

  /**
   * Serialize all fields of specified {@link IDoEntity} sorted alphabetically.
   */
  protected void serializeAttributes(IDoEntity entity, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    serializeTypeVersion(gen, entity);
    TreeMap<String, DoNode<?>> sortedMap = new TreeMap<>(m_context.getComparator());
    sortedMap.putAll(entity.allNodes());
    for (Map.Entry<String, DoNode<?>> e : sortedMap.entrySet()) {
      gen.assignCurrentValue(entity);
      serializeAttribute(e.getKey(), e.getValue(), gen, ctxt);
    }
    serializeContributions(gen, entity, ctxt);
  }

  protected void serializeTypeVersion(JsonGenerator gen, IDoEntity entity) throws JacksonException {
    NamespaceVersion typeVersion = m_dataObjectInventory.get().getTypeVersion(entity.getClass());
    if (typeVersion != null) {
      gen.writeName(m_context.getTypeVersionAttributeName());
      gen.writeString(typeVersion.unwrap());
    }
  }

  protected void serializeContributions(JsonGenerator gen, IDoEntity entity, SerializationContext ctxt) throws JacksonException {
    if (entity.hasContributions()) {
      //noinspection deprecation
      Collection<IDoEntity> contributions = entity.getAllContributions();
      validateContributions(entity, contributions);
      gen.writePOJOProperty(m_context.getContributionsAttributeName(), contributions);
    }
  }

  protected void serializeAttribute(String attributeName, Object obj, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    if (obj instanceof DoValue) {
      // serialize DoValue value as unwrapped object
      obj = ((DoValue<?>) obj).get();
    }

    if (obj == null) {
      gen.writePOJOProperty(attributeName, null);
    }
    else if (obj instanceof Collection || obj instanceof IDoCollection) {
      gen.writePOJOProperty(attributeName, obj);
    }
    else if (obj instanceof Map) {
      serializeMap(attributeName, (Map<?, ?>) obj, gen, ctxt);
    }
    else if (obj.getClass() == DoEntity.class) {
      // DoEntity exclusion: in special circumstances (e.g. migration scenarios) where a typed DO entity might contain an untyped DO entity,
      // the typed DoEntitySerializer must not be used because expecting different instances of the attributes
      // (e.g. an attribute with type IId results in QualifiedIdSerializer, expecting a IId and not a String as present in the untyped DO entity).
      gen.writePOJOProperty(attributeName, obj);
    }
    else {
      JavaType declaredAttributeType = getAttributeType(attributeName, ctxt).map(AttributeType::getJavaType).orElse(null);
      if (isSerializeByDeclaredType(declaredAttributeType, obj.getClass())) {
        serializeTypedAttribute(attributeName, obj, gen, ctxt, declaredAttributeType);
      }
      else {
        // use serialization by value
        gen.writePOJOProperty(attributeName, obj);
      }
    }
  }

  /**
   * Serializes a map attribute within {@link IDoEntity}
   */
  protected void serializeMap(String attributeName, Map<?, ?> map, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    Optional<AttributeType> typeOpt = getAttributeType(attributeName, ctxt);
    JavaType keyType = null;
    ValueSerializer<Object> keySerializer = null;
    JavaType valueType = null;
    ValueSerializer<Object> valueSerializer = null;
    if (typeOpt.isPresent()) {
      MapType mapType = (MapType) typeOpt.get().getJavaType();

      // A data object (e.g. DoValue<Map<TestItemDo, String>>) or a pojo (e.g. DoValue<Map<Pojo, String>>) should never be used as a key type of a map,
      // because SdtKeySerializers.Default will be used which would trigger toString on the given object (not really useful).
      keyType = mapType.getKeyType();
      keySerializer = ctxt.findKeySerializer(keyType, null);

      // Check for != Object is required because findTypedValueSerializer would otherwise return UnknownSerializer.
      // By not setting a serializer here, JsonGenerator#writeObject will be called further below, which will result in a value-based serialization.
      valueType = mapType.getContentType();
      if (valueType.getRawClass() != Object.class) {
        valueSerializer = ctxt.findTypedValueSerializer(valueType, true);
      }
    }

    // This "raw" map serialization forces Jackson to include type information by using the appropriate serializer if a type is available
    // or use the default serialization via key serializer/JsonGenerator#writeObject otherwise.
    gen.writeName(attributeName);
    gen.writeStartObject();
    gen.assignCurrentValue(map);
    for (Entry<?, ?> entry : map.entrySet()) {
      // serialize map key
      Object key = entry.getKey();
      if (key == null) {
        ctxt.findNullKeySerializer(keyType, null).serialize(key, gen, ctxt);
      }
      else {
        ValueSerializer<Object> ser;
        if (keySerializer == null || (m_context.isLenientMode() && !keyType.isTypeOrSuperTypeOf(key.getClass()))) {
          // use serialization by value either:
          // - if no type information is available (key serializer is null)
          // - if lenient mode and declared key type is not equals/not a super type of the given value
          ser = ctxt.findKeySerializer(key.getClass(), null);
        }
        else {
          ser = keySerializer;
        }
        ser.serialize(key, gen, ctxt);
      }

      // serialize map value
      Object value = entry.getValue();
      if (valueSerializer != null && value != null && isSerializeByDeclaredType(valueType, value.getClass())) {
        // Use serialization by typed attribute if:
        // (1) a value serializer and a value is available
        // (2) if attribute value type eligible for typed serialization
        valueSerializer.serialize(value, gen, ctxt);
      }
      else {
        gen.writePOJO(value);
      }
    }
    gen.writeEndObject();
  }

  /**
   * Checks if serialization according to declared type should be used. Conditions are:
   * <ul>
   * <li>Declared attribute type is available (e.g. attribute is not part of an untyped DoEntity</li>
   * <li>If lenient mode: Only if attribute type matches the declared attribute type (data object might have an invalid
   * structure, e.g. string instead of an enum if deserialized lenient)</li>
   * <li>Attribute type is not a data object type or subclass/subinterface (favor value based serialization for
   * attributes declared as data object or subclasses/subinterfaces)</li>
   * </ul>
   * <br>
   * NOTE: If this method is changed, check the similar statement in
   * {@link DoCollectionSerializer#serializeList(Iterable, JsonGenerator, SerializationContext)}
   *
   * @return {@code true} if for given {@code declaredAttributeType} and given {@code attributeType} serialization using
   * the declared type should be used. Returns {@code false} otherwise.
   */
  protected boolean isSerializeByDeclaredType(JavaType declaredAttributeType, Class<?> attributeType) {
    return declaredAttributeType != null
        && (!m_context.isLenientMode() || declaredAttributeType.isTypeOrSuperTypeOf(attributeType))
        && !(declaredAttributeType.isTypeOrSubTypeOf(IDataObject.class));
  }

  /**
   * Serialize single attribute using appropriate typed value serializer
   */
  protected void serializeTypedAttribute(String attributeName, Object obj, JsonGenerator gen, SerializationContext ctxt, JavaType type) throws JacksonException {
    ValueSerializer<Object> ser = ctxt.findTypedValueSerializer(type, true);
    gen.writeName(attributeName);
    ser.serialize(obj, gen, ctxt);
  }

  protected Optional<AttributeType> getAttributeType(String attributeName, SerializationContext ctxt) {
    return m_dataObjectInventory.get().getAttributeDescription(handledType().asSubclass(IDoEntity.class), attributeName)
        .map(a -> TypeFactoryUtility.toAttributeType(a.getType(), ctxt))
        .filter(AttributeType::isKnown); // filter completely unknown types, forcing to use the default behavior for unknown types
  }

  protected void validateContributions(IDoEntity doEntity, Collection<IDoEntity> contributions) {
    for (IDoEntity contribution0 : contributions) {
      if (!(contribution0 instanceof IDoEntityContribution contribution)) {
        continue; // Skip validation for unknown contributions
      }
      Set<Class<? extends IDoEntity>> containerClasses = m_dataObjectInventory.get().getContributionContainers(contribution.getClass());
      Class<? extends IDoEntityContribution> contributionClass = contribution.getClass();
      assertTrue(containerClasses.stream().anyMatch(containerClass -> containerClass.isInstance(doEntity)), "{} is not a valid container class of {}", doEntity.getClass().getSimpleName(), contributionClass.getSimpleName());
    }
  }
}
