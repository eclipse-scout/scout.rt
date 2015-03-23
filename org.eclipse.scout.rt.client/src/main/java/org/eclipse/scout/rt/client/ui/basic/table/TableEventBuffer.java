/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

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
    coalesceSameType(events);
    removeEmptyEvents(events);
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

      //remove all previous row related events
      if (type == TableEvent.TYPE_ALL_ROWS_DELETED) {
        remove(getRowRelatedEvents(), events.subList(0, i));
      }
      //remove all previous events of the same type
      else if (isIgnorePrevious(type)) {
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
   * @return a list with the same rows as 'rowsToRemove', except those that were removed from an
   *         event whose type matches one of the 'creationTypes'. This allows for completely removing
   *         a row that was created and deleted in the same request.
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
      if (row.getRowIndex() == rowToRemove.getRowIndex()) {
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
      int type = event.getType();

      //merge current update event with previous insert event of the same row
      if (type == newType) {
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
      int type = event.getType();

      if (isCoalesceConsecutivePrevious(type)) {
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
      if (row.getRowIndex() == newRow.getRowIndex()) {
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
        return;
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
   * Merge list of rows, such that, if a row of the same index is in both lists, only the one of the second list (later
   * event) is kept.
   */
  protected List<ITableRow> mergeRows(List<ITableRow> first, List<ITableRow> second) {
    List<ITableRow> rows = new ArrayList<>();
    Map<Integer, ITableRow> rowIndexes = getRowsByRowIndexMap(second);
    for (ITableRow row : first) {
      if (!rowIndexes.containsKey(row.getRowIndex())) {
        rows.add(row);
      }
    }
    for (ITableRow row : second) {
      rows.add(row);
    }
    return rows;
  }

  protected Map<Integer, ITableRow> getRowsByRowIndexMap(List<ITableRow> rows) {
    Map<Integer, ITableRow> rowsByRowIndex = new HashMap<>();
    for (ITableRow row : rows) {
      rowsByRowIndex.put(row.getRowIndex(), row);
    }
    return rowsByRowIndex;
  }

  /**
   * Merge list of cols, such that, if a column of the same index is in both lists, only the one of the second list
   * (later event) is kept.
   */
  protected Collection<IColumn<?>> mergeColumns(Collection<IColumn<?>> first, Collection<IColumn<?>> second) {
    List<IColumn<?>> cols = new ArrayList<>();
    Map<Integer, IColumn<?>> colIndexes = new HashMap<>();
    for (IColumn<?> column : second) {
      colIndexes.put(column.getColumnIndex(), column);
    }
    for (IColumn<?> column : first) {
      if (!colIndexes.containsKey(column.getColumnIndex())) {
        cols.add(column);
      }
    }
    for (IColumn<?> column : second) {
      cols.add(column);
    }
    return cols;
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
