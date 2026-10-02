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
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.scout.rt.jackson.dataobject.enumeration.EnumMapKeyDeserializer;

import tools.jackson.databind.BeanDescription.Supplier;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.cfg.DeserializerFactoryConfig;
import tools.jackson.databind.deser.BeanDeserializerFactory;
import tools.jackson.databind.deser.DeserializerFactory;
import tools.jackson.databind.type.MapType;

/**
 * Custom {@link DeserializerFactory} that prevents Jackson from specializing {@code Map<Enum, V>} as
 * {@link EnumMap}. Lenient deserialization may return unknown enum keys as {@link String}, which an
 * {@link EnumMap} cannot hold. Explicitly declared {@link EnumMap} types retain Jackson's default handling.
 *
 * @see EnumMapKeyDeserializer
 */
public class ScoutDeserializerFactory extends BeanDeserializerFactory {

  @Serial
  private static final long serialVersionUID = 1L;

  public ScoutDeserializerFactory() {
    this(new DeserializerFactoryConfig());
  }

  public ScoutDeserializerFactory(DeserializerFactoryConfig config) {
    super(config);
  }

  @Override
  public DeserializerFactory withConfig(DeserializerFactoryConfig config) {
    if (_factoryConfig == config) {
      return this;
    }
    return new ScoutDeserializerFactory(config);
  }

  @Override
  public ValueDeserializer<?> createMapDeserializer(DeserializationContext ctxt, MapType type, Supplier beanDescRef) {
    if (type.hasRawClass(Map.class) && type.getKeyType().isEnumType()) {
      type = (MapType) ctxt.getTypeFactory().constructSpecializedType(type, LinkedHashMap.class);
      beanDescRef = ctxt.lazyIntrospectBeanDescriptionForCreation(type);
    }
    return super.createMapDeserializer(ctxt, type, beanDescRef);
  }
}
