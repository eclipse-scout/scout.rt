package org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield;

import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IValueFieldExtension;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.AbstractCalendarField;

public interface ICalendarFieldExtension<T extends ICalendar, OWNER extends AbstractCalendarField<T>> extends IValueFieldExtension<Date, OWNER> {
}
