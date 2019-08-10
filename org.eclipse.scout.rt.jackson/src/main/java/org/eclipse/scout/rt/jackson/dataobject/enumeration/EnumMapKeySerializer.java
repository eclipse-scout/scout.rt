/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject.enumeration;

import java.io.IOException;

import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer used for map keys of type {@link IEnum}.
 */
public class EnumMapKeySerializer extends JsonSerializer<IEnum> {

  @Override
  public void serialize(IEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(value.stringValue());
  }
}
