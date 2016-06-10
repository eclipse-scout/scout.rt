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

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
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
    if (events.size() < 2) {
      return;
    }

    // traverse the list in reversed order
    // previous events may be deleted from the list
    final Set<Integer> typesToDelete = new HashSet<>();
    final Set<Integer> typesToClear = new HashSet<>();
    final Set<ITableRow> rowsToRemove = new HashSet<>();
    final Set<Integer> rowRelatedEventTypes = getRowRelatedEvents();
    final List<DeletedRowsRemover> deletedRowsRemoverList = new LinkedList<>();

    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final TableEvent event = it.previous();
      final int type = event.getType();

      // process deleted rows remover first so that unused rows are removed from delete events
      if (!deletedRowsRemoverList.isEmpty()) {
        for (Iterator<DeletedRowsRemover> removerIt = deletedRowsRemoverList.iterator(); removerIt.hasNext();) {
          final DeletedRowsRemover remover = removerIt.next();
          remover.removeDeletedRows(event);
          if (!isRowOrderUnchanged(type)) {
            remover.complete();
            removerIt.remove();
          }
        }
      }

      // handle types to delete
      if (typesToDelete.contains(type)) {
        it.remove();
        continue;
      }

      // handle types to clear or row to remove
      if (typesToClear.contains(type)) {
        event.clearRows();
      }
      else if (rowRelatedEventTypes.contains(type)) {
        event.removeRows(rowsToRemove, null);
      }

      if (type == TableEvent.TYPE_ALL_ROWS_DELETED) {
        // remove all row related events from the given event list if the event type requires rows. If the event type is row
        // related but rows are not required (e.g. ROWS_SELECTED), the event is not removed, but all rows are stripped.
        // Exception: {@link TableEvent#TYPE_SCROLL_TO_SELECTION} does not require rows, but is removed nevertheless (because
        // scrolling is pointless without rows).
        for (Integer rowRelatedType : rowRelatedEventTypes) {
          if (isRowsRequired(rowRelatedType) || rowRelatedType == TableEvent.TYPE_SCROLL_TO_SELECTION) {
            typesToDelete.add(rowRelatedType);
          }
          else {
            typesToClear.add(rowRelatedType);
          }
        }
      }
      else if (type == TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED) {
        // ignore all previous aggregate function changes.
        typesToDelete.add(TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
        typesToDelete.add(TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
        typesToDelete.add(TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
      }
      else if (isIgnorePrevious(type)) {
        // remove all previous events of the same type
        typesToDelete.add(type);
      }
      else if (type == TableEvent.TYPE_ROWS_INSERTED) {
        rowsToRemove.addAll(event.getRows());
      }
      else if (type == TableEvent.TYPE_ROWS_DELETED && event.hasRows()) {
        deletedRowsRemoverList.add(new DeletedRowsRemover(event));
      }
    }

    // complete deleted rows remover
    for (DeletedRowsRemover remover : deletedRowsRemoverList) {
      remover.complete();
    }
  }

  /**
   * Update a previous event of given type and removes a newer one of another type.
   */
  protected void replacePrevious(List<TableEvent> events, int oldType, int newType) {
    if (events.size() < 2) {
      return;
    }

    final List<CommonRowsRemover> commonRowsRemovers = new LinkedList<>();
    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final TableEvent event = it.previous();
      final int type = event.getType();

      if (type == newType && event.hasRows()) {
        CommonRowsRemover remover = new CommonRowsRemover(event);
        commonRowsRemovers.add(remover);
      }
      else if (type == oldType && event.hasRows()) {
        // apply to accumulated removers
        for (CommonRowsRemover remover : commonRowsRemovers) {
          remover.removeCommonRows(event);
        }
      }
      if (!isRowOrderUnchanged(type)) {
        // complete and reset common row removers
        for (CommonRowsRemover remover : commonRowsRemovers) {
          remover.complete();
        }
        commonRowsRemovers.clear();
      }
    }

    // complete remaining common row removers
    for (CommonRowsRemover remover : commonRowsRemovers) {
      remover.complete();
    }
  }

  /**
   * Merge previous events of the same type (rows and columns) into the current and delete the previous events
   */
  protected void coalesceSameType(List<TableEvent> events) {
    if (events.size() < 2) {
      return;
    }

    final Map<Integer, TableEvent> initialEventByType = new HashMap<>();
    final Map<Integer, TableEventMerger> eventMergerByType = new HashMap<>();

    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final TableEvent event = it.previous();
      final int type = event.getType();
      final boolean rowRelatedEvent = isRowRelatedEvent(type);

      // clean-up initial event and event merger maps
      if (!initialEventByType.isEmpty()) {
        for (Iterator<Entry<Integer, TableEvent>> initialEventIt = initialEventByType.entrySet().iterator(); initialEventIt.hasNext();) {
          final Entry<Integer, TableEvent> entry = initialEventIt.next();
          final int previousEventType = entry.getKey().intValue();
          if (type != previousEventType && rowRelatedEvent == isRowRelatedEvent(previousEventType)) {
            // Stop merging events if the event one of its previous events are of the same "relation type"
            // (e.g. both row-related or both non-row-related)
            initialEventIt.remove();
            TableEventMerger eventMerger = eventMergerByType.remove(previousEventType);
            if (eventMerger != null) {
              eventMerger.complete();
            }
          }
        }
      }

      if (!isCoalesceConsecutivePrevious(type)) {
        continue;
      }

      final TableEvent initialEvent = initialEventByType.get(type);
      if (initialEvent == null) {
        // this is the first event of given type.
        // put it into the initial event cache and continue with the next event
        initialEventByType.put(type, event);
        continue;
      }

      // there is already an initial event.
      // check if there is already an event merger or create one
      TableEventMerger eventMerger = eventMergerByType.get(type);
      if (eventMerger == null) {
        eventMerger = new TableEventMerger(initialEvent);
        eventMergerByType.put(type, eventMerger);
      }

      // merge current event and remove it from the original event list
      eventMerger.merge(event);
      it.remove();
    }

    // complete "open" event mergers
    for (TableEventMerger eventMerger : eventMergerByType.values()) {
      eventMerger.complete();
    }
  }

  /**
   * If a ROW_ORDER_CHANGED event happens directly after ROWS_INSERTED, we may removed the ROW_ORDER_CHANGED event and
   * send the new order in the ROWS_INSERTED event instead.
   */
  protected void applyRowOrderChangedToRowsInserted(List<TableEvent> events) {
    if (events.size() < 2) {
      return;
    }

    List<Integer> eventIndexesToDelete = new ArrayList<Integer>();
    for (ListIterator<TableEvent> it = events.listIterator(events.size()); it.hasPrevious();) {
      final int eventIndex = it.previousIndex();
      final TableEvent event = it.previous();
      if (event.getType() == TableEvent.TYPE_ROW_ORDER_CHANGED) {
        while (it.hasPrevious()) {
          final TableEvent previous = it.previous();
          if (previous.getType() == TableEvent.TYPE_ROWS_INSERTED
              && event.getRowCount() == previous.getRowCount()
              && CollectionUtility.equalsCollection(event.getRows(), previous.getRows(), false)) {

            // replace rows and mark ROW_ORDER_CHANGED event to be removed
            previous.setRows(event.getRows());
            eventIndexesToDelete.add(eventIndex);
            break;
          }
          if (!isRowOrderUnchanged(previous.getType())) {
            // rowOrder has been affected by the given event that is not of type TYPE_ROWS_INSERTED.
            // Hence the outer loop can look for the next TYPE_ROW_ORDER_CHANGED_EVENT without revisiting the events
            // seen in this while-loop. However, the current event has to be un-visited if its type is TYPE_ROW_ORDER_CHANGED.
            if (previous.getType() == TableEvent.TYPE_ROW_ORDER_CHANGED) {
              it.next();
            }
            break;
          }
        }
      }
    }

    if (eventIndexesToDelete.isEmpty()) {
      return;
    }

    // remove marked rows
    ListIterator<TableEvent> it = events.listIterator(events.size());
    for (int index : eventIndexesToDelete) {
      while (it.hasPrevious()) {
        int currentEventIndex = it.previousIndex();
        it.previous();
        if (currentEventIndex == index) {
          it.remove();
          // get next event position to delete
          break;
        }
      }
    }
  }

  protected void removeEmptyEvents(List<TableEvent> events) {
    for (Iterator<TableEvent> it = events.iterator(); it.hasNext();) {
      TableEvent event = it.next();
      if (isRowsRequired(event.getType()) && !event.hasRows()) {
        it.remove();
      }
    }
  }

  /**
   * Removes identical events (same type and content) when they occur consecutively (not necessarily directly, but
   * within the same type group). The oldest event is preserved.
   */
  protected void removeIdenticalEvents(List<TableEvent> events) {
    if (events.size() < 2) {
      return;
    }

    // Please note: In contrast to all other methods in this class, this method loops through the
    // list in FORWARD direction (so the oldest event will be kept).
    final ListIterator<TableEvent> it = events.listIterator();
    Map<Integer, List<TableEvent>> predecessorEventsOfSameType = new HashMap<>();
    int currentEventGroupType = -1;

    while (it.hasNext()) {
      final TableEvent event = it.next();

      if (event.getType() != currentEventGroupType) {
        // first event of next group. Initialize group related data.
        currentEventGroupType = event.getType();
        predecessorEventsOfSameType.clear();
        if (lookAheadEventType(it) == currentEventGroupType) {
          predecessorEventsOfSameType.put(identicalEventHashCode(event), CollectionUtility.arrayList(event));
        }
        continue;
      }

      // event belongs to the same group. Check whether it is identical to one of its predecessors.
      boolean removed = false;
      int tableEventHashCode = identicalEventHashCode(event);
      List<TableEvent> identidalEventList = predecessorEventsOfSameType.get(tableEventHashCode);
      if (identidalEventList != null) {
        for (TableEvent predecessorEvent : identidalEventList) {
          if (isIdenticalEvent(event, predecessorEvent)) {
            it.remove();
            removed = true;
            break;
          }
        }
      }
      if (!removed) {
        if (identidalEventList == null && lookAheadEventType(it) == currentEventGroupType) {
          identidalEventList = new ArrayList<>();
          predecessorEventsOfSameType.put(tableEventHashCode, identidalEventList);
        }
        if (identidalEventList != null) {
          identidalEventList.add(event);
        }
      }
    }
  }

  /**
   * @return Returns the next event's type or <code>-1</code> if {@link ListIterator#hasNext()} returns
   *         <code>false</code>. The iterator is moved back to its initial position (i.e.
   *         {@link ListIterator#previous()}).
   */
  private int lookAheadEventType(ListIterator<TableEvent> it) {
    if (!it.hasNext()) {
      return -1;
    }
    try {
      return it.next().getType();
    }
    finally {
      it.previous();
    }
  }

  /**
   * Computes a hash value for identical table events. This method must be kept in sync with
   * {@link #isIdenticalEvent(TableEvent, TableEvent)}.
   */
  protected int identicalEventHashCode(TableEvent event) {
    final int prime = 31;
    int result = 1;
    result = prime * result + event.getType();
    result = prime * result + (event.isConsumed() ? 1231 : 1237);
    result = prime * result + (event.isSortInMemoryAllowed() ? 1231 : 1237);

    List<ITableRow> rows = event.getRows();
    result = prime * result + ((rows == null) ? 0 : rows.hashCode());

    List<IMenu> popupMenus = event.getPopupMenus();
    result = prime * result + ((popupMenus == null) ? 0 : popupMenus.hashCode());

    TransferObject dragObject = event.getDragObject();
    result = prime * result + ((dragObject == null) ? 0 : dragObject.hashCode());

    TransferObject dropObject = event.getDropObject();
    result = prime * result + ((dropObject == null) ? 0 : dropObject.hashCode());

    TransferObject copyObject = event.getCopyObject();
    result = prime * result + ((copyObject == null) ? 0 : copyObject.hashCode());

    Collection<IColumn<?>> columns = event.getColumns();
    result = prime * result + ((columns == null) ? 0 : columns.hashCode());
    return result;
  }

  @Override
  protected boolean isIdenticalEvent(TableEvent event1, TableEvent event2) {
    if (event1 == null && event2 == null) {
      return true;
    }
    if (event1 == null || event2 == null) {
      return false;
    }
    boolean identical = event1.getType() == event2.getType()
        && event1.isConsumed() == event2.isConsumed()
        && event1.isSortInMemoryAllowed() == event2.isSortInMemoryAllowed()
        && event1.getRowCount() == event2.getRowCount()
        && CollectionUtility.equalsCollection(event1.getRows(), event2.getRows())
        && CollectionUtility.equalsCollection(event1.getPopupMenus(), event2.getPopupMenus())
        && CompareUtility.equals(event1.getDragObject(), event2.getDragObject())
        && CompareUtility.equals(event1.getDropObject(), event2.getDropObject())
        && CompareUtility.equals(event1.getCopyObject(), event2.getCopyObject())
        && CompareUtility.equals(event1.getColumns(), event2.getColumns());
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

  protected Set<Integer> getRowRelatedEvents() {
    Set<Integer> res = new HashSet<>();
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

  /**
   * Helper for merging rows and columns form other {@link TableEvent}s into the initial target event. <br/>
   * <b>Note</b>: The {@link #merge(TableEvent)} method does not check any rules. The given event's rows and columns are
   * merged in any case.<br/>
   * <b>Usage</b>:
   *
   * <pre>
   * TableEventMerger eventMerger = new TableEventMerger(targetEvent);
   * eventMerger.merge(e1);
   * eventMerger.merge(e2);
   * eventMerger.complete();
   * </pre>
   */
  protected static class TableEventMerger {

    private final TableEvent m_targetEvent;
    private final Collection<IColumn<?>> m_targetColumns;
    private final HashSet<IColumn<?>> m_targetColumnSet;
    private final List<ITableRow> m_targetRows;
    private final HashSet<ITableRow> m_targetRowSet;

    private List<IColumn<?>> m_mergedColumns;
    private List<ITableRow> m_mergedRows;

    public TableEventMerger(TableEvent targetEvent) {
      assertNotNull(targetEvent, "targetEvent must not be null");
      m_targetEvent = targetEvent;
      m_targetColumns = targetEvent.getColumns();
      m_targetColumnSet = new HashSet<>(m_targetColumns);
      m_mergedColumns = new LinkedList<>();
      m_targetRows = targetEvent.getRows();
      m_targetRowSet = new HashSet<>(m_targetRows);
      m_mergedRows = new LinkedList<>();
    }

    /**
     * Merges rows and columns. Using this method after invoking {@link #complete()} throws an
     * {@link IllegalStateException}.
     */
    public void merge(TableEvent event) {
      if (m_mergedColumns == null || m_mergedRows == null) {
        throw new IllegalStateException("Invocations of merge is not allowed after complete has been invoked.");
      }
      mergeCollections(event.getColumns(), m_mergedColumns, m_targetColumnSet);
      mergeCollections(event.getRows(), m_mergedRows, m_targetRowSet);
    }

    /**
     * Completes the merge process. Subsequent invocations of this method does not have any effects.
     */
    public void complete() {
      if (m_mergedColumns == null || m_mergedRows == null) {
        return;
      }
      m_mergedColumns.addAll(m_targetColumns);
      m_targetEvent.setColumns(m_mergedColumns);
      m_mergedColumns = null;

      m_mergedRows.addAll(m_targetRows);
      m_targetEvent.setRows(m_mergedRows);
      m_mergedRows = null;
    }

    /**
     * Merge collections, such that, if an element is in both collections, only the one of the second collection (later
     * event) is kept.
     */
    protected <TYPE> void mergeCollections(Collection<TYPE> source, List<TYPE> target, HashSet<TYPE> targetSet) {
      for (Iterator<TYPE> it = source.iterator(); it.hasNext();) {
        TYPE sourceElement = it.next();
        if (!targetSet.add(sourceElement)) { // returns true, if the sourceElement has been added; false, if it was already in the set.
          it.remove();
        }
      }
      target.addAll(0, source);
    }
  }

  /**
   * Helper for removing rows form an initial {@link TableEvent}. <br/>
   * <b>Usage</b>:
   *
   * <pre>
   * CommonRowsRemover rowsRemover = new CommonRowsRemover(initialEvent);
   * rowsRemover.removeCommonRows(e1);
   * rowsRemover.removeCommonRows(e2);
   * rowsRemover.complete();
   * </pre>
   */
  protected static class CommonRowsRemover {

    private final TableEvent m_initialEvent;
    private final List<ITableRow> m_rows;

    public CommonRowsRemover(TableEvent initialEvent) {
      m_initialEvent = initialEvent;
      m_rows = new LinkedList<>(m_initialEvent.getRows());
    }

    public void removeCommonRows(TableEvent event) {
      if (event == null || !event.hasRows() || m_rows.isEmpty()) {
        return;
      }

      for (Iterator<ITableRow> it = m_rows.iterator(); it.hasNext();) {
        ITableRow next = it.next();
        if (event.containsRow(next)) {
          it.remove();
        }
      }
    }

    public void complete() {
      if (m_rows.isEmpty()) {
        m_initialEvent.clearRows();
      }
      else {
        m_initialEvent.setRows(m_rows);
      }
    }
  }

  /**
   * Removes the rows from the initial delete event from all events passed to the
   * {@link DeletedRowsRemover#removeDeletedRows(TableEvent)} method. If a row to delete is part of an insert event, it
   * is removed from the initial delete event as well. The process must be stopped if a
   * {@link TableEvent#TYPE_ROW_ORDER_CHANGED} event is seen.
   * <p/>
   * This implementation uses lazy initialization of helper data structure for performance reasons.
   */
  protected static class DeletedRowsRemover {

    private final TableEvent m_deleteEvent;
    private Set<ITableRow> m_rowsToRemove;
    private Set<ITableRow> m_removedRowsCollector;

    public DeletedRowsRemover(TableEvent deleteEvent) {
      m_deleteEvent = deleteEvent;
    }

    public void removeDeletedRows(TableEvent event) { // never remove rows from a previous row order changed / checked event. Even when the row is deleted later,
      // the UI expects the row to be still available at the point where the row order / checked state is changed.
      if (TableEvent.TYPE_ROW_ORDER_CHANGED != event.getType()) {
        ensureInitizlized();
        event.removeRows(m_rowsToRemove, event.getType() == TableEvent.TYPE_ROWS_INSERTED ? m_removedRowsCollector : null);
      }
    }

    protected void ensureInitizlized() {
      if (m_removedRowsCollector != null) {
        return;
      }
      m_rowsToRemove = m_deleteEvent.getRowsSet();
      m_removedRowsCollector = new HashSet<>();
    }

    public void complete() {
      if (CollectionUtility.isEmpty(m_removedRowsCollector)) {
        // the original delete event must not be modified
        return;
      }

      List<ITableRow> remainingRows = new ArrayList<ITableRow>();
      for (ITableRow row : m_deleteEvent.getRows()) {
        if (!m_removedRowsCollector.contains(row)) {
          remainingRows.add(row);
        }
      }
      m_deleteEvent.setRows(remainingRows);
    }
  }
}
