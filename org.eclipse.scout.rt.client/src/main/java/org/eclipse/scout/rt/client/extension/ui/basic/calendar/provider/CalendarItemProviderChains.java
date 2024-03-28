/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.AbstractCalendarItemProvider;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;
import org.eclipse.scout.rt.shared.services.common.calendar.ICalendarItem;

public final class CalendarItemProviderChains {

  private CalendarItemProviderChains() {
  }

  protected abstract static class AbstractCalendarItemProviderChain extends AbstractExtensionChain<ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> {

    public AbstractCalendarItemProviderChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions, ICalendarItemProviderExtension.class);
    }
  }

  public static class CalendarItemProviderLoadItemsChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderLoadItemsChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execLoadItems(final Date minDate, final Date maxDate, final Set<ICalendarItem> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execLoadItems(CalendarItemProviderLoadItemsChain.this, minDate, maxDate, result);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class CalendarItemProviderItemActionChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderItemActionChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execItemAction(final ICalendarItem item) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execItemAction(CalendarItemProviderItemActionChain.this, item);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class CalendarItemProviderLoadItemsInBackgroundChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderLoadItemsInBackgroundChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execLoadItemsInBackground(final IClientSession session, final Date minDate, final Date maxDate, final Set<ICalendarItem> result) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execLoadItemsInBackground(CalendarItemProviderLoadItemsInBackgroundChain.this, session, minDate, maxDate, result);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class CalendarItemProviderItemMovedChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderItemMovedChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execItemMoved(final ICalendarItem item, final Date fromDate, final Date toDate) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execItemMoved(CalendarItemProviderItemMovedChain.this, item, fromDate, toDate);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class CalendarItemProviderAutoAssignItemChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderAutoAssignItemChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execAutoAssignCalendarItems(Set<ICalendarItem> items) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execAutoAssignCalendarItems(CalendarItemProviderAutoAssignItemChain.this, items);
        }
      };
      callChain(methodInvocation);
    }
  }

}
