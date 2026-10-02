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
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.util.LazyValue;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.KeyDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer used for map keys of type {@link IEnum}.
 */
public class EnumMapKeyDeserializer extends KeyDeserializer {

  protected final LazyValue<EnumResolver> m_enumResolver = new LazyValue<>(EnumResolver.class);

  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final Class<? extends IEnum> m_enumClass;

  public EnumMapKeyDeserializer(ScoutDataObjectModuleContext moduleContext, Class<? extends IEnum> enumClass) {
    m_moduleContext = moduleContext;
    m_enumClass = enumClass;
  }

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws JacksonException {
    try {
      return m_enumResolver.get().resolve(m_enumClass, key);
    }
    catch (RuntimeException e) {
      if (m_moduleContext.isLenientMode()) {
        return key;
      }
      throw InvalidFormatException.from(null, "Failed to deserialize map key IEnum: " + e.getMessage(), key, m_enumClass);
    }
  }
}
