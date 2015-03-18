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
  protected List<? extends TableEvent> coalesce(List<TableEvent> list) {
    removeObsolete(list);
    replacePrevious(list, TableEvent.TYPE_ROWS_INSERTED, TableEvent.TYPE_ROWS_UPDATED);
    coalesceSameType(list);
    return list;
  }

  /**
   * Remove previous events that are now obsolete.
   */
  private void removeObsolete(List<TableEvent> list) {
    //traverse the list in reversed order
    //previous events may be deleted from the list
    for (int j = 0; j < list.size() - 1; j++) {
      int i = list.size() - 1 - j;
      TableEvent cur = list.get(i);

      final int type = cur.getType();

      //remove all previous row related events
      if (type == TableEvent.TYPE_ALL_ROWS_DELETED) {
        remove(getRowRelatedEvents(), list.subList(0, i));
      }
      //remove all previous events of the same type
      else if (isIgnorePrevious(type)) {
        remove(type, list.subList(0, i));
      }
      else if (type == TableEvent.TYPE_ROWS_DELETED) {
        removeRows(cur.getRows(), list.subList(0, i),
            TableEvent.TYPE_ROWS_UPDATED,
            TableEvent.TYPE_ROWS_CHECKED);
      }
    }
  }

  /**
   * Traverses the list backwards and removes rows of a given type from a list. If the row list becomes empty the whole
   * event is removed. This process is stopped, when a event that may change row indexes is encountered.
   */
  private void removeRows(List<ITableRow> rows, List<TableEvent> list, Integer... types) {
    final List<Integer> typeList = Arrays.asList(types);
    final ListIterator<TableEvent> iter = list.listIterator(list.size());
    while (iter.hasPrevious()) {
      final TableEvent e = iter.previous();
      if (typeList.contains(e.getType())) {
        removeRows(rows, e);
        if (e.getRowCount() == 0) {
          iter.remove();
        }
      }
      if (!isRowOrderUnchanged(e.getType())) {
        break;
      }

    }

  }

  private void removeRows(List<ITableRow> rows, TableEvent e) {
    final Map<Integer, ITableRow> rowIndexes = getRowIndexMap(rows);
    final List<ITableRow> newRows = new ArrayList<>();

    for (ITableRow r : e.getRows()) {
      if (!rowIndexes.containsKey(r.getRowIndex())) {
        newRows.add(r);
      }
    }
    e.setRows(newRows);
  }

  /**
   * Update a previous event of given type and removes a newer one of another type.
   */
  private void replacePrevious(List<TableEvent> list, int oldType, int newType) {
    for (int j = 0; j < list.size() - 1; j++) {
      int i = list.size() - 1 - j;
      TableEvent cur = list.get(i);
      final int type = cur.getType();
      //merge current update event with previous insert event of the same row
      if (type == newType && cur.getRowCount() == 1) {
        final boolean updated = updatePreviousRow(cur, list.subList(0, i), oldType);
        if (updated) {
          list.remove(i);
        }
      }
    }
  }

  /**
   * Merge previous events of the same type (rows and columns) into the current and delete the previous events
   */
  private void coalesceSameType(List<TableEvent> list) {
    for (int j = 0; j < list.size() - 1; j++) {
      int i = list.size() - 1 - j;
      TableEvent cur = list.get(i);

      final int type = cur.getType();
      if (isCoalesceConsecutivePrevious(type)) {
        coalesce(cur, list.subList(0, i));
      }
    }
  }

  /**
   * Updates previous rows in the list, if it is of the given type. Breaks, if events are encountered, that may change
   * the row order.
   */
  private boolean updatePreviousRow(TableEvent cur, List<TableEvent> list, int type) {
    final ListIterator<TableEvent> iter = list.listIterator(list.size());
    while (iter.hasPrevious()) {
      final TableEvent previous = iter.previous();
      if (previous.getType() == type) {
        final boolean replaced = tryReplaceRow(previous, cur.getFirstRow());
        if (replaced) {
          return true;
        }
      }
      if (!isRowOrderUnchanged(previous.getType())) {
        return false;
      }
    }
    return false;
  }

  /**
   * @return <code>true</code>, if the event does not influence the row order.
   */
  private boolean isRowOrderUnchanged(int type) {
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

  /**
   * Replaces the row in the event, if it is contained.
   *
   * @return <code>true</code>, if successful.
   */
  private boolean tryReplaceRow(TableEvent e, ITableRow newRow) {
    final List<ITableRow> rows = new ArrayList<>();
    boolean replaced = false;
    for (ITableRow r : e.getRows()) {
      if (r.getRowIndex() == newRow.getRowIndex()) {
        rows.add(newRow);
        replaced = true;
      }
      else {
        rows.add(r);
      }
    }
    e.setRows(rows);
    return replaced;
  }

  /**
   * Merge events of the same type in the given list (rows and columns) into the current and delete the other events
   * from the list.
   */
  private void coalesce(TableEvent cur, List<TableEvent> list) {
    final ListIterator<TableEvent> iter = list.listIterator(list.size());
    while (iter.hasPrevious()) {
      final TableEvent previous = iter.previous();
      if (cur.getType() == previous.getType()) {
        merge(previous, cur);
        iter.remove();
      }
      else {
        return;
      }
    }
  }

  /**
   * Adds rows and columns
   */
  private TableEvent merge(TableEvent first, TableEvent second) {
    second.setColumns(mergeColumns(first.getColumns(), second.getColumns()));
    second.setRows(mergeRows(first.getRows(), second.getRows()));
    return second;
  }

  /**
   * Merge list of rows, such that, if a row of the same index is in both lists, only the one of the second list (later
   * event) is kept.
   */
  private List<ITableRow> mergeRows(List<ITableRow> first, List<ITableRow> second) {
    List<ITableRow> rows = new ArrayList<>();
    Map<Integer, ITableRow> rowIndexes = getRowIndexMap(second);

    for (ITableRow r : first) {
      if (!rowIndexes.containsKey(r.getRowIndex())) {
        rows.add(r);
      }
    }

    for (ITableRow r : second) {
      rows.add(r);
    }
    return rows;
  }

  private Map<Integer, ITableRow> getRowIndexMap(List<ITableRow> rows) {
    Map<Integer, ITableRow> rowIndexes = new HashMap<>();
    for (ITableRow r : rows) {
      rowIndexes.put(r.getRowIndex(), r);
    }
    return rowIndexes;
  }

  /**
   * Merge list of cols, such that, if a column of the same index is in both lists, only the one of the second list
   * (later
   * event) is kept.
   */
  private Collection<IColumn<?>> mergeColumns(Collection<IColumn<?>> first, Collection<IColumn<?>> second) {
    List<IColumn<?>> cols = new ArrayList<>();
    HashMap<Integer, IColumn<?>> colIndexes = new HashMap<>();

    for (IColumn<?> r : second) {
      colIndexes.put(r.getColumnIndex(), r);
    }

    for (IColumn<?> r : first) {
      if (!colIndexes.containsKey(r.getColumnIndex())) {
        cols.add(r);
      }
    }

    for (IColumn<?> r : second) {
      cols.add(r);
    }
    return cols;
  }

  private List<Integer> getRowRelatedEvents() {
    final ArrayList<Integer> res = new ArrayList<>();
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

  /**
   * @param type
   * @return true, if previous events of the same type can be ignored. false otherwise
   */
  private boolean isIgnorePrevious(int type) {
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
  private boolean isCoalesceConsecutivePrevious(int type) {
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

}
