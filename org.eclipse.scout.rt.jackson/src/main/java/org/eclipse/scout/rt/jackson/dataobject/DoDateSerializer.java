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
