package org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield;

import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.form.fields.AbstractValueFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;

public abstract class AbstractCalendarFieldExtension<T extends ICalendar, OWNER extends AbstractCalendarField<T>> extends AbstractValueFieldExtension<Date, OWNER> implements ICalendarFieldExtension<T, OWNER> {

  public AbstractCalendarFieldExtension(OWNER owner) {
    super(owner);
  }
}
