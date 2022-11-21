/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
 * Custom serializer for {@link IId} instances - like {@link TypedIdSerializer} it uses {@link IdCodec} for
 * serialization. It may be used as a replacement for {@link IIdMapKeySerializer}.
 */
public class QualifiedIIdMapKeySerializer extends JsonSerializer<IId> {

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  @Override
  public void serialize(IId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(m_idCodec.get().toQualified(value));
  }
}
