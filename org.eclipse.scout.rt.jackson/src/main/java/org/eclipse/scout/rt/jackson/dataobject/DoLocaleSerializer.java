/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.jdk.JDKStringLikeSerializer;
import tools.jackson.databind.ser.std.ToStringSerializerBase;

/**
 * Custom serializer for {@link Locale} that always serializes to {@link Locale#toLanguageTag()}
 * instead of default Jackson behavior which serializes the {@link Locale#ROOT} to an empty string.
 */
public class DoLocaleSerializer extends ToStringSerializerBase {

  public DoLocaleSerializer() {
    super(Locale.class);
  }

  /**
   * Always use the language tag, so that {@link Locale#ROOT} is serialized to "und".
   * Jackson uses empty string for the {@link Locale#ROOT}.
   *
   * @see JDKStringLikeSerializer#serialize(Object, JsonGenerator, SerializationContext)
   */
  @Override
  public String valueToString(Object value) {
    return ((Locale) value).toLanguageTag();
  }
}
