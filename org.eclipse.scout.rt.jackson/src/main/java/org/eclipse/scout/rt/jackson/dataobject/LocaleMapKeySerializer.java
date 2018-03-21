/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Custom serializer for map keys of type {@link Locale} using {@link Locale#toLanguageTag()} instead of
 * {@link Locale#toString()} default Jackson behavior.
 * <p>
 * TODO [8.x] pbz: Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class LocaleMapKeySerializer extends JsonSerializer<Locale> {

  @Override
  public void serialize(Locale value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(value.toLanguageTag());
  }
}
