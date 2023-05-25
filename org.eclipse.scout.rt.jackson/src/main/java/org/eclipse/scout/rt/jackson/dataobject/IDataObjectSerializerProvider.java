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

import org.eclipse.scout.rt.platform.ApplicationScoped;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.ReferenceType;

/**
 * Provider for own Jackson serializers and deserializers.
 */
@ApplicationScoped
public interface IDataObjectSerializerProvider {

  /**
   * Finds a serializer.
   * <p>
   * Called from Jackson by {@link Serializers.Base#findSerializer(SerializationConfig, JavaType, BeanDescription)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  JsonSerializer<?> findSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc);

  /**
   * Finds a deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findBeanDeserializer(JavaType, DeserializationConfig, BeanDescription)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  JsonDeserializer<?> findDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc);

  /**
   * Finds a key serializer.
   * <p>
   * Called from Jackson by {@link Serializers.Base#findSerializer(SerializationConfig, JavaType, BeanDescription)} for
   * key serializers.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default JsonSerializer<?> findKeySerializer(ScoutDataObjectModuleContext moduleContext, JavaType type, SerializationConfig config, BeanDescription beanDesc) {
    return null;
  }

  /**
   * Finds a key deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findBeanDeserializer(JavaType, DeserializationConfig, BeanDescription)} for key
   * deserializers.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default KeyDeserializer findKeyDeserializer(ScoutDataObjectModuleContext moduleContext, JavaType type, DeserializationConfig config, BeanDescription beanDesc) {
    return null;
  }

  /**
   * Finds a reference serializer.
   * <p>
   * Called from Jackson by
   * {@link Serializers.Base#findReferenceSerializer(SerializationConfig, ReferenceType, BeanDescription, TypeSerializer, JsonSerializer)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default JsonSerializer<?> findReferenceSerializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, SerializationConfig config, BeanDescription beanDesc, TypeSerializer contentTypeSerializer,
      JsonSerializer<Object> contentValueSerializer) {
    return null;
  }

  /**
   * Finds a reference deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findReferenceDeserializer(ReferenceType, DeserializationConfig, BeanDescription, TypeDeserializer, JsonDeserializer)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default JsonDeserializer<?> findReferenceDeserializer(ScoutDataObjectModuleContext moduleContext, ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer,
      JsonDeserializer<?> contentDeserializer) {
    return null;
  }

  /**
   * Finds a collection serializer.
   * <p>
   * Called from Jackson by
   * {@link Serializers.Base#findCollectionSerializer(SerializationConfig, CollectionType, BeanDescription, TypeSerializer, JsonSerializer)}.
   *
   * @return <code>null</code> if no matching serializer can be provided.
   */
  default JsonSerializer<?> findCollectionSerializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, SerializationConfig config, BeanDescription beanDesc, TypeSerializer elementTypeSerializer,
      JsonSerializer<Object> elementValueSerializer) {
    return null;
  }

  /**
   * Finds a collection deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findCollectionDeserializer(CollectionType, DeserializationConfig, BeanDescription, TypeDeserializer, JsonDeserializer)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  default JsonDeserializer<?> findCollectionDeserializer(ScoutDataObjectModuleContext moduleContext, CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer,
      JsonDeserializer<?> elementDeserializer) {
    return null;
  }

  /**
   * Finds an enum deserializer.
   * <p>
   * Called from Jackson by
   * {@link Deserializers.Base#findEnumDeserializer(Class, DeserializationConfig, BeanDescription)}.
   *
   * @return <code>null</code> if no matching deserializer can be provided.
   */
  // no counterpart findEnumSerializer because not supported by Serializers.Base
  default JsonDeserializer<?> findEnumDeserializer(ScoutDataObjectModuleContext moduleContext, Class<?> type, DeserializationConfig config, BeanDescription beanDesc) {
    return null;
  }
}
