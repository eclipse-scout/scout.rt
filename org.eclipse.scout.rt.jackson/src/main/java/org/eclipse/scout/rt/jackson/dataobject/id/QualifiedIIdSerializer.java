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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom serializer for {@link IId} instances - like {@link TypedIdSerializer} it uses {@link IdCodec} for
 * serialization. It may be used as a replacement for {@link UnqualifiedIIdSerializer}.
 */
public class QualifiedIIdSerializer extends StdSerializer<IId> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  public QualifiedIIdSerializer() {
    super(IId.class, false);
  }

  @Override
  public void serialize(IId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(m_idCodec.get().toQualified(value));
  }
}
