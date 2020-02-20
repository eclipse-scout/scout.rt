/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

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
    if (IId.class.isAssignableFrom(rawClass)) {
      @SuppressWarnings("unchecked")
      Class<? extends IId<?>> idClass = (Class<? extends IId<?>>) rawClass.asSubclass(IId.class);
      return new IIdMapKeyDeserializer(idClass);
    }
    else if (IEnum.class.isAssignableFrom(rawClass)) {
      Class<? extends IEnum> enumClass = (Class<? extends IEnum>) rawClass.asSubclass(IEnum.class);
      return new EnumMapKeyDeserializer(enumClass);
    }
    return null;
  }
}
