/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link IId} values.
 */
public class UnqualifiedIIdDeserializer extends AbstractIdCodecDeserializer<IId> {

  protected final Class<? extends IId> m_idClass;

  public UnqualifiedIIdDeserializer(ScoutDataObjectModuleContext moduleContext, Class<? extends IId> idClass) {
    super(moduleContext, idClass);
    m_idClass = idClass;
  }

  @Override
  public IId deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    String rawValue = p.getString();
    try {
      return idCodec().fromUnqualified(m_idClass, rawValue, idCodecFlags());
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(p, "Failed to deserialize unqualified IId: " + e.getMessage(), rawValue, m_idClass);
    }
  }
}
