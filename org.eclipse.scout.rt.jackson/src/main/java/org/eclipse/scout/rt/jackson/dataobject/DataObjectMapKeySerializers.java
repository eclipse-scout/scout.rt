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

import java.util.Locale;

import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeySerializer;
import org.eclipse.scout.rt.jackson.dataobject.id.IIdMapKeySerializer;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;

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
  public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
    Class<?> rawClass = type.getRawClass();
    if (Locale.class.isAssignableFrom(rawClass)) {
      return new LocaleMapKeySerializer();
    }

    if (IId.class.isAssignableFrom(rawClass)) {
      return new IIdMapKeySerializer();
    }

    if (IEnum.class.isAssignableFrom(rawClass)) {
      return new EnumMapKeySerializer();
    }

    return null;
  }
}
