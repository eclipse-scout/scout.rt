/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdExternalFormatter;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for {@link IId} instances - like {@link TypedIdSerializer} it uses {@link IdExternalFormatter} for
 * serialization. It may be used as a replacement for {@link IIdMapKeySerializer}.
 */
public class QualifiedIIdMapKeySerializer extends JsonSerializer<IId<?>> {

  protected final LazyValue<IdExternalFormatter> m_idExternalFormatter = new LazyValue<>(IdExternalFormatter.class);

  @Override
  public void serialize(IId<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(m_idExternalFormatter.get().toExternalForm(value));
  }
}
