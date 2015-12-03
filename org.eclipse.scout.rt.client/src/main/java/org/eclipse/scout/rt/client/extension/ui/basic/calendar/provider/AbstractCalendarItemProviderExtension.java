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
package org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider;

import java.util.Date;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderItemActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderItemMovedChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsInBackgroundChain;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public abstract class AbstractCalendarItemProviderExtension<OWNER extends AbstractCalendarItemProvider> extends AbstractExtension<OWNER> implements ICalendarItemProviderExtension<OWNER> {

  public AbstractCalendarItemProviderExtension(OWNER owner) {
    super(owner);
  }

  @Override
  public void execLoadItems(CalendarItemProviderLoadItemsChain chain, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    chain.execLoadItems(minDate, maxDate, result);
  }

  @Override
  public void execItemAction(CalendarItemProviderItemActionChain chain, ICalendarItem item) {
    chain.execItemAction(item);
  }

  @Override
  public void execLoadItemsInBackground(CalendarItemProviderLoadItemsInBackgroundChain chain, IClientSession session, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    chain.execLoadItemsInBackground(session, minDate, maxDate, result);
  }

  @Override
  public void execItemMoved(CalendarItemProviderItemMovedChain chain, ICalendarItem item, Date newDate) {
    chain.execItemMoved(item, newDate);
  }

}
