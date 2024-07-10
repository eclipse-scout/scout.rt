/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.opentelemetry;

import java.util.Date;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.AbstractCalendarItemProviderExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderItemActionChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsChain;
import org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider.CalendarItemProviderChains.CalendarItemProviderLoadItemsInBackgroundChain;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.opentelemetry.ITracingHelper;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

import io.opentelemetry.api.trace.Tracer;

public class TracingCalendarItemProviderExtension extends AbstractCalendarItemProviderExtension<AbstractCalendarItemProvider> {

  private Tracer m_tracer;

  public TracingCalendarItemProviderExtension(AbstractCalendarItemProvider owner) {
    super(owner);
    m_tracer = BEANS.get(ITracingHelper.class).createTracer(TracingCalendarItemProviderExtension.class);
  }

  @Override
  public void execLoadItems(CalendarItemProviderLoadItemsChain chain, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    String name = getOwner().getClass().getSimpleName() + "#execLoadItems";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.calendarItemProvider.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.calendarItemProvider.event.minDate", minDate.toString());
      span.setAttribute("scout.client.calendarItemProvider.event.maxDate", minDate.toString());
      super.execLoadItems(chain, minDate, maxDate, result);
    });
  }

  @Override
  public void execLoadItemsInBackground(CalendarItemProviderLoadItemsInBackgroundChain chain, IClientSession session, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    String name = getOwner().getClass().getSimpleName() + "#execLoadItemsInBackground";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.calendarItemProvider.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.calendarItemProvider.event.minDate", minDate.toString());
      span.setAttribute("scout.client.calendarItemProvider.event.maxDate", minDate.toString());
      super.execLoadItemsInBackground(chain, session, minDate, maxDate, result);
    });
  }

  @Override
  public void execItemAction(CalendarItemProviderItemActionChain chain, ICalendarItem item) {
    String name = getOwner().getClass().getSimpleName() + "#execItemAction";
    BEANS.get(ITracingHelper.class).wrapInSpan(m_tracer, name, span -> {
      span.setAttribute("scout.client.calendarItemProvider.class", getOwner().getClass().getName());
      span.setAttribute("scout.client.calendarItemProvider.event.item.text", item.getSubject());
      super.execItemAction(chain, item);
    });
  }
}
