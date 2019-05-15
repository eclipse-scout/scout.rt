/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.jackson.dataobject.id;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.id.IdExternalFormatter;
import org.eclipse.scout.rt.dataobject.id.TypedId;
import org.eclipse.scout.rt.platform.BEANS;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom serializer for all {@link TypedId} instances.
 */
public class TypedIdSerializer extends StdSerializer<TypedId<?>> {
  private static final long serialVersionUID = 1L;

  public TypedIdSerializer() {
    super(TypedId.class, false);
  }

  @Override
  public void serialize(TypedId<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(BEANS.get(IdExternalFormatter.class).toExternalForm(value.getId()));
  }
}
