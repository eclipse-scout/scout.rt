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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;

/**
 * A buffer for table events ({@link TableEvent}) with coalesce functionality:
 * <p>
 * <ul>
 * <li>Unnecessary events are removed.
 * <li>Events are merged, if possible.
 * </ul>
 * </p>
 * Not thread safe, to be accessed in client model job.
 */
public class TableEventBuffer extends AbstractEventBuffer<TableEvent> {

  /**
   * Removes unnecessary events or combines events in the list.
   */
  @Override
  protected List<TableEvent> coalesce(List<TableEvent> events) {
    removeObsolete(events);
    replacePrevious(events, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED);
    removeEmptyEvents(events);
    removeIdenticalEvents(events);
    coalesceSameType(events);
    applyRowOrderChangedToRowsInserted(events);
    return events;
  }

  /**
   * Remove previous events that are now obsolete.
   */
  protected void removeObsolete(List<TableEvent> events) {
    //traverse the list in reversed order
    //previous events may be deleted from the list
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TableEvent event = events.get(i);

      int type = event.getType();
      if (type == TableEvent.TYPE_ALL_ROWS_DELETED) {
        //remove all previous row related events
        remove(getRowRelatedEvents(), events.subList(0, i));
      }
      else if (type == TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED) {
        //ignore all previous aggregate function changes.
        List<Integer> typesToDelete = CollectionUtility.arrayList(
            TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED,
            TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
        if (isIgnorePrevious(type)) {
          typesToDelete.add(type);
        }
        remove(typesToDelete, events.subList(0, i));
      }
      else if (isIgnorePrevious(type)) {
        //remove all previous events of the same type
        remove(type, events.subList(0, i));
      }
      else if (type == TableEvent.TYPE_ROWS_DELETED) {
        List<ITableRow> remainingRows = removeRowsFromPreviousEvents(event.getRows(), events.subList(0, i), TableEvent.TYPE_ROWS_INSERTED);
        event.setRows(remainingRows);
      }
    }
  }

  /**
   * Removes the given 'rowsToRemove' from all 'events'. The event list is traversed backwards. This process is stopped,
   * when a event that may change row indexes is encountered.
   *
   * @return a list with the same rows as 'rowsToRemove', except those that were removed from an event whose type
   *         matches one of the 'creationTypes'. This allows for completely removing a row that was created and deleted
   *         in the same request.
   */
  protected List<ITableRow> removeRowsFromPreviousEvents(List<ITableRow> rowsToRemove, List<TableEvent> events, Integer... creationTypes) {
    List<Integer> creationTypesList = Arrays.asList(creationTypes);
    List<ITableRow> remainingRows = new ArrayList<ITableRow>();

    for (ITableRow rowToRemove : rowsToRemove) {
      boolean rowRemovedFromCreationEvent = false;

      for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
        TableEvent event = it.previous();
        boolean removed = removeRow(event, rowToRemove);
        if (removed && creationTypesList.contains(event.getType())) {
          rowRemovedFromCreationEvent = true;
        }
        if (!isRowOrderUnchanged(event.getType())) {
          break;
        }
      }

      if (!rowRemovedFromCreationEvent) {
        remainingRows.add(rowToRemove);
      }
    }

