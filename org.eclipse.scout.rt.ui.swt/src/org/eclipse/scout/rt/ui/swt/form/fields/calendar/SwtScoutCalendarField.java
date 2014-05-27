/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.swt.form.fields.calendar;

import org.eclipse.scout.commons.job.JobEx;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.calendarfield.ICalendarField;
import org.eclipse.scout.rt.ui.swt.LogicalGridData;
import org.eclipse.scout.rt.ui.swt.LogicalGridLayout;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtColors;
import org.eclipse.scout.rt.ui.swt.basic.calendar.SwtScoutCalendar;
import org.eclipse.scout.rt.ui.swt.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.swt.form.fields.SwtScoutFieldComposite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * An SwtScoutCalendarField contains an SwtScoutCalendar and embeds it
 * within a Scout field.
 */
public class SwtScoutCalendarField extends SwtScoutFieldComposite<ICalendarField<ICalendar>> implements ISwtScoutCalendarField {

  protected SwtScoutCalendar m_calendar;
  protected ICalendar m_scoutCalendarModel;

  public Control getLabeledComponent() {
    return m_calendar;
  }

  @Override
  protected void initializeSwt(Composite parent) {
    Composite container = getEnvironment().getFormToolkit().createComposite(parent);
    setSwtContainer(container);
    getSwtContainer().setLayout(new LogicalGridLayout(1, 0));

    m_scoutCalendarModel = ((ICalendarField) getScoutObject()).getCalendar();

    m_calendar = new SwtScoutCalendar(container, SWT.NONE, this);
    m_calendar.setScoutCalendarModel(m_scoutCalendarModel);

    LogicalGridData textData = LogicalGridDataBuilder.createField(((IFormField) getScoutObject()).getGridData());
    m_calendar.setLayoutData(textData);

  }

  @Override
  protected void attachScout() {
    super.attachScout();
    m_calendar.setCondensedMode(getScoutObject().getCalendar().isDisplayCondensed());
    m_calendar.setCalendarComponentsFromScout(getScoutObject().getCalendar().getComponents());
    //notify Scout
    Runnable r = new Runnable() {
      @Override
      public void run() {
        getScoutObject().getCalendar().getUIFacade().setVisibleRangeFromUI(m_calendar.getViewDateStart().getTime(), m_calendar.getViewDateEnd().getTime());
      }
    };
    JobEx job = getEnvironment().invokeScoutLater(r, 0);
    try {
      job.join(2345);
    }
    catch (InterruptedException e) {
      //nop
    }
    //end notify
  }

  @Override
  protected void detachScout() {
    super.detachScout();
    SwtColors.getInstance().dispose();
  }
}
