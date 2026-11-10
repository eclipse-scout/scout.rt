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

import tools.jackson.databind.deser.jdk.JDKKeyDeserializer;

/**
 * Custom map key deserializer for {@link Locale}.
 * <p>
 * Ensures compatibility with {@link LocaleMapKeySerializer} by delegating locale deserialization
 * to {@link DoLocaleDeserializer}, including deserialization of {@code "und"} as {@link Locale#ROOT}.
 */
public class LocaleMapKeyDeserializer extends JDKKeyDeserializer {

  protected LocaleMapKeyDeserializer() {
    super(TYPE_LOCALE, Locale.class, new DoLocaleDeserializer());
  }
}
