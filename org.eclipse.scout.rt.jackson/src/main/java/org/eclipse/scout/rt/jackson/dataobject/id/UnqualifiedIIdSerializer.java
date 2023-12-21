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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for all {@link IId} instances.
 */
public class UnqualifiedIIdSerializer extends AbstractIdCodecSerializer<IId> {
  private static final long serialVersionUID = 1L;

  public UnqualifiedIIdSerializer(ScoutDataObjectModuleContext moduleContext, JavaType type) {
    super(moduleContext, type);
  }

  @Override
  public void serialize(IId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeObject(idCodec().toUnqualified(value, idCodecFlags()));
  }
}
