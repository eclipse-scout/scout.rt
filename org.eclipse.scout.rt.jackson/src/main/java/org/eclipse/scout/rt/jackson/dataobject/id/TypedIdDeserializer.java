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
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

/**
 * Custom deserializer for {@link TypedId} values.
 */
public class TypedIdDeserializer extends StdDeserializer<TypedId<IId>> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdCodec> m_idCodec = new LazyValue<>(IdCodec.class);

  public TypedIdDeserializer() {
    super(TypedId.class);
  }

  @Override
  public TypedId<IId> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String rawValue = p.getText();
    try {
      return TypedId.of(m_idCodec.get().fromQualified(rawValue));
    }
    catch (RuntimeException e) {
      throw InvalidFormatException.from(p, "Failed to deserialize TypedId: " + e.getMessage(), rawValue, TypedId.class);
    }
  }
}