    return remainingRows;
  }

  protected boolean removeRow(TableEvent event, ITableRow rowToRemove) {
    boolean removed = false;
    List<ITableRow> rows = event.getRows();
    for (Iterator<ITableRow> it = rows.iterator(); it.hasNext();) {
      ITableRow row = it.next();
      if (row == rowToRemove) {
        it.remove();
        removed = true;
      }
    }
    event.setRows(rows);
    return removed;
  }

  /**
   * Update a previous event of given type and removes a newer one of another type.
   */
  protected void replacePrevious(List<TableEvent> events, int oldType, int newType) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TableEvent event = events.get(i);

      if (event.getType() == newType) {
        //merge current update event with previous insert event of the same row
        updatePreviousRow(event, events.subList(0, i), oldType);
      }
    }
  }

  /**
   * Merge previous events of the same type (rows and columns) into the current and delete the previous events
   */
  protected void coalesceSameType(List<TableEvent> events) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TableEvent event = events.get(i);

      if (isCoalesceConsecutivePrevious(event.getType())) {
        coalesceConsecutivePrevious(event, events.subList(0, i));
      }
    }
  }

  /**
   * Updates previous rows in the list, if it is of the given type. Breaks, if events are encountered, that may change
   * the row order.
   */
  protected void updatePreviousRow(TableEvent event, List<TableEvent> events, int type) {
    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      TableEvent previous = it.previous();
      if (previous.getType() == type) {
        List<ITableRow> rows = event.getRows();
        replaceRows(previous, rows);
        event.setRows(rows);
      }
      if (!isRowOrderUnchanged(previous.getType())) {
        break;
      }
    }
  }

  protected void replaceRows(TableEvent event, List<ITableRow> newRows) {
    for (Iterator<ITableRow> it = newRows.iterator(); it.hasNext();) {
      ITableRow newRow = it.next();
      boolean replaced = tryReplaceRow(event, newRow);
      if (replaced) {
        it.remove();
      }
    }
  }

  /**
   * Replaces the row in the event, if it is contained.
   *
   * @return <code>true</code> if successful.
   */
  protected boolean tryReplaceRow(TableEvent event, ITableRow newRow) {
    List<ITableRow> targetRows = new ArrayList<>();
    boolean replaced = false;
    for (ITableRow row : event.getRows()) {
      if (row == newRow) {
        row = newRow;
        replaced = true;
      }
      targetRows.add(row);
    }
    event.setRows(targetRows);
    return replaced;
  }

  /**
   * Merge events of the same type in the given list (rows and columns) into the current and delete the other events
   * from the list.
   */
  protected void coalesceConsecutivePrevious(TableEvent event, List<TableEvent> list) {
    for (ListIterator<TableEvent> it = list.listIterator(list.size()); it.hasPrevious();) {
      TableEvent previous = it.previous();
      if (event.getType() == previous.getType()) {
        merge(previous, event);
        it.remove();
      }
      else {
        // Stop only if the event and the previous event are of the same "relation type" (e.g. both row-related or both non-row-related)
        if (isRowRelatedEvent(event.getType()) == isRowRelatedEvent(previous.getType())) {
          return;
        }
      }
    }
  }

  /**
   * Adds rows and columns
   */
  protected TableEvent merge(TableEvent first, TableEvent second) {
    second.setColumns(mergeColumns(first.getColumns(), second.getColumns()));
    second.setRows(mergeRows(first.getRows(), second.getRows()));
    return second;
  }

  /**
   * Merge list of rows, such that, if a row is in both lists, only the one of the second list (later event) is kept.
   */
  protected List<ITableRow> mergeRows(List<ITableRow> first, List<ITableRow> second) {
    List<ITableRow> rows = new ArrayList<>();
    Set<ITableRow> secondRowSet = new HashSet<>(second);
    for (ITableRow row : first) {
      if (!secondRowSet.contains(row)) {
        rows.add(row);
      }
    }
    for (ITableRow row : second) {
      rows.add(row);
    }
    return rows;
  }

  /**
   * Merge list of cols, such that, if a column is in both lists, only the one of the second list (later event) is kept.
   */
  protected Collection<IColumn<?>> mergeColumns(Collection<IColumn<?>> first, Collection<IColumn<?>> second) {
    List<IColumn<?>> cols = new ArrayList<>();
    Set<IColumn<?>> secondColumnSet = new HashSet<>(second);
    for (IColumn<?> column : first) {
      if (!secondColumnSet.contains(column)) {
        cols.add(column);
      }
    }
    for (IColumn<?> column : second) {
      cols.add(column);
    }
    return cols;
  }

  /**
   * If a ROW_ORDER_CHANGED event happens directly after ROWS_INSERTED, we may removed the ROW_ORDER_CHANGED event and
   * send the new order in the ROWS_INSERTED event instead.
   */
  protected void applyRowOrderChangedToRowsInserted(List<TableEvent> events) {
    for (int j = 0; j < events.size() - 1; j++) {
      int i = events.size() - 1 - j;
      TableEvent event = events.get(i);

      if (event.getType() == TableEvent.TYPE_ROW_ORDER_CHANGED) {
        TableEvent previous = findInsertionBeforeRowOrderChanged(events.subList(0, i));
        // Check if previous is ROWS_INSERTED and they have the same rows
        if (previous != null && previous.getType() == TableEvent.TYPE_ROWS_INSERTED &&
            event.getRowCount() == previous.getRowCount() && CollectionUtility.equalsCollection(event.getRows(), previous.getRows(), false)) {
          // replace rows and remove ROW_ORDER_CHANGED event
          previous.setRows(event.getRows());
          events.remove(i);
        }
      }
    }
  }

  /**
   * Finds previous ROWS_INSERTED event while ignoring events that don't change row order (e.g. COLUMN_HEADERS_UPDATED)
   */
  protected TableEvent findInsertionBeforeRowOrderChanged(List<TableEvent> events) {
    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      TableEvent previous = it.previous();
      if (previous.getType() == TableEvent.TYPE_ROWS_INSERTED) {
        return previous;
      }
      if (!isRowOrderUnchanged(previous.getType())) {
        break;
      }
    }
    return null;
  }

  protected void removeEmptyEvents(List<TableEvent> events) {
    for (Iterator<TableEvent> it = events.iterator(); it.hasNext();) {
      TableEvent event = it.next();
      if (isRowsRequired(event.getType()) && event.getRows().isEmpty()) {
        it.remove();
      }
    }
  }

  /**
   * Removes identical events (same type and content) when they occur consecutively (not necessarily directly, but
   * within the same type group). The oldest event is preserved.
   */
  protected void removeIdenticalEvents(List<TableEvent> events) {
    // Please note: In contrast to all other methods in this class, this method loops through the
    // list in FORWARD direction (so the oldest event will be kept).
    for (int i = 0; i < events.size(); i++) {
      TableEvent event = events.get(i);

      List<TableEvent> subList = events.subList(i + 1, events.size());
      for (Iterator<TableEvent> it = subList.iterator(); it.hasNext();) {
        TableEvent next = it.next();
        if (next.getType() != event.getType()) {
          // Stop when a node of different type occurs
          break;
        }
        if (isIdenticalEvent(event, next)) {
          it.remove();
        }
      }
    }
  }

  @Override
  protected boolean isIdenticalEvent(TableEvent event1, TableEvent event2) {
    if (event1 == null && event2 == null) {
      return true;
    }
    if (event1 == null || event2 == null) {
      return false;
    }
    boolean identical = (event1.getType() == event2.getType()
        && CollectionUtility.equalsCollection(event1.getRows(), event2.getRows(), true)
        && CollectionUtility.equalsCollection(event1.getPopupMenus(), event2.getPopupMenus())
        && event1.isConsumed() == event2.isConsumed()
        && CompareUtility.equals(event1.getDragObject(), event2.getDragObject())
        && CompareUtility.equals(event1.getDropObject(), event2.getDropObject())
        && CompareUtility.equals(event1.getCopyObject(), event2.getCopyObject())
        && CompareUtility.equals(event1.getColumns(), event2.getColumns())
        && event1.isSortInMemoryAllowed() == event2.isSortInMemoryAllowed());
    return identical;
  }

  /**
   * @return <code>true</code>, if the event does not influence the row order.
   */
  protected boolean isRowOrderUnchanged(int type) {
    switch (type) {
      case TableEvent.TYPE_ROWS_SELECTED:
      case TableEvent.TYPE_ROW_ACTION:
      case TableEvent.TYPE_ROW_CLICK:
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_CHECKED:
      case TableEvent.TYPE_SCROLL_TO_SELECTION:
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED:
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED:
        return true;
      default:
        return false;
    }
  }

  protected List<Integer> getRowRelatedEvents() {
    List<Integer> res = new ArrayList<>();
    res.add(TableEvent.TYPE_ALL_ROWS_DELETED);
    res.add(TableEvent.TYPE_ROW_ACTION);
    res.add(TableEvent.TYPE_ROW_CLICK);
    res.add(TableEvent.TYPE_ROW_DROP_ACTION);
    res.add(TableEvent.TYPE_ROW_FILTER_CHANGED);
    res.add(TableEvent.TYPE_ROW_ORDER_CHANGED);
    res.add(TableEvent.TYPE_ROWS_CHECKED);
    res.add(TableEvent.TYPE_ROWS_COPY_REQUEST);
    res.add(TableEvent.TYPE_ROWS_DELETED);
    res.add(TableEvent.TYPE_ROWS_DRAG_REQUEST);
    res.add(TableEvent.TYPE_ROWS_INSERTED);
    res.add(TableEvent.TYPE_ROWS_SELECTED);
    res.add(TableEvent.TYPE_ROWS_UPDATED);
    res.add(TableEvent.TYPE_REQUEST_FOCUS_IN_CELL);
    res.add(TableEvent.TYPE_SCROLL_TO_SELECTION);
    return res;
  }

  protected boolean isRowRelatedEvent(int type) {
    return getRowRelatedEvents().contains(type);
  }

  /**
   * @param type
   *          {@link TableEvent} type
   * @return true, if previous events of the same type can be ignored. false otherwise
   */
  protected boolean isIgnorePrevious(int type) {
    switch (type) {
      case TableEvent.TYPE_ROWS_SELECTED:
      case TableEvent.TYPE_SCROLL_TO_SELECTION:
      case TableEvent.TYPE_ROWS_DRAG_REQUEST:
      case TableEvent.TYPE_ROW_ORDER_CHANGED:
      case TableEvent.TYPE_COLUMN_ORDER_CHANGED:
      case TableEvent.TYPE_ALL_ROWS_DELETED:
      case TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  /**
   * @return true, if previous consecutive events of the same type can be coalesced.
   */
  protected boolean isCoalesceConsecutivePrevious(int type) {
    switch (type) {
      case TableEvent.TYPE_ROWS_UPDATED:
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ROWS_CHECKED:
      case TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED:
      case TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED:
      case TableEvent.TYPE_COLUMN_HEADERS_UPDATED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }

  protected boolean isRowsRequired(int type) {
    switch (type) {
      case TableEvent.TYPE_ROW_ACTION:
      case TableEvent.TYPE_ROW_CLICK:
      case TableEvent.TYPE_ROW_DROP_ACTION:
      case TableEvent.TYPE_ROW_ORDER_CHANGED:
      case TableEvent.TYPE_ROWS_COPY_REQUEST:
      case TableEvent.TYPE_ROWS_DELETED:
      case TableEvent.TYPE_ROWS_DRAG_REQUEST:
      case TableEvent.TYPE_ROWS_INSERTED:
      case TableEvent.TYPE_ROWS_UPDATED: {
        return true;
      }
      default: {
        return false;
      }
    }
  }
}
