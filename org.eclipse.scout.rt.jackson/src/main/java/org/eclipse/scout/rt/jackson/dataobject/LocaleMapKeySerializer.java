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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom serializer for map keys of type {@link Locale} using {@link Locale#toLanguageTag()} instead of
 * {@link Locale#toString()} default Jackson behavior.
 * <p>
 * TODO pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class LocaleMapKeySerializer extends ValueSerializer<Locale> {

  @Override
  public void serialize(Locale value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
    gen.writeName(value.toLanguageTag());
  }
}
