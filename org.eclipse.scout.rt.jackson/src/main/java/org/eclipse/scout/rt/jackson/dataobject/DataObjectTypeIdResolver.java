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

import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.SimpleType;

/**
 * {@link TypeIdResolver} implementation handling type resolution of data objects.
 *
 * @see DataObjectInventory
 */
@Bean
public class DataObjectTypeIdResolver extends TypeIdResolverBase {

  private final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  private JavaType m_baseType;

  @Override
  public void init(JavaType baseType) {
    m_baseType = baseType;
  }

  @Override
  public Id getMechanism() {
    return Id.NAME;
  }

  @Override
  public String idFromValue(Object obj) {
    return idFromClass(obj.getClass());
  }

  @Override
  public String idFromBaseType() {
    return idFromClass(m_baseType.getRawClass());
  }

  @Override
  public String idFromValueAndType(Object value, Class<?> clazz) {
    if (value != null) {
      return idFromClass(value.getClass());
    }
    else {
      return idFromClass(clazz);
    }
  }

  /**
   * @return type id to use for serialization of specified class.
   */
  protected String idFromClass(Class<?> c) {
    return m_dataObjectInventory.get().toTypeName(c);
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) {
    return SimpleType.constructUnsafe(m_dataObjectInventory.get().fromTypeName(id));
  }

  @Override
  public String getDescForKnownTypeIds() {
    return m_dataObjectInventory.get().getTypeNameToClassMap()
        .entrySet()
        .stream()
        .map(e -> e.getKey() + " -> " + e.getValue().getName())
        .collect(Collectors.joining("\n"));
  }
}
