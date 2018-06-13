package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.platform.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.dataobject.ValueFormat;
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

  protected final SimpleDateFormat m_defaultFormatter = new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN);

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  public DoDateSerializer() {
    this(null, null);
  }

  protected DoDateSerializer(Boolean useTimestamp, DateFormat customFormat) {
    super(useTimestamp, customFormat);
  }

  @Override
  public void serialize(Date value, JsonGenerator g, SerializerProvider provider) throws IOException {
    if (g.getCurrentValue() instanceof IDoEntity && g.getOutputContext().hasCurrentName()) {
      Class<? extends IDoEntity> entityClass = g.getCurrentValue().getClass().asSubclass(IDoEntity.class);
      SimpleDateFormat formatter = findFormatter(entityClass, g.getOutputContext().getCurrentName());
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

  protected SimpleDateFormat findFormatter(Class<? extends IDoEntity> entityClass, String name) {
    return m_dataObjectInventory.get().getAttributeDescription(entityClass, name)
        .flatMap(DataObjectAttributeDescriptor::getFormatPattern)
        .map(pattern -> new SimpleDateFormat(pattern))
        .orElse(m_defaultFormatter);
  }
}
