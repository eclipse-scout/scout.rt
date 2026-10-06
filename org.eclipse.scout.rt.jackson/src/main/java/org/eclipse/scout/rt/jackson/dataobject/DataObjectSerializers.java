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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Value;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.Serializers;
import tools.jackson.databind.type.CollectionLikeType;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.ReferenceType;

/**
 * Serializer provider for data object serializer for ({@code DoEntity}, {@code DoValue} and {@code DoList}.
 */
@Bean
public class DataObjectSerializers extends Serializers.Base {

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectSerializers withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  public ScoutDataObjectModuleContext getModuleContext() {
    return m_moduleContext;
  }

  @Override
  public ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findSerializer(getModuleContext(), type, config, beanDescRef, formatOverrides);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findSerializer(config, type, beanDescRef, formatOverrides);
  }

  @Override
  public ValueSerializer<?> findReferenceSerializer(SerializationConfig config, ReferenceType refType, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer contentTypeSerializer, ValueSerializer<Object> contentValueSerializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findReferenceSerializer(getModuleContext(), refType, config, beanDescRef, formatOverrides, contentTypeSerializer, contentValueSerializer);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findReferenceSerializer(config, refType, beanDescRef, formatOverrides, contentTypeSerializer, contentValueSerializer);
  }

  @Override
  public ValueSerializer<?> findEnumSerializer(SerializationConfig config, JavaType type, Supplier beanDescRef, Value formatOverrides) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findEnumSerializer(getModuleContext(), type, config, beanDescRef, formatOverrides);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findEnumSerializer(config, type, beanDescRef, formatOverrides);
  }

  @Override
  public ValueSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides, TypeSerializer elementTypeSerializer, ValueSerializer<Object> elementValueSerializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findCollectionSerializer(getModuleContext(), type, config, beanDescRef, formatOverrides, elementTypeSerializer, elementValueSerializer);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findCollectionSerializer(config, type, beanDescRef, formatOverrides, elementTypeSerializer, elementValueSerializer);
  }

  @Override
  public ValueSerializer<?> findCollectionLikeSerializer(SerializationConfig config, CollectionLikeType type, Supplier beanDescRef, Value formatOverrides, TypeSerializer elementTypeSerializer, ValueSerializer<Object> elementValueSerializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findCollectionLikeSerializer(getModuleContext(), type, config, beanDescRef, formatOverrides, elementTypeSerializer, elementValueSerializer);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findCollectionLikeSerializer(config, type, beanDescRef, formatOverrides, elementTypeSerializer, elementValueSerializer);
  }
}
