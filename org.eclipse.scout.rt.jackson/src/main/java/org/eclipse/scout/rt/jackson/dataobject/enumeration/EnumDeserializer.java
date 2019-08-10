/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.enumeration;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.enumeration.EnumResolver;
import org.eclipse.scout.rt.dataobject.enumeration.IEnum;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

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
    return m_enumResolver.get().resolve(m_enumType, p.readValueAs(String.class));
  }
}
