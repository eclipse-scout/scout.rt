/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
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
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer used for map keys of type {@link IId}.
 */
public class UnqualifiedIIdMapKeyDeserializer extends AbstractIdCodecMapKeyDeserializer {

  protected final Class<? extends IId> m_idClass;

  public UnqualifiedIIdMapKeyDeserializer(ScoutDataObjectModuleContext moduleContext, Class<? extends IId> idClass) {
    super(moduleContext);
    m_idClass = idClass;
  }

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    try {
      return idCodec().fromUnqualified(m_idClass, key, idCodecFlags());
    }
    catch (RuntimeException e) {
      if (m_moduleContext.isLenientMode()) {
        return key;
      }
      throw InvalidFormatException.from(null, "Failed to deserialize unqualified IId map key: " + e.getMessage(), key, m_idClass);
    }
  }
}
