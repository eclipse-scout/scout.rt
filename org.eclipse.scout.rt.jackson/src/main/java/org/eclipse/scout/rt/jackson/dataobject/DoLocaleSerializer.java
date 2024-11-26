/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Custom serializer for {@link Locale} using {@link Locale#toLanguageTag()} instead of {@link Locale#toString()}
 * default Jackson behavior.
 * <p>
 * TODO pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 *
 * @see <a href="https://github.com/FasterXML/jackson-databind/issues/1600">Issue</a>
 */
public class DoLocaleSerializer extends ToStringSerializer {
  private static final long serialVersionUID = 1L;

  public DoLocaleSerializer() {
    super(Locale.class);
  }

  @Override
  public boolean isEmpty(SerializerProvider prov, Object value) {
    return value == null || ((Locale) value).toLanguageTag().isEmpty();
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    // No restriction on if current value is instance of IDoEntity because otherwise Locales used as value in a Map aren't correctly serialized.
    // Issue 1600 is fixed in 3.0, we enforce this behavior for Scout already.
    gen.writeString(((Locale) value).toLanguageTag());
  }
}
