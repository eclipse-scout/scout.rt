/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IId;
import org.eclipse.scout.rt.dataobject.id.IdExternalFormatter;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for {@link IId} instances - like {@link TypedIdDeserializer} it uses {@link IdExternalFormatter}
 * for serialization. It may be used as a replacement for {@link IIdDeserializer}.
 */
public class QualifiedIIdDeserializer extends StdDeserializer<IId<?>> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdExternalFormatter> m_idExternalFormatter = new LazyValue<>(IdExternalFormatter.class);

  public QualifiedIIdDeserializer() {
    super(IId.class);
  }

  @Override
  public IId<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return m_idExternalFormatter.get().fromExternalForm(p.readValueAs(String.class));
  }
}
