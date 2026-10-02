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

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.jsontype.TypeDeserializer;
import tools.jackson.databind.type.CollectionType;
import tools.jackson.databind.type.ReferenceType;

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
  public ValueDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription.Supplier beanDescRef) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueDeserializer<?> deserializer = provider.findDeserializer(getModuleContext(), type, config, beanDescRef);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findBeanDeserializer(type, config, beanDescRef);
  }

  @Override
  public ValueDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, Supplier beanDescRef, TypeDeserializer contentTypeDeserializer, ValueDeserializer<?> contentDeserializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueDeserializer<?> deserializer = provider.findReferenceDeserializer(getModuleContext(), refType, config, beanDescRef, contentTypeDeserializer, contentDeserializer);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findReferenceDeserializer(refType, config, beanDescRef, contentTypeDeserializer, contentDeserializer);
  }

  @Override
  public ValueDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config, Supplier beanDescRef, TypeDeserializer elementTypeDeserializer, ValueDeserializer<?> elementDeserializer) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueDeserializer<?> deserializer = provider.findCollectionDeserializer(getModuleContext(), type, config, beanDescRef, elementTypeDeserializer, elementDeserializer);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findCollectionDeserializer(type, config, beanDescRef, elementTypeDeserializer, elementDeserializer);
  }

  @Override
  public ValueDeserializer<?> findEnumDeserializer(JavaType type, DeserializationConfig config, Supplier beanDescRef) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueDeserializer<?> deserializer = provider.findEnumDeserializer(getModuleContext(), type, config, beanDescRef);
      if (deserializer != null) {
        return deserializer;
      }
    }
    return super.findEnumDeserializer(type, config, beanDescRef);
  }

  @Override
  public boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
    return false;
  }
}
