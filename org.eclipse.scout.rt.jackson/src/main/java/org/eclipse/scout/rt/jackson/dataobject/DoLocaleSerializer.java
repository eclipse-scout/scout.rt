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

import tools.jackson.databind.ser.jdk.JDKStringLikeSerializer;
import tools.jackson.databind.ser.std.ToStringSerializerBase;

/**
 * Custom serializer for {@link Locale}.
 * <p>
 * Serializes locales in the same way as Jackson, except for {@link Locale#ROOT},
 * which is serialized as {@code "und"} instead of an empty string.
 *
 * @see JDKStringLikeSerializer#localeToString(Locale)
 */
public class DoLocaleSerializer extends ToStringSerializerBase {

  public DoLocaleSerializer() {
    super(Locale.class);
  }

  @Override
  public String valueToString(Object value) {
    return ((Locale) value).toLanguageTag();
  }
}
