package org.eclipse.scout.rt.client.ui.basic.table;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class TableListeners extends AbstractGroupedListenerList<TableListener, TableEvent, Integer> {

  @Override
  protected Integer eventType(TableEvent event) {
    return event.getType();
  }

  @Override
  protected Integer allEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(TableListener listener, TableEvent event) {
    listener.tableChanged(event);
  }
}
