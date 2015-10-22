package org.eclipse.scout.rt.client.extension.ui.basic.calendar.provider;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
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
      callChain(methodInvocation, minDate, maxDate, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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
      callChain(methodInvocation, item);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

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
      callChain(methodInvocation, session, minDate, maxDate, result);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

  public static class CalendarItemProviderItemMovedChain extends AbstractCalendarItemProviderChain {

    public CalendarItemProviderItemMovedChain(List<? extends ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider>> extensions) {
      super(extensions);
    }

    public void execItemMoved(final ICalendarItem item, final Date newDate) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(ICalendarItemProviderExtension<? extends AbstractCalendarItemProvider> next) {
          next.execItemMoved(CalendarItemProviderItemMovedChain.this, item, newDate);
        }
      };
      callChain(methodInvocation, item, newDate);
      if (methodInvocation.getException() instanceof ProcessingException) {
        throw (ProcessingException) methodInvocation.getException();
      }

    }
  }

}
