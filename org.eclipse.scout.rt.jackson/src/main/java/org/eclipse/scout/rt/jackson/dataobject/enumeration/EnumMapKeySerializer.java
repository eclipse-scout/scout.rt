/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject.enumeration;

import org.eclipse.scout.rt.dataobject.enumeration.IEnum;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom serializer used for map keys of type {@link IEnum}.
 */
public class EnumMapKeySerializer extends ValueSerializer<IEnum> {

  @Override
  public void serialize(IEnum value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writeName(value.stringValue());
  }
}
