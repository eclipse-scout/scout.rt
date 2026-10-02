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
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.LazyValue;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.jdk.JavaUtilDateSerializer;

/**
 * Custom {@link JavaUtilDateSerializer} handling date values within {@link IDoEntity} which are annotated with custom
 * {@link ValueFormat} format.
 */
public class DoDateSerializer extends JavaUtilDateSerializer {

  protected final LazyValue<DoDateSerializationHelper> m_helper = new LazyValue<>(DoDateSerializationHelper.class);

  public DoDateSerializer() {
    this(null, null);
  }

  protected DoDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
    super(useTimestamp, customFormat);
  }

  @Override
  public void serialize(Date value, JsonGenerator g, SerializationContext ctxt) throws JacksonException {
    SimpleDateFormat formatter = m_helper.get().findFormatter(g.streamWriteContext());
    if (formatter != null) {
      // TODO [23.0] bsh, pbz: Set the time zone of the formatter to the time zone of Jackson here
      // This would allow setting a custom time zone on the ObjectMapper. However, we should also set the default time zone
      // in Jackson to the default time zone of the system, otherwise the current behavior will be changed and the
      // serialized date strings change. Jackson uses UTC by default (https://github.com/fasterxml/jackson-databind/issues/915)
      // and it's not possible to change this. Therefore, every ObjectMapper instance which sets a custom date format
      // using om.setDateFormat(...), would also have to set om.setTimeZone(TimeZone.getDefault()). As this requires
      // adjustments to existing code, it should only be considered in a future major release.
      // Note: The same issue exist in DoDateDeserializer.
      g.writeString(formatter.format(value));
    }
    else {
      super.serialize(value, g, ctxt);
    }
  }

  @Override
  public JavaUtilDateSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
    return new JavaUtilDateSerializer(timestamp, customFormat);
  }
}
