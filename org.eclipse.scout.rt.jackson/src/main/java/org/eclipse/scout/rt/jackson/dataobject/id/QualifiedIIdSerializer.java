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
