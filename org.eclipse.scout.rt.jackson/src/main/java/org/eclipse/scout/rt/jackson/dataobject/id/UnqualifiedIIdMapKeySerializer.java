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
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

/**
 * Custom serializer used for map keys of type {@link IId}.
 */
public class UnqualifiedIIdMapKeySerializer extends AbstractIdCodecMapKeySerializer<IId> {

  public UnqualifiedIIdMapKeySerializer(ScoutDataObjectModuleContext moduleContext) {
    super(moduleContext);
  }

  @Override
  public void serialize(IId value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writeName(idCodec().toUnqualified(value, idCodecFlags()));
  }
}
