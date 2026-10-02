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

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.fasterxml.jackson.annotation.JsonFormat;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.Serializers;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.ReferenceType;

/**
 * Provider for own Jackson serializers and deserializers.
 */
@ApplicationScoped
public interface IDataObjectSerializerProvider {

  /**
   * Finds a serializer.
   * <p>
   * Called from Jackson by {@link Serializers.Base#findSerializer(SerializationConfig, JavaType, BeanDescription.Supplier, JsonFormat.Value)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  ValueSerializer<?> findSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides);

  /**
   * Finds a deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findBeanDeserializer(JavaType, DeserializationConfig, BeanDescription.Supplier)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  ValueDeserializer<?> findDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef);

  /**
   * Finds a key serializer.
   * <p>
   * Called from Jackson by {@link Serializers.Base#findSerializer(SerializationConfig, JavaType, BeanDescription.Supplier, JsonFormat.Value)} for
   * key serializers.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default ValueSerializer<?> findKeySerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    return null;
  }

  /**
   * Finds a key deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findBeanDeserializer(JavaType, DeserializationConfig, BeanDescription.Supplier)} for key
   * deserializers.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default KeyDeserializer findKeyDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
    return null;
  }

  /**
   * Finds a reference serializer.
   * <p>
   * Called from Jackson by
   * {@link Serializers.Base#findReferenceSerializer(SerializationConfig, ReferenceType, BeanDescription.Supplier, JsonFormat.Value, TypeSerializer, ValueSerializer)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default ValueSerializer<?> findReferenceSerializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer contentTypeSerializer,
      ValueSerializer<Object> contentValueSerializer) {
    return null;
  }

  /**
   * Finds a reference deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findReferenceDeserializer(ReferenceType, DeserializationConfig, BeanDescription.Supplier, TypeDeserializer, ValueDeserializer)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default ValueDeserializer<?> findReferenceDeserializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, DeserializationConfig config, BeanDescription.Supplier beanDescRef, TypeDeserializer contentTypeDeserializer,
      ValueDeserializer<?> contentDeserializer) {
    return null;
  }

  /**
   * Finds a collection serializer.
   * <p>
   * Called from Jackson by
   * {@link Serializers.Base#findCollectionSerializer(SerializationConfig, CollectionType, BeanDescription.Supplier, JsonFormat.Value, TypeSerializer, ValueSerializer)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default ValueSerializer<?> findCollectionSerializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer elementTypeSerializer,
      ValueSerializer<Object> elementValueSerializer) {
    return null;
  }

  /**
   * Finds a collection deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findCollectionDeserializer(CollectionType, DeserializationConfig, BeanDescription.Supplier, TypeDeserializer, ValueDeserializer)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default ValueDeserializer<?> findCollectionDeserializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef, TypeDeserializer elementTypeDeserializer,
      ValueDeserializer<?> elementDeserializer) {
    return null;
  }

  /**
   * Finds an enum serializer.
   * <p>
   * Called from Jackson by
   * {@link Serializers.Base#findEnumSerializer(SerializationConfig, JavaType, BeanDescription.Supplier, JsonFormat.Value)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default ValueSerializer<?> findEnumSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    return null;
  }

  /**
   * Finds an enum deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findEnumDeserializer(JavaType, DeserializationConfig, BeanDescription.Supplier)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default ValueDeserializer<?> findEnumDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
    return null;
  }
}
