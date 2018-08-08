package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.platform.dataobject.DataObjectAttributeDescriptor;
import org.eclipse.scout.rt.platform.dataobject.DataObjectInventory;
import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

/**
 * Custom {@link DateDeserializer} handling date values within {@link IDoEntity} which are annotated with custom
 * {@link ValueFormat} format.
 */
public class DoDateDeserializer extends DateDeserializer {
  private static final long serialVersionUID = 1L;

  protected final LazyValue<DataObjectInventory> m_dataObjectInventory = new LazyValue<>(DataObjectInventory.class);

  public DoDateDeserializer() {
  }

  public DoDateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
    super(base, df, formatString);
  }

  @Override
  protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.getCurrentValue() != null && p.getCurrentName() != null && p.getCurrentValue() instanceof IDoEntity) {
      Class<? extends IDoEntity> entityClass = p.getCurrentValue().getClass().asSubclass(IDoEntity.class);
      SimpleDateFormat formatter = findFormatter(entityClass, p.getCurrentName());
      String str = p.getText().trim();
      try {
        return formatter.parse(str);
      }
      catch (ParseException e) {
        throw ctxt.weirdStringException(str, handledType(), "expected format [" + formatter.toPattern() + "]");
      }
    }
    return super._parseDate(p, ctxt);
  }

  protected SimpleDateFormat findFormatter(Class<? extends IDoEntity> entityClass, String name) {
    String pattern = m_dataObjectInventory.get().getAttributeDescription(entityClass, name)
        .flatMap(DataObjectAttributeDescriptor::getFormatPattern)
        .orElse(IValueFormatConstants.DEFAULT_DATE_PATTERN);
    return new SimpleDateFormat(pattern);
  }

  @Override
  protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
    return new DoDateDeserializer(this, df, formatString);
  }
}
