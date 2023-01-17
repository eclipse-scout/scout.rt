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
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Custom serializer for all {@link IEnum} instances.
 */
public class EnumSerializer extends StdSerializer<IEnum> {
  private static final long serialVersionUID = 1L;

  public EnumSerializer(JavaType type) {
    super(type);
  }

  @Override
  public void serialize(IEnum value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(value.stringValue());
  }
}
