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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.ReferenceType;

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
  public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonSerializer<?> serializer = provider.findSerializer(getModuleContext(), type, config, beanDesc);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findSerializer(config, type, beanDesc);
  }

  @Override
  public JsonSerializer<?> findReferenceSerializer(SerializationConfig config, ReferenceType refType, BeanDescription beanDesc, TypeSerializer contentTypeSerializer, JsonSerializer<Object> contentValueSerializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonSerializer<?> serializer = provider.findReferenceSerializer(getModuleContext(), refType, config, beanDesc, contentTypeSerializer, contentValueSerializer);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findReferenceSerializer(config, refType, beanDesc, contentTypeSerializer, contentValueSerializer);
  }

  @Override
  public JsonSerializer<?> findCollectionSerializer(SerializationConfig config, CollectionType type, BeanDescription beanDesc, TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonSerializer<?> serializer = provider.findCollectionSerializer(getModuleContext(), type, config, beanDesc, elementTypeSerializer, elementValueSerializer);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findCollectionSerializer(config, type, beanDesc, elementTypeSerializer, elementValueSerializer);
  }
}
