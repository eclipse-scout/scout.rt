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
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom serializer for {@link IId} instances - like {@link TypedIdSerializer} it uses {@link IdExternalFormatter} for
 * serialization. It may be used as a replacement for {@link IIdSerializer}.
 */
public class QualifiedIIdSerializer extends StdSerializer<IId<?>> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdExternalFormatter> m_idExternalFormatter = new LazyValue<>(IdExternalFormatter.class);

  public QualifiedIIdSerializer() {
    super(IId.class, false);
  }

  @Override
  public void serialize(IId<?> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(m_idExternalFormatter.get().toExternalForm(value));
  }
}
