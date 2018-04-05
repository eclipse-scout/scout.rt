package org.eclipse.scout.rt.client.ui.desktop;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class DesktopListeners extends AbstractGroupedListenerList<DesktopListener, DesktopEvent, Integer> {

  @Override
  protected Integer eventType(DesktopEvent event) {
    return event.getType();
  }

  @Override
  protected Integer allEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(DesktopListener listener, DesktopEvent event) {
    listener.desktopChanged(event);
  }
}
