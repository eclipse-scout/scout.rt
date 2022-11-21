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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer for {@link IId} instances - like {@link TypedIdDeserializer} it uses {@link IdCodec} for
 * serialization. It may be used as a replacement for {@link IIdDeserializer}.
 */
public class QualifiedIIdDeserializer extends StdDeserializer<IId> {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<IdCodec> m_idExternalFormatter = new LazyValue<>(IdCodec.class);

  public QualifiedIIdDeserializer() {
    super(IId.class);
  }

  @Override
  public IId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return m_idExternalFormatter.get().fromQualified(p.getText());
  }
}
