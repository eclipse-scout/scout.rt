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

import java.util.Currency;
import java.util.Locale;

import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeyDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.IIdMapKeyDeserializer;
import org.eclipse.scout.rt.platform.Bean;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.KeyDeserializers;

/**
 * Class that defines API for extensions that can provide additional deserializers for deserializer Map keys of various
 * types, from JSON property names.
 * <p>
 * <b>Note:</b> The difference between a key deserializer and an ordinary one is that the former transforms a JSON name
 * into a map key, whereas the latter reads any type of JSON value.
 */
@Bean
public class DataObjectMapKeyDeserializers implements KeyDeserializers {

  protected ScoutDataObjectModuleContext m_moduleContext;

  public DataObjectMapKeyDeserializers withModuleContext(ScoutDataObjectModuleContext moduleContext) {
    m_moduleContext = moduleContext;
    return this;
  }

  public ScoutDataObjectModuleContext getModuleContext() {
    return m_moduleContext;
  }

  @Override
  public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    Class<?> rawClass = type.getRawClass();
    if (Locale.class.isAssignableFrom(rawClass)) {
      return new LocaleMapKeyDeserializer();
    }
    if (Currency.class.isAssignableFrom(rawClass)) {
      return new CurrencyMapKeyDeserializer();
    }
    if (IId.class.isAssignableFrom(rawClass)) {
      @SuppressWarnings("unchecked")
      Class<? extends IId> idClass = rawClass.asSubclass(IId.class);
      return new IIdMapKeyDeserializer(idClass);
    }
    else if (IEnum.class.isAssignableFrom(rawClass)) {
      Class<? extends IEnum> enumClass = rawClass.asSubclass(IEnum.class);
      return new EnumMapKeyDeserializer(enumClass);
    }
    return null;
  }
}
