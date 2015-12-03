/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
