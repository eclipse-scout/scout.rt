/*
 * Copyright (c) 2010-2023 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.id;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

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

  protected final Class<? extends IId<?>> m_concreteIdType;

  public QualifiedIIdDeserializer() {
    super(IId.class);
    m_concreteIdType = null;
  }

  public QualifiedIIdDeserializer(Class<? extends IId<?>> concreteIdType) {
    super(assertNotNull(concreteIdType));
    m_concreteIdType = concreteIdType;
  }

  @Override
  public IId<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return m_concreteIdType != null
        ? m_idExternalFormatter.get().fromExternalFormWithKnownType(m_concreteIdType, p.readValueAs(String.class))
        : m_idExternalFormatter.get().fromExternalForm(p.readValueAs(String.class));
  }
}
