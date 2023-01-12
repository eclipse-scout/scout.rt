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
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer used for map keys of type {@link IId}.
 */
public class IIdMapKeySerializer extends JsonSerializer<IId> {

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  @Override
  public void serialize(IId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(m_idCodec.get().toUnqualified(value));
  }
}
