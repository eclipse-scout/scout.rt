/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
