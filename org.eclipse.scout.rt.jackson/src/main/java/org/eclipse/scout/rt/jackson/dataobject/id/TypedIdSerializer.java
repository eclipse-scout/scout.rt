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
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.jackson.dataobject.ScoutDataObjectModuleContext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for all {@link TypedId} instances.
 */
public class TypedIdSerializer extends AbstractIdCodecSerializer<TypedId<? extends IId>> {
  private static final long serialVersionUID = 1L;

  public TypedIdSerializer(ScoutDataObjectModuleContext moduleContext) {
    super(moduleContext, TypedId.class);
  }

  @Override
  public void serialize(TypedId<? extends IId> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(idCodec().toQualified(value.getId(), idCodecFlags()));
  }
}
