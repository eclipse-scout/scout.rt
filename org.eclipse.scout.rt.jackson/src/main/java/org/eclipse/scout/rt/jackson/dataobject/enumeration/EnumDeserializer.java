/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.enumeration;

import org.eclipse.scout.rt.dataobject.enumeration.EnumResolver;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.util.LazyValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link IEnum} values.
 */
public class EnumDeserializer extends StdDeserializer<IEnum> {

  protected final Class<? extends IEnum> m_enumType;
  protected final LazyValue<EnumResolver> m_enumResolver = new LazyValue<>(EnumResolver.class);

  public EnumDeserializer(Class<? extends IEnum> enumType) {
    super(enumType);
    m_enumType = enumType;
  }

  @Override
  public IEnum deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    String rawValue = p.readValueAs(String.class);
    try {
      return m_enumResolver.get().resolve(m_enumType, rawValue);
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(p, "Failed to deserialize IEnum: " + e.getMessage(), rawValue, m_enumType);
    }
  }
}
