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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.basic.calendar.AbstractCalendar;
import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.client.ui.basic.calendar.provider.ICalendarItemProvider;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class CalendarChains {

  private CalendarChains() {
  }

  protected abstract static class AbstractCalendarChain extends AbstractExtensionChain<ICalendarExtension<? extends AbstractCalendar>> {

    public AbstractCalendarChain(List<? extends ICalendarExtension<? extends AbstractCalendar>> extensions) {
      super(extensions, ICalendarExtension.class);
    }
  }

  public static class CalendarFilterCalendarItemsChain extends AbstractCalendarChain {

    public CalendarFilterCalendarItemsChain(List<? extends ICalendarExtension<? extends AbstractCalendar>> extensions) {
      super(extensions);
    }

    public void execFilterCalendarItems(final Set<Class<? extends ICalendarItemProvider>> changedProviderTypes, final Map<Class<? extends ICalendarItemProvider>, Collection<CalendarComponent>> componentsByProvider) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarExtension<? extends AbstractCalendar> next) {
          next.execFilterCalendarItems(CalendarFilterCalendarItemsChain.this, changedProviderTypes, componentsByProvider);
        }
      };
      callChain(methodInvocation, changedProviderTypes, componentsByProvider);
    }
  }

  public static class CalendarDisposeCalendarChain extends AbstractCalendarChain {

    public CalendarDisposeCalendarChain(List<? extends ICalendarExtension<? extends AbstractCalendar>> extensions) {
      super(extensions);
    }

    public void execDisposeCalendar() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarExtension<? extends AbstractCalendar> next) {
          next.execDisposeCalendar(CalendarDisposeCalendarChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class CalendarInitCalendarChain extends AbstractCalendarChain {

    public CalendarInitCalendarChain(List<? extends ICalendarExtension<? extends AbstractCalendar>> extensions) {
      super(extensions);
    }

    public void execInitCalendar() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarExtension<? extends AbstractCalendar> next) {
          next.execInitCalendar(CalendarInitCalendarChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }
}
