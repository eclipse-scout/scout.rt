/*******************************************************************************
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
 * TODO [9.1] pbz: [JSON] Remove this class when Jackson is upgraded to 3.0 (issue 1600)
 */
public class LocaleMapKeySerializer extends JsonSerializer<Locale> {

  @Override
  public void serialize(Locale value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeFieldName(value.toLanguageTag());
  }
}
