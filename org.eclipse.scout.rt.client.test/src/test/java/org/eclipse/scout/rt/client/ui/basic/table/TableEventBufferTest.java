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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link TableEventBuffer}
 */
public class TableEventBufferTest {

  private TableEventBuffer m_testBuffer;

  // Maps to generate each row/column only once (TableEventBuffer checks for reference equality)
  private Map<Integer, ITableRow> m_mockRows;
  private Map<Integer, IColumn<?>> m_mockColumns;

  @Before
  public void setup() {
    m_testBuffer = new TableEventBuffer();
    m_mockRows = new HashMap<>();
    m_mockColumns = new HashMap<>();
  }

  /**
   * EventBuffer should be initially empty.
   */
  @Test
  public void testEmpty() {
    assertTrue(m_testBuffer.isEmpty());
    assertTrue(m_testBuffer.consumeAndCoalesceEvents().isEmpty());
  }

  /**
   * A single event should remain the same when removed from the buffer.
   */
  @Test
  public void testSingleEvent() {
    final TableEvent se = mockEvent(TableEvent.TYPE_ROWS_SELECTED);
    m_testBuffer.add(se);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(se, events.iterator().next());
    assertTrue(m_testBuffer.isEmpty());
  }

  /**
   * Some events should not be coalesced: selected, updated, row_action.
   */
  @Test
  public void testNoCoalesce() {
    final TableEvent e1 = mockEvent(TableEvent.TYPE_ROWS_SELECTED);
    final TableEvent e2 = mockEvent(TableEvent.TYPE_ROWS_UPDATED, 1);
    final TableEvent e3 = mockEvent(TableEvent.TYPE_ROW_ACTION, 1);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    final List<TableEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, coalesced.size());
    assertSame(e1, coalesced.get(0));
    assertSame(e2, coalesced.get(1));
    assertSame(e3, coalesced.get(2));
  }

  /**
   * Only the last selection event should be kept.
   */
  @Test
  public void testSelections() {
    final TableEvent se1 = mockEvent(TableEvent.TYPE_ROWS_SELECTED);
    final TableEvent se2 = mockEvent(TableEvent.TYPE_SCROLL_TO_SELECTION);
    final TableEvent se3 = mockEvent(TableEvent.TYPE_ROWS_SELECTED);
    m_testBuffer.add(se1);
    m_testBuffer.add(se2);
    m_testBuffer.add(se3);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertSame(se2, events.get(0));
    assertSame(se3, events.get(1));
  }

  /**
   * If all rows are deleted, previous row related events should be removed: row_action, scroll_to_selection,... Other
   * events should remain in the list.
   */
  @Test
  public void testAllRowsDeleted() {
    m_testBuffer.add(mockEvent(TableEvent.TYPE_SCROLL_TO_SELECTION));
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROW_ACTION));
    final TableEvent columnEvent = mockEvent(TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    m_testBuffer.add(columnEvent);
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROW_CLICK));
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROW_ORDER_CHANGED));
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROWS_INSERTED));
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROWS_DELETED));
    TableEvent rowsSelectedEvent = mockEvent(TableEvent.TYPE_ROWS_SELECTED, 2);
    m_testBuffer.add(rowsSelectedEvent);
    final TableEvent allDeletedEvent = mockEvent(TableEvent.TYPE_ALL_ROWS_DELETED);
    m_testBuffer.add(allDeletedEvent);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
    assertSame(columnEvent, events.get(0));
    assertSame(rowsSelectedEvent, events.get(1));
    assertSame(0, events.get(1).getRowCount());
    assertSame(allDeletedEvent, events.get(2));
  }

  /**
   * Multiple update events should be merged into a single event with the rows combined in the correct order.
   */
  @Test
  public void testCombineMultipleUpdates() {
    List<ITableRow> rows1 = new ArrayList<>();
    final ITableRow r1 = mockRow(0);
    final ITableRow r2 = mockRow(1);
    rows1.add(r1);
    rows1.add(r2);
    final TableEvent e1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, rows1);
    List<ITableRow> rows2 = new ArrayList<>();
    final ITableRow r3 = mockRow(2);
    rows2.add(r3);
    final TableEvent e2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, rows2);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);

    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();

    assertEquals(1, events.size());
    final List<ITableRow> resultRows = events.get(0).getRows();
    assertEquals(3, resultRows.size());

    List<ITableRow> expected = new ArrayList<>();
    expected.add(r1);
    expected.add(r2);
    expected.add(r3);
    assertTrue(CollectionUtility.equalsCollection(expected, resultRows));
  }

  /**
   * Multiple insert events on the same row should be merged into a single event with the rows combined in the correct
   * order.
   */
  @Test
  public void testCombineMultipleInsertsSameRows() {
    final TableEvent e1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1, 2));
    final TableEvent e2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(1, 3));
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);

    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();

    assertEquals(1, events.size());
    final List<ITableRow> resultRows = events.get(0).getRows();
    assertEquals(4, resultRows.size());
  }

  /**
   * Multiple consecutive column headers updated should be merged in such a way that only the last event is kept with
   * the merged columns.
   */
  @Test
  public void testCombineMultipleInsertsSameRow() {
    List<IColumn<?>> cols1 = new ArrayList<>();
    final IColumn<?> c1 = mockColumn(0);

    final IColumn<?> c2 = mockColumn(1);
    cols1.add(c1);
    cols1.add(c2);
    List<IColumn<?>> cols2 = new ArrayList<>();
    final IColumn<?> c3 = mockColumn(0);
    cols2.add(c3);
    final TableEvent e1 = mockEvent(TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    e1.setColumns(cols1);
    m_testBuffer.add(e1);
    final TableEvent e2 = mockEvent(TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    e2.setColumns(cols2);
    m_testBuffer.add(e2);

    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();

    assertEquals(1, events.size());
    final Collection<IColumn<?>> resultRows = events.get(0).getColumns();
    assertEquals(2, resultRows.size());

    List<IColumn<?>> expected = new ArrayList<>();
    expected.add(c2);
    expected.add(c3);
    assertTrue(CollectionUtility.equalsCollection(expected, resultRows));
  }

  /**
   * Updates that are not consecutive are not combined.
   */
  @Test
  public void testCombineOnlyConsecutiveUpdates() {
    final TableEvent e1 = createTestUpdateEvent();
    m_testBuffer.add(e1);
    m_testBuffer.add(mockEvent(TableEvent.TYPE_ROWS_INSERTED, 1));
    final TableEvent e2 = createTestUpdateEvent();
    m_testBuffer.add(e2);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
    assertEquals(2, events.get(0).getRows().size());
    assertEquals(1, events.get(1).getRows().size());
    assertEquals(1, events.get(2).getRows().size()); // one was merge to insert
  }

  ////// REPLACE

  /**
   * If a row is inserted and later updated, only an insert event with the updated value needs to be kept.
   */
  @Test
  public void testInsertedFollowedByUpdated() {
    final TableEvent insert = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0));

    final List<ITableRow> updatedRows = mockRows(0);
    when(updatedRows.get(0).getCell(0)).thenReturn(mock(ICell.class));
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, updatedRows);

    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertNotNull(events.get(0).getFirstRow().getCell(0));
  }

  /**
   * If multiple rows are inserted later a row is updated, it should be merged and the event removed.
   */
  @Test
  public void testInsertedFollowedUpdatedMultipleRows() {
    final TableEvent insert = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1));
    final List<ITableRow> updatedRows = mockRows(0);
    when(updatedRows.get(0).getCell(0)).thenReturn(mock(ICell.class));
    final TableEvent otherUpdate = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(3, 4));
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, updatedRows);
    m_testBuffer.add(insert);
    m_testBuffer.add(otherUpdate);
    m_testBuffer.add(update);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(2, events.get(0).getRowCount());
    assertNotNull(events.get(0).getFirstRow().getCell(0));
  }

  /**
   * If a row is inserted and another row is updated, the events are not merged
   */
  @Test
  public void testInsertedFollowedUpdatedNoMatch() {
    final TableEvent insert = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(1, 2, 3));
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0));
    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
  }

  /**
   * If a row is inserted and later updated, but there is a TableEvent, there should be no merge.
   */
  @Test
  public void testInsertedFollowedUpdatedIndexChanging() {
    final TableEvent insert = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(1, 2, 3));
    final TableEvent rowFilterChanged = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROW_FILTER_CHANGED);
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0));
    m_testBuffer.add(insert);
    m_testBuffer.add(rowFilterChanged);
    m_testBuffer.add(update);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
  }

  @Test
  public void testUpdateFollowedByDeleteRemoved() {
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0));
    final TableEvent delete = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_DELETED, mockRows(0));
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(0).getType());
    assertEquals(1, events.get(0).getRowCount());
  }

  /**
   * Insert(r0,r1) + Update(r0,r2) + Delete(r0,r3) = Insert(r1) + Update(r2) + Delete(r3)
   */
  @Test
  public void testInsertAndUpdateFollowedByDelete() {
    final TableEvent insert = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1));
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0, 2));
    final TableEvent delete = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_DELETED, mockRows(0, 3));
    m_testBuffer.add(insert);
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, events.size());
    assertEquals(TableEvent.TYPE_ROWS_INSERTED, events.get(0).getType());
    assertEquals(TableEvent.TYPE_ROWS_UPDATED, events.get(1).getType());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(2).getType());
    assertEquals(1, events.get(0).getRowCount());
    assertEquals(1, events.get(1).getRowCount());
    assertEquals(1, events.get(2).getRowCount());
  }

  /**
   * <b>Events:</b>
   * <ul>
   * <li>Insert(r0,r1,r2,r3,4,r5,r6)
   * <li>Update(r0, r6)
   * <li>DeleteAll()
   * <li>Insert(r0,r1)
   * <li>Update(r0,r2)
   * <li>Delete(r0,r3)
   * </ul>
   * <b>Expected result:</b>
   * <ul>
   * <li>Insert(r1)
   * <li>Update(r2)
   * <li>Delete(r3)
   * </ul>
   */
  @Test
  public void testInsertDeleteAllInsertUpdateFollowedByDelete() {
    final TableEvent insert1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1, 2, 3, 4, 5, 6));
    final TableEvent update1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0, 6));
    final TableEvent deleteAll = new TableEvent(mock(ITable.class), TableEvent.TYPE_ALL_ROWS_DELETED);
    final TableEvent insert2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1));
    final TableEvent update2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0, 2));
    final TableEvent delete = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_DELETED, mockRows(0, 3));
    m_testBuffer.add(insert1);
    m_testBuffer.add(update1);
    m_testBuffer.add(deleteAll);
    m_testBuffer.add(insert2);
    m_testBuffer.add(update2);
    m_testBuffer.add(delete);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(4, events.size());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, events.get(0).getType());
    assertEquals(TableEvent.TYPE_ROWS_INSERTED, events.get(1).getType());
    assertEquals(TableEvent.TYPE_ROWS_UPDATED, events.get(2).getType());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(3).getType());
    assertEquals(0, events.get(0).getRowCount());
    assertEquals(1, events.get(1).getRowCount());
    assertEquals(1, events.get(2).getRowCount());
    assertEquals(1, events.get(3).getRowCount());
  }

  @Test
  public void testPopulatedFollowedByDeleteNotRemoved() {
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_TABLE_POPULATED, mockRows(0));
    final TableEvent delete = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_DELETED, mockRows(0));
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(TableEvent.TYPE_TABLE_POPULATED, events.get(0).getType());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(1).getType());
  }

  @Test
  public void testUpdateFollowedByDeleteNoMatch() {
    final TableEvent update = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0));
    final TableEvent delete = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_DELETED, mockRows(1));
    m_testBuffer.add(update);
    m_testBuffer.add(delete);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(1).getType());
  }

  @Test
  public void testInsertChangeRowOrder() {
    final TableEvent insert1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(0, 1, 2, 3, 4));
    final TableEvent insert2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_INSERTED, mockRows(6, 5));
    final TableEvent orderChanged = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROW_ORDER_CHANGED, mockRows(6, 5, 4, 3, 2, 1, 0));
    m_testBuffer.add(insert1);
    m_testBuffer.add(insert2);
    m_testBuffer.add(orderChanged);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertEquals(TableEvent.TYPE_ROWS_INSERTED, events.get(0).getType());
    assertEquals(7, events.get(0).getRowCount());
    assertEquals(6, events.get(0).getRows().get(0).getRowIndex());
    assertEquals(4, events.get(0).getRows().get(2).getRowIndex());
    assertEquals(0, events.get(0).getRows().get(6).getRowIndex());
  }

  @Test
  public void testCoalesceIdenticalEvents() {
    List<ITableRow> mockRows = mockRows(0, 1, 2, 3, 4);
    final TableEvent event1 = new TableEvent(mock(ITable.class), TableEvent.TYPE_TABLE_POPULATED, mockRows);
    final TableEvent event2 = new TableEvent(mock(ITable.class), TableEvent.TYPE_TABLE_POPULATED, mockRows);
    m_testBuffer.add(event1);
    m_testBuffer.add(event2);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, events.size());
    assertEquals(TableEvent.TYPE_TABLE_POPULATED, events.get(0).getType());
    assertEquals(5, events.get(0).getRowCount());
  }

  /**
   * If two rows are deleted separately, both should be present in the event list. This test checks that checks are
   * _not_ done by checking for the same rowIndex.
   */
  @Test
  public void testDeleteTwoRows() {
    List<ITableRow> mockRows = mockRows(0, 1, 2, 3, 4);
    ITable table = mock(ITable.class);
    final TableEvent event1 = new TableEvent(table, TableEvent.TYPE_ROWS_UPDATED, Collections.singletonList(mockRows.get(2)));
    final TableEvent event2 = new TableEvent(table, TableEvent.TYPE_ROWS_DELETED, Collections.singletonList(mockRows.get(2)));
    final TableEvent event3 = new TableEvent(table, TableEvent.TYPE_ROWS_UPDATED, Collections.singletonList(mockRows.get(3)));
    final TableEvent event4 = new TableEvent(table, TableEvent.TYPE_ROWS_DELETED, Collections.singletonList(mockRows.get(3)));
    final TableEvent event5 = new TableEvent(table, TableEvent.TYPE_ROW_ORDER_CHANGED, Arrays.asList(mockRows.get(4), mockRows.get(1), mockRows.get(0)));
    m_testBuffer.add(event1);
    m_testBuffer.add(event2);
    m_testBuffer.add(event3);
    m_testBuffer.add(event4);
    m_testBuffer.add(event5);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, events.size());
    assertEquals(TableEvent.TYPE_ROWS_DELETED, events.get(0).getType());
    assertEquals(2, events.get(0).getRowCount());
    assertEquals(TableEvent.TYPE_ROW_ORDER_CHANGED, events.get(1).getType());
    assertEquals(3, events.get(1).getRowCount());
  }

  @Test
  public void testRemoveObsoleteAggregationChanges() {
    ITable table = mock(ITable.class);
    final TableEvent event1 = new TableEvent(table, TableEvent.TYPE_COLUMN_HEADERS_UPDATED);
    final TableEvent event2 = new TableEvent(table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    final TableEvent event3 = new TableEvent(table, TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    final TableEvent event4 = new TableEvent(table, TableEvent.TYPE_ALL_ROWS_DELETED);
    final TableEvent event5 = new TableEvent(table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    final TableEvent event6 = new TableEvent(table, TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    final TableEvent event7 = new TableEvent(table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
    final TableEvent event8 = new TableEvent(table, TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    m_testBuffer.add(event1);
    m_testBuffer.add(event2);
    m_testBuffer.add(event3);
    m_testBuffer.add(event4);
    m_testBuffer.add(event5);
    m_testBuffer.add(event6);
    m_testBuffer.add(event7);
    m_testBuffer.add(event8);
    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(4, events.size());
    assertEquals(TableEvent.TYPE_COLUMN_HEADERS_UPDATED, events.get(0).getType());
    assertEquals(TableEvent.TYPE_ALL_ROWS_DELETED, events.get(1).getType());
    assertEquals(TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, events.get(2).getType());
    assertEquals(TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED, events.get(3).getType());
  }

  @Test
  public void testCoalesceAndRemoveObsoleteColumnBackgroundEffectEvents() {
    ITable table = mock(ITable.class);
    final IColumn<?> c1 = mockColumn(0);
    final IColumn<?> c2 = mockColumn(1);
    final IColumn<?> c3 = mockColumn(2);

    final TableEvent event0 = new TableEvent(table, TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    event0.setColumns(CollectionUtility.arrayList(c3));
    final TableEvent event1 = new TableEvent(table, TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    event1.setColumns(CollectionUtility.arrayList(c1));
    final TableEvent event2 = new TableEvent(table, TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED);
    event2.setColumns(CollectionUtility.arrayList(c2));
    final TableEvent event3 = new TableEvent(table, TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED);
    event3.setColumns(CollectionUtility.arrayList(c1));

    m_testBuffer.add(event0);
    m_testBuffer.add(new TableEvent(table, TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED));
    m_testBuffer.add(event1);
    m_testBuffer.add(event2);
    m_testBuffer.add(event3);

    final List<TableEvent> events = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(events.size(), 3);
    assertEquals(TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED, events.get(0).getType());
    assertEquals(TableEvent.TYPE_COLUMN_BACKGROUND_EFFECT_CHANGED, events.get(1).getType());
    assertEquals(TableEvent.TYPE_COLUMN_AGGREGATION_CHANGED, events.get(2).getType());
    TableEvent bgEffectEvent = events.get(1);
    assertEquals(2, bgEffectEvent.getColumns().size());
    assertTrue(bgEffectEvent.getColumns().containsAll(CollectionUtility.arrayList(c1, c2)));

  }

  private TableEvent createTestUpdateEvent() {
    return new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_UPDATED, mockRows(0, 1));
  }

  private TableEvent mockEvent(int type) {
    return mockEvent(type, 0);
  }

  private TableEvent mockEvent(int type, int rowCount) {
    List<ITableRow> rows = null;
    if (rowCount > 0) {
      rows = new ArrayList<>();
      for (int i = 0; i < rowCount; i++) {
        rows.add(mockRow(i));
      }
    }
    return new TableEvent(mock(ITable.class), type, rows);
  }

  private List<ITableRow> mockRows(int... indexes) {
    List<ITableRow> rows = new ArrayList<>();
    for (int i : indexes) {
      rows.add(mockRow(i));
    }
    return rows;
  }

  private ITableRow mockRow(int rowIndex) {
    ITableRow row = m_mockRows.get(rowIndex);
    if (row == null) {
      row = mock(ITableRow.class, "MockRow[" + rowIndex + "]");
      when(row.getRowIndex()).thenReturn(rowIndex);
      m_mockRows.put(rowIndex, row);
    }
    return row;
  }

  private IColumn<?> mockColumn(int colIndex) {
    IColumn<?> column = m_mockColumns.get(colIndex);
    if (column == null) {
      column = mock(IColumn.class, "MockColumn[" + colIndex + "]");
      when(column.getColumnIndex()).thenReturn(colIndex);
      m_mockColumns.put(colIndex, column);
    }
    return column;
  }
}
