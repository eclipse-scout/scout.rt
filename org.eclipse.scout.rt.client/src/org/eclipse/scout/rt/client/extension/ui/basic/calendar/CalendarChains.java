package org.eclipse.scout.rt.client.extension.ui.basic.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
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

    public void execDisposeCalendar() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarExtension<? extends AbstractCalendar> next) throws ProcessingException {
          next.execDisposeCalendar(CalendarDisposeCalendarChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class CalendarInitCalendarChain extends AbstractCalendarChain {

    public CalendarInitCalendarChain(List<? extends ICalendarExtension<? extends AbstractCalendar>> extensions) {
      super(extensions);
    }

    public void execInitCalendar() throws ProcessingException {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarExtension<? extends AbstractCalendar> next) throws ProcessingException {
          next.execInitCalendar(CalendarInitCalendarChain.this);
        }
      };
      callChain(methodInvocation);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }
}
