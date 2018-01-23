package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.platform.dataobject.DoEntity;
import org.eclipse.scout.rt.platform.dataobject.IValueFormatConstants;
import org.eclipse.scout.rt.platform.dataobject.ValueFormat;
import org.eclipse.scout.rt.platform.util.LazyValue;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;

/**
 * Custom {@link DateDeserializer} handling date values within {@link DoEntity} which are annotated with custom
 * {@link ValueFormat} format.
 */
public class DoDateDeserializer extends DateDeserializer {
  private static final long serialVersionUID = 1L;

  protected final SimpleDateFormat m_defaultDateFormatter = new SimpleDateFormat(IValueFormatConstants.DEFAULT_DATE_PATTERN);

  protected final LazyValue<DataObjectDefinitionRegistry> m_dataObjectDefinitionRegistry = new LazyValue<>(DataObjectDefinitionRegistry.class);

  public DoDateDeserializer() {
  }

  public DoDateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
    super(base, df, formatString);
  }

  @Override
  protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.getCurrentValue() != null && p.getCurrentName() != null && p.getCurrentValue() instanceof DoEntity) {
      @SuppressWarnings("unchecked")
      Class<? extends DoEntity> entityClass = (Class<? extends DoEntity>) p.getCurrentValue().getClass();
      SimpleDateFormat formatter = findFormatter(entityClass, p.getCurrentName());
      String str = p.getText().trim();
      try {
        return formatter.parse(str);
      }
      catch (ParseException e) {
        throw ctxt.weirdStringException(str, handledType(), "expected format [" + _formatString + "]");
      }
    }
    return super._parseDate(p, ctxt);
  }

  protected SimpleDateFormat findFormatter(Class<? extends DoEntity> entityClass, String name) {
    return m_dataObjectDefinitionRegistry.get().getAttributeDescription(entityClass, name)
        .flatMap(DataObjectAttributeDefinition::getFormatPattern)
        .map(pattern -> new SimpleDateFormat(pattern))
        .orElse(m_defaultDateFormatter);
  }

  @Override
  protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
    return new DoDateDeserializer(this, df, formatString);
  }
}
