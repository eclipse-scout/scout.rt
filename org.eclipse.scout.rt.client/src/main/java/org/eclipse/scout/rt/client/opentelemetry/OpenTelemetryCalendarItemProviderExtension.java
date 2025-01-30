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
import org.eclipse.scout.rt.client.opentelemetry.OpenTelemetryExtensionInstrumenterFactory.OpenTelemetryExtensionRequest;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public class OpenTelemetryCalendarItemProviderExtension extends AbstractCalendarItemProviderExtension<AbstractCalendarItemProvider> {

  private Instrumenter<OpenTelemetryExtensionRequest<AbstractCalendarItemProvider>, Void> m_instrumenter;
  private static final AttributeKey<String> MIN_DATE = AttributeKey.stringKey("scout.extension.min.date");
  private static final AttributeKey<String> MAX_DATE = AttributeKey.stringKey("scout.extension.max.date");
  private static final AttributeKey<String> ITEM_TEXT = AttributeKey.stringKey("scout.extension.item.text");

  public OpenTelemetryCalendarItemProviderExtension(AbstractCalendarItemProvider owner) {
    super(owner);
    m_instrumenter = BEANS.get(OpenTelemetryExtensionInstrumenterFactory.class).createInstrumenter(
        getClass(),
        (request) -> "");
  }

  @Override
  public void execLoadItems(CalendarItemProviderLoadItemsChain chain, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execLoadItems")
        .withAdditionalAttributesProvider(attributes -> {
          attributes.put(MIN_DATE, minDate.toString());
          attributes.put(MAX_DATE, maxDate.toString());
        })
        .wrapCall(() -> super.execLoadItems(chain, minDate, maxDate, result), m_instrumenter);
  }

  @Override
  public void execLoadItemsInBackground(CalendarItemProviderLoadItemsInBackgroundChain chain, IClientSession session, Date minDate, Date maxDate, Set<ICalendarItem> result) {
    new OpenTelemetryExtensionRequest<>(getOwner(), "execLoadItemsInBackground")
        .withAdditionalAttributesProvider(attributes -> {
          attributes.put(MIN_DATE, minDate.toString());
          attributes.put(MAX_DATE, maxDate.toString());
        })
        .wrapCall(() -> super.execLoadItemsInBackground(chain, session, minDate, maxDate, result), m_instrumenter);
  }

  @Override
  public void execItemAction(CalendarItemProviderItemActionChain chain, ICalendarItem item) {
    String itemText = item.getItemId() == null ? "" : item.getItemId().toString();
    new OpenTelemetryExtensionRequest<>(getOwner(), "execItemAction")
        .withAdditionalAttributesProvider(attributes -> attributes.put(ITEM_TEXT, itemText))
        .wrapCall(() -> super.execItemAction(chain, item), m_instrumenter);
  }
}
