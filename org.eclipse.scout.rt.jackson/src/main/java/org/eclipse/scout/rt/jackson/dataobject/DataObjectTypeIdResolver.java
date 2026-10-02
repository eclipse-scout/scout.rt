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

import java.io.Serial;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.TypeIdResolver;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.SimpleType;

/**
 * {@link TypeIdResolver} implementation handling type resolution of data objects.
 *
 * @see DataObjectInventory
 */
@Bean
public class DataObjectTypeIdResolver extends TypeIdResolverBase {

  @Serial
  private static final long serialVersionUID = 1L;

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
  public String idFromValue(DatabindContext ctxt, Object value) {
    return idFromClass(value.getClass());
  }

  @Override
  public String idFromBaseType(DatabindContext ctxt) {
    return idFromClass(m_baseType.getRawClass());
  }

  @Override
  public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) {
    if (value != null) {
      return idFromClass(value.getClass());
    }
    else {
      return idFromClass(suggestedType);
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
