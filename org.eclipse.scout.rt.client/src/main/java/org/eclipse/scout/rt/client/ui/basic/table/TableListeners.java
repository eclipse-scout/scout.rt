package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.event.AbstractGroupedListenerList;

public final class TableListeners extends AbstractGroupedListenerList<TableListener, TableEvent, Integer> {
  private static final Set<Integer> KNOWN_EVENT_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
      TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED,
      TableEvent.TYPE_ROWS_INSERTED,
      TableEvent.TYPE_ROWS_UPDATED,
      TableEvent.TYPE_ROWS_DELETED,
      TableEvent.TYPE_ROWS_SELECTED,
      TableEvent.TYPE_ROW_ACTION,
      TableEvent.TYPE_ALL_ROWS_DELETED,
      TableEvent.TYPE_ROW_ORDER_CHANGED,
      TableEvent.TYPE_ROW_FILTER_CHANGED,
      TableEvent.TYPE_ROWS_DRAG_REQUEST,
      TableEvent.TYPE_ROW_DROP_ACTION,
      TableEvent.TYPE_ROWS_COPY_REQUEST,
      TableEvent.TYPE_COLUMN_ORDER_CHANGED,
      TableEvent.TYPE_COLUMN_HEADERS_UPDATED,
      TableEvent.TYPE_REQUEST_FOCUS,
      TableEvent.TYPE_REQUEST_FOCUS_IN_CELL,
      TableEvent.TYPE_ROW_CLICK,
      TableEvent.TYPE_SCROLL_TO_SELECTION,
      TableEvent.TYPE_ROWS_CHECKED,
      TableEvent.TYPE_ROWS_EXPANDED,
      TableEvent.TYPE_USER_FILTER_ADDED,
      TableEvent.TYPE_USER_FILTER_REMOVED,
      TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED,
      TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED)));

  @Override
  protected Integer eventType(TableEvent event) {
    return event.getType();
  }

  @Override
  protected Set<Integer> knownEventTypes() {
    return KNOWN_EVENT_TYPES;
  }

  @Override
  protected Integer otherEventsType() {
    return null;
  }

  @Override
  protected void handleEvent(TableListener listener, TableEvent event) {
    listener.tableChanged(event);
  }
}
