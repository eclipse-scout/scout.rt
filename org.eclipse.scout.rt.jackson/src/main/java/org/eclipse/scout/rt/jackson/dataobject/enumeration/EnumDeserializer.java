/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.enumeration;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.enumeration.EnumResolver;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link IEnum} values.
 */
public class EnumDeserializer extends StdDeserializer<IEnum> {
  private static final long serialVersionUID = 1L;

  protected final Class<? extends IEnum> m_enumType;
  protected final LazyValue<EnumResolver> m_enumResolver = new LazyValue<>(EnumResolver.class);

  public EnumDeserializer(Class<? extends IEnum> enumType) {
    super(enumType);
    m_enumType = enumType;
  }

  @Override
  public IEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String rawValue = p.readValueAs(String.class);
    try {
      return m_enumResolver.get().resolve(m_enumType, rawValue);
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(p, "Failed to deserialize IEnum: " + e.getMessage(), rawValue, m_enumType);
    }
  }
}
