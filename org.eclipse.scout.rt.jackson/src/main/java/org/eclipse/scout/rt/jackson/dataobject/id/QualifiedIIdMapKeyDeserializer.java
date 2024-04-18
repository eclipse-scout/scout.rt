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

import static org.eclipse.scout.rt.platform.util.Assertions.assertInstance;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdCodec;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link IId} instances - like {@link TypedIdDeserializer} it uses {@link IdCodec} for
 * serialization. It may be used as a replacement for {@link UnqualifiedIIdDeserializer}.
 */
public class QualifiedIIdMapKeyDeserializer extends KeyDeserializer {

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  protected final ScoutDataObjectModuleContext m_moduleContext;
  protected final Class<? extends IId> m_idClass;

  public QualifiedIIdMapKeyDeserializer(ScoutDataObjectModuleContext moduleContext, Class<? extends IId> idClass) {
    m_moduleContext = moduleContext;
    m_idClass = idClass;
  }

  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    try {
      if (m_moduleContext.isLenientMode()) {
        return m_idCodec.get().fromQualifiedLenient(key);
      }
      // check required to prevent returning an instance that isn't compatible with requested ID class
      return assertInstance(m_idCodec.get().fromQualified(key), m_idClass);
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(null, "Failed to deserialize qualified IId map key: " + e.getMessage(), key, m_idClass);
    }
  }
}
