package org.eclipse.scout.rt.jackson.dataobject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.platform.dataobject.IDoEntity;
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

  protected final LazyValue<DoDateSerializationHelper> m_helper = new LazyValue<>(DoDateSerializationHelper.class);

  public DoDateDeserializer() {
  }

  public DoDateDeserializer(DateDeserializer base, DateFormat df, String formatString) {
    super(base, df, formatString);
  }

  @Override
  protected Date _parseDate(JsonParser p, DeserializationContext ctxt) throws IOException {
    SimpleDateFormat formatter = m_helper.get().findFormatter(p.getParsingContext());
    if (formatter != null) {
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

  @Override
  protected DateDeserializer withDateFormat(DateFormat df, String formatString) {
    return new DoDateDeserializer(this, df, formatString);
  }
}
