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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.LazyValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.JavaUtilDateDeserializer;

/**
 * Custom {@link JavaUtilDateDeserializer} handling date values within {@link IDoEntity} which are annotated with custom
 * {@link ValueFormat} format.
 */
public class DoDateDeserializer extends JavaUtilDateDeserializer {

  protected final LazyValue<DoDateSerializationHelper> m_helper = new LazyValue<>(DoDateSerializationHelper.class);

  public DoDateDeserializer() {
  }

  public DoDateDeserializer(JavaUtilDateDeserializer base, DateFormat df, String formatString) {
    super(base, df, formatString);
  }

  @Override
  protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    SimpleDateFormat formatter = m_helper.get().findFormatter(p.streamReadContext());
    if (formatter != null) {
      // TODO [23.0] bsh, pbz: Set the time zone of the formatter to the time zone of Jackson here
      // This would allow setting a custom time zone on the ObjectMapper. However, we should also set the default time zone
      // in Jackson to the default time zone of the system, otherwise the current behavior will be changed and the
      // serialized date strings change. Jackson uses UTC by default (https://github.com/fasterxml/jackson-databind/issues/915)
      // and it's not possible to change this. Therefore, every ObjectMapper instance which sets a custom date format
      // using om.setDateFormat(...), would also have to set om.setTimeZone(TimeZone.getDefault()). As this requires
      // adjustments to existing code, it should only be considered in a future major release.
      // Note: The same issue exist in DoDateSerializer.

      String str = p.getText().trim();
      // Treat empty string and 'null' string as null value (as default Jackson date deserializer)
      if (str.isEmpty() || _hasTextualNull(str)) {
        return (Date) getNullValue(ctxt);
      }
      try {
        return formatter.parse(str);
      }
      catch (ParseException e) {
        throw ctxt.weirdStringException(str, handledType(), "expected format [" + formatter.toPattern() + "]");
      }
    }
    return super._parseDate(p, ctxt);
  }

  @Override
  protected JavaUtilDateDeserializer withDateFormat(DateFormat df, String formatString) {
    return new DoDateDeserializer(this, df, formatString);
  }
}
