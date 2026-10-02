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

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.Serializers;

/**
 * Set of custom serializers handling map keys.
 * <p>
 * Subclass and extend this class to provide support for additional serializer for Map keys of various types.
 * <p>
 * <b>Note:</b> The difference between a key serializer and an ordinary one is that the former transforms a map key into
 * a JSON name (i.e. always typed string), whereas the latter writes a JSON value (i.e. any JSON type, including objects
 * and arrays).
 */
@Bean
public class DataObjectMapKeySerializers extends Serializers.Base {

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectMapKeySerializers withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  public ScoutDataObjectModuleContext getModuleContext() {
    return m_moduleContext;
  }

  @Override
  public ValueSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, JsonFormat.Value formatOverrides) {
    for (IDataObjectSerializerProvider provider : BEANS.all(IDataObjectSerializerProvider.class)) {
      ValueSerializer<?> serializer = provider.findKeySerializer(getModuleContext(), type, config, beanDescRef, formatOverrides);
      if (serializer != null) {
        return serializer;
      }
    }
    return super.findSerializer(config, type, beanDescRef, formatOverrides);
  }
}
