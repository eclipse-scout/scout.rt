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
package org.eclipse.scout.rt.client.extension.ui.basic.calendar;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.basic.calendar.CalendarChains.CalendarDisposeCalendarChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.CalendarChains.CalendarFilterCalendarItemsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.CalendarChains.CalendarInitCalendarChain;
import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;

public abstract class AbstractCalendarExtension<OWNER extends AbstractCalendar> extends AbstractExtension<OWNER> implements ICalendarExtension<OWNER> {

  /**
   * @param owner
   */
  public AbstractCalendarExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execFilterCalendarItems(CalendarFilterCalendarItemsChain chain, Set<Class<? extends ICalendarItemProvider>> changedProviderTypes,
      Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> componentsByProvider) {
    chain.execFilterCalendarItems(changedProviderTypes, componentsByProvider);
  }

  @Override
  public void execDisposeCalendar(CalendarDisposeCalendarChain chain) {
    chain.execDisposeCalendar();
  }

  @Override
  public void execInitCalendar(CalendarInitCalendarChain chain) {
    chain.execInitCalendar();
  }

}
