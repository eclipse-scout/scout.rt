/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer used for map keys of type {@link IId}.
 */
public class UnqualifiedIIdMapKeyDeserializer extends KeyDeserializer {

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final Class<? extends IId> m_idClass;

  public UnqualifiedIIdMapKeyDeserializer(ScoutDataObjectModuleContext moduleContext, Class<? extends IId> idClass) {
    m_moduleContext = moduleContext;
    m_idClass = idClass;
  }

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    try {
      return m_idCodec.get().fromUnqualified(m_idClass, key);
    }
    catch (RuntimeException e) {
      if (m_moduleContext.isLenientMode()) {
        return key;
      }
      throw InvalidFormatException.from(null, "Failed to deserialize unqualified IId map key: " + e.getMessage(), key, m_idClass);
    }
  }
}
