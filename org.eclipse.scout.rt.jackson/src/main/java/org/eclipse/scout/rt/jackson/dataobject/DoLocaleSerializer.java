/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.dataobject.IDoEntity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Custom serializer for {@link Locale} using {@link Locale#toLanguageTag()} instead of {@link Locale#toString()}
 * default Jackson behavior.
 * <p>
 * TODO [8.x] pbz: Remove this class when Jackson issue 1600 is fixed
 *
 * @see https://github.com/FasterXML/jackson-databind/issues/1600
 */
public class DoLocaleSerializer extends ToStringSerializer {
  private static final long serialVersionUID = 1L;

  public DoLocaleSerializer() {
    super(Locale.class);
  }

  @Override
  public boolean isEmpty(SerializerProvider prov, Object value) {
    return value == null || Locale.class.cast(value).toLanguageTag().isEmpty();
  }

  @Override
  public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    if (gen.getCurrentValue() instanceof IDoEntity) {
      gen.writeString(Locale.class.cast(value).toLanguageTag());
    }
    else {
      super.serialize(value, gen, provider);
    }
  }
}
