/*
 * Copyright (c) 2010-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.dataobject.IDoEntity;
import org.eclipse.scout.rt.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

/**
 * Custom {@link DateSerializer} handling date values within {@link IDoEntity} which are annotated with custom
 * {@link ValueFormat} format.
 */
public class DoDateSerializer extends DateSerializer {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DoDateSerializationHelper> m_helper = new LazyValue<>(DoDateSerializationHelper.class);

  public DoDateSerializer() {
    this(null, null);
  }

  protected DoDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
    super(useTimestamp, customFormat);
  }

  @Override
  public void serialize(Date value, JsonGenerator g, SerializerProvider provider) throws IOException {
    SimpleDateFormat formatter = m_helper.get().findFormatter(g.getOutputContext());
    if (formatter != null) {
      // TODO bsh, pbz: Set the time zone of the formatter to the time zone of Jackson here
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
      super.serialize(value, g, provider);
    }
  }

  @Override
  public DateSerializer withFormat(Boolean timestamp, DateFormat customFormat) {
    return new DoDateSerializer(timestamp, customFormat);
  }
}
