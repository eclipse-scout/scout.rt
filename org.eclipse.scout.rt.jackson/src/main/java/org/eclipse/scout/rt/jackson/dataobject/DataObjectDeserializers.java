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

import java.util.Date;

import org.eclipse.scout.rt.dataobject.DoList;
import org.eclipse.scout.rt.dataobject.DoValue;
import org.eclipse.scout.rt.dataobject.IDataObject;
import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.IIdDeserializer;
import org.eclipse.scout.rt.jackson.dataobject.id.TypedIdDeserializer;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.resource.BinaryResource;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
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
  public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
      throws JsonMappingException {
    if (refType.hasRawClass(DoValue.class)) {
      return new DoValueDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
    }
    return super.findReferenceDeserializer(refType, config, beanDesc, contentTypeDeserializer, contentDeserializer);
  }

  @Override
  public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    if (IEnum.class.isAssignableFrom(type)) {
      return new EnumDeserializer(type.asSubclass(IEnum.class));
    }
    return super.findEnumDeserializer(type, config, beanDesc);
  }

  @Override
  public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
    Class<?> rawClass = type.getRawClass();
    if (IDoEntity.class.isAssignableFrom(rawClass)) {
      return new DoEntityDeserializer(m_moduleContext, type);
    }
    else if (DoList.class.isAssignableFrom(rawClass)) {
      return new DoListDeserializer(type);
    }
    else if (Date.class.isAssignableFrom(rawClass)) {
      return new DoDateDeserializer();
    }
    else if (IDataObject.class.isAssignableFrom(rawClass)) {
      return new DataObjectDeserializer(type.getRawClass());
    }
    else if (BinaryResource.class.isAssignableFrom(rawClass)) {
      return new DoBinaryResourceDeserializer();
    }
    else if (IId.class.isAssignableFrom(rawClass)) {
      @SuppressWarnings("unchecked")
      Class<? extends IId<?>> idClass = (Class<? extends IId<?>>) rawClass;
      return new IIdDeserializer(idClass);
    }
    else if (TypedId.class.isAssignableFrom(rawClass)) {
      return new TypedIdDeserializer();
    }
    return super.findBeanDeserializer(type, config, beanDesc);
  }
}
