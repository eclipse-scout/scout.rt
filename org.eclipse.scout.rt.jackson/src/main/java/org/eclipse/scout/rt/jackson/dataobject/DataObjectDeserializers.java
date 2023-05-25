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
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.ReferenceType;

/**
 * Deserializer provider for data object deserializer for ({@code DoEntity}, {@code DoValue} and {@code DoList}.
 */
@Bean
public class DataObjectDeserializers extends Deserializers.Base {

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectDeserializers withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  public ScoutDataObjectModuleContext getModuleContext() {
    return m_moduleContext;
  }

  @Override
  public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonDeserializer<?> deserializer = provider.findDeserializer(getModuleContext(), type, config, beanDesc);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findBeanDeserializer(type, config, beanDesc);
  }

  @Override
  public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
      throws JsonMappingException {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonDeserializer<?> deserializer = provider.findReferenceDeserializer(getModuleContext(), refType, config, beanDesc, contentTypeDeserializer, contentDeserializer);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findReferenceDeserializer(refType, config, beanDesc, contentTypeDeserializer, contentDeserializer);
  }

  @Override
  public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
      throws JsonMappingException {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonDeserializer<?> deserializer = provider.findCollectionDeserializer(getModuleContext(), type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findCollectionDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
  }

  @Override
  public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      JsonDeserializer<?> deserializer = provider.findEnumDeserializer(getModuleContext(), type, config, beanDesc);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findEnumDeserializer(type, config, beanDesc);
  }
}
