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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

/**
 * Custom deserializer used for map keys of type {@link IEnum}.
 */
public class EnumMapKeyDeserializer extends KeyDeserializer {

  private final Class<? extends IEnum> m_enumClass;
  protected final LazyValue<EnumResolver> m_enumResolver = new LazyValue<>(EnumResolver.class);

  public EnumMapKeyDeserializer(Class<? extends IEnum> enumClass) {
    m_enumClass = enumClass;
  }

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    return m_enumResolver.get().resolve(m_enumClass, key);
  }
}
