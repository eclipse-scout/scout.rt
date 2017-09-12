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
package org.eclipse.scout.rt.client.ui.form.fields.calendarfield;

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.form.fields.calendarfield.ICalendarFieldExtension;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.ICalendarDisplayMode;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("0b1ac83b-6fa4-4e12-88d0-680ed168e914")
public abstract class AbstractCalendarField<T extends ICalendar> extends AbstractValueField<Date> implements ICalendarField<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractCalendarField.class);

  private T m_calendar;
  private final OptimisticLock m_valueSelectionMediator;

  public AbstractCalendarField() {
    this(true);
  }

  public AbstractCalendarField(boolean callInitializer) {
    super(false);
    m_valueSelectionMediator = new OptimisticLock();
    if (callInitializer) {
      callInitializer();
    }
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

    super.initConfig();

    List<ICalendar> contributedCalendars = m_contributionHolder.getContributionsByClass(ICalendar.class);
    m_calendar = (T) CollectionUtility.firstElement(contributedCalendars);

    if (m_calendar == null) {
      Class<? extends ICalendar> configuredCalendar = getConfiguredCalendar();
      m_calendar = (T) ConfigurationUtility.newInnerInstance(this, configuredCalendar);
    }

    if (m_calendar != null) {
      if (m_calendar instanceof AbstractCalendar) {
        ((AbstractCalendar) m_calendar).setContainerInternal(this);
      }

      // restore calendar settings
      ClientUIPreferences env = ClientUIPreferences.getInstance();
      m_calendar.setDisplayMode(env.getCalendarDisplayMode(ICalendarDisplayMode.MONTH));
      m_calendar.setDisplayCondensed(env.getCalendarDisplayCondensed(true));
      /*
       * add observer for: - selected date sync - persistence of calendar
       * settings
       */
      m_calendar.addPropertyChangeListener(
          e -> {
            if (e.getPropertyName().equals(ICalendar.PROP_SELECTED_DATE)) {
              syncCalendarToCalendarField();
            }
          });
      syncCalendarToCalendarField();
    }
    else {
      LOG.warn("there is no inner class of type ICalendar in {}", getClass().getName());
    }
  }

  /*
   * Runtime
   */

  @Override
  protected void initFieldInternal() {
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
  protected void execChangedMasterValue(Object newMasterValue) {
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

  protected static class LocalCalendarFieldExtension<T extends ICalendar, OWNER extends AbstractCalendarField<T>> extends LocalValueFieldExtension<Date, OWNER> implements ICalendarFieldExtension<T, OWNER> {

    public LocalCalendarFieldExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected ICalendarFieldExtension<T, ? extends AbstractCalendarField<T>> createLocalExtension() {
    return new LocalCalendarFieldExtension<>(this);
  }

}
