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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link IId} instances - like {@link TypedIdDeserializer} it uses {@link IdCodec} for
 * serialization. It may be used as a replacement for {@link UnqualifiedIIdDeserializer}.
 */
public class QualifiedIIdDeserializer extends StdDeserializer<IId> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);
  protected final Class<? extends IId> m_idClass;

  protected final ScoutDataObjectModuleContext m_moduleContext;

  public QualifiedIIdDeserializer(ScoutDataObjectModuleContext context, Class<? extends IId> idClass) {
    super(idClass);
    m_idClass = idClass;
    m_moduleContext = context;
  }

  @Override
  public IId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    // check required to prevent returning an instance that isn't compatible with requested ID class
    String rawValue = p.getText();
    try {
      if (m_moduleContext.isLenientMode()) {
        return assertInstance(m_idCodec.get().fromQualifiedLenient(rawValue), m_idClass);
      }
      return assertInstance(m_idCodec.get().fromQualified(rawValue), m_idClass);
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(p, "Failed to deserialize qualified IId: " + e.getMessage(), rawValue, m_idClass);
    }
  }
}
