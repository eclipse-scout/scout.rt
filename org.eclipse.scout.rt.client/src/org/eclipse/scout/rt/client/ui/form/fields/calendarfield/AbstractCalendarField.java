/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form.fields.calendarfield;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.OptimisticLock;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

@ClassId("0b1ac83b-6fa4-4e12-88d0-680ed168e914")
public abstract class AbstractCalendarField<T extends ICalendar> extends AbstractValueField<Date> implements ICalendarField<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractCalendarField.class);

  private T m_calendar;
  private OptimisticLock m_valueSelectionMediator;

  public AbstractCalendarField() {
    this(true);
  }

  public AbstractCalendarField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  private Class<? extends ICalendar> getConfiguredCalendar() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.filterClass(dca, ICalendar.class);
  }

  @Override
  @Order(210)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoAddDefaultMenus() {
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void initConfig() {
    m_valueSelectionMediator = new OptimisticLock();
    super.initConfig();
    final ClientUIPreferences env = ClientUIPreferences.getInstance();
    if (getConfiguredCalendar() != null) {
      try {
        m_calendar = (T) ConfigurationUtility.newInnerInstance(this, getConfiguredCalendar());
        if (m_calendar instanceof AbstractCalendar) {
          ((AbstractCalendar) m_calendar).setContainerInternal(this);
        }
        // restore calendar settings
        m_calendar.setDisplayMode(env.getCalendarDisplayMode(ICalendar.DISPLAY_MODE_MONTH));
        m_calendar.setDisplayCondensed(env.getCalendarDisplayCondensed(true));
        /*
         * add observer for: - selected date sync - persistence of calendar
         * settings
         */
        m_calendar.addPropertyChangeListener(
            new PropertyChangeListener() {
              @Override
              public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(ICalendar.PROP_SELECTED_DATE)) {
                  syncCalendarToCalendarField();
                }
              }
            }
            );
        syncCalendarToCalendarField();
      }
      catch (Exception e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("error creating instance of class '" + getConfiguredCalendar().getName() + "'.", e));
      }
    }
    else {
      LOG.warn("there is no inner class of type ICalendar in " + getClass());
    }
  }

  /*
   * Runtime
   */

  @Override
  protected void initFieldInternal() throws ProcessingException {
    getCalendar().initCalendar();
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    ClientUIPreferences.getInstance().setCalendarPreferences(getCalendar().getDisplayMode(), getCalendar().isDisplayCondensed());
    super.disposeFieldInternal();
    getCalendar().disposeCalendar();
  }

  @Override
  public final T getCalendar() {
    return m_calendar;
  }

  /**
   * Full override: reload calendar components
   */
  @Override
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
    setValue(null);
    getCalendar().reloadCalendarItems();
  }

  @Override
  protected void valueChangedInternal() {
    super.valueChangedInternal();
    syncCalendarFieldToCalendar();
  }

  private void syncCalendarFieldToCalendar() {
    try {
      if (m_valueSelectionMediator.acquire()) {
        Date value = getValue();
        T cal = getCalendar();
        cal.setSelectedDate(value);
      }
    }
    finally {
      m_valueSelectionMediator.release();
    }
  }

  private void syncCalendarToCalendarField() {
    try {
      if (m_valueSelectionMediator.acquire()) {
        T cal = getCalendar();
        Date value = cal.getSelectedDate();
        setValue(value);
      }
    }
    finally {
      m_valueSelectionMediator.release();
    }
  }

  @Override
  public void reloadCalendarItems() {
    getCalendar().reloadCalendarItems();
  }

}
