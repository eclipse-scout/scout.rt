/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Test;

/**
 * Tests for {@link TableEvent}
 */
public class TableEventTest {

  @Test
  public void testToString() throws Exception {
    final TableEvent e = new TableEvent(mock(ITable.class), TableEvent.TYPE_ALL_ROWS_DELETED);
    assertTrue(e.toString().contains("TYPE_ALL_ROWS_DELETED"));
  }

  @Test
  public void testRowCountAndHasRowsZeroRows() {
    TableEvent e = new TableEvent(mock(ITable.class), TableEvent.TYPE_ALL_ROWS_DELETED);
    assertEquals(0, e.getRowCount());
    assertFalse(e.hasRows());
  }

  @Test
  public void testRowCountAndHasRowsOneRow() {
    TableEvent e = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_SELECTED, mockRows(0));
    assertEquals(1, e.getRowCount());
    assertTrue(e.hasRows());

    e.setRows(null);
    assertEquals(0, e.getRowCount());
    assertFalse(e.hasRows());
  }

  @Test
  public void testRowCountAndHasRowsTwoRows() {
    TableEvent e = new TableEvent(mock(ITable.class), TableEvent.TYPE_ROWS_SELECTED, mockRows(0, 1));
    assertEquals(2, e.getRowCount());
    assertTrue(e.hasRows());

    e.setRows(mockRows(0, 1, 2));
    assertEquals(3, e.getRowCount());
    assertTrue(e.hasRows());
  }

  @Test
  public void testContainsRows() {
    ITable table = mock(ITable.class);
    ITableRow r0 = mockRow(0);
    ITableRow r1 = mockRow(1);
    ITableRow r2 = mockRow(2);

    TableEvent event = new TableEvent(table, TableEvent.TYPE_ROWS_CHECKED, Arrays.asList(r0, r1));

    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));
    assertFalse(event.containsRow(r2));
  }

  @Test
  public void testRemoveRow() {
    ITable table = mock(ITable.class);
    ITableRow r0 = mockRow(0);
    ITableRow r1 = mockRow(1);
    ITableRow r2 = mockRow(2);

    TableEvent event = new TableEvent(table, TableEvent.TYPE_ROWS_CHECKED, Arrays.asList(r0, r1, r0));

    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));

    assertFalse(event.removeRow(r2));
    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));

    assertTrue(event.removeRow(r1));
    assertEquals(2, event.getRowCount());
    assertTrue(event.containsRow(r0));

    assertTrue(event.removeRow(r0));
    assertFalse(event.hasRows());
  }

  @Test
  public void testRemoveRows() {
    ITable table = mock(ITable.class);
    ITableRow r0 = mockRow(0);
    ITableRow r1 = mockRow(1);
    ITableRow r2 = mockRow(2);

    TableEvent event = new TableEvent(table, TableEvent.TYPE_ROWS_CHECKED, Arrays.asList(r0, r1, r0));

    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));

    Set<ITableRow> removedRowsCollector = new HashSet<>();

    // null-rows
    assertFalse(event.removeRows(null, removedRowsCollector));
    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));
    assertTrue(removedRowsCollector.isEmpty());

    // empty set
    assertFalse(event.removeRows(Collections.<ITableRow> emptySet(), removedRowsCollector));
    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));
    assertTrue(removedRowsCollector.isEmpty());

    // row not part of event
    assertFalse(event.removeRows(Collections.singleton(r2), removedRowsCollector));
    assertEquals(3, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertTrue(event.containsRow(r1));
    assertTrue(removedRowsCollector.isEmpty());

    // one row part of the event, the other not
    assertTrue(event.removeRows(CollectionUtility.hashSet(r1, r2), removedRowsCollector));
    assertEquals(2, event.getRowCount());
    assertTrue(event.containsRow(r0));
    assertEquals(Collections.singleton(r1), removedRowsCollector);

    // remove single row
    assertTrue(event.removeRows(Collections.singleton(r0), removedRowsCollector));
    assertFalse(event.hasRows());
    assertEquals(CollectionUtility.hashSet(r0, r1), removedRowsCollector);

    // remove from empty event, without collector
    assertFalse(event.removeRows(Collections.singleton(r0), null));
  }

  @Test
  public void testClearRows() {
    ITable table = mock(ITable.class);
    ITableRow r0 = mockRow(0);
    ITableRow r1 = mockRow(1);

    TableEvent event = new TableEvent(table, TableEvent.TYPE_ROWS_CHECKED, Arrays.asList(r0, r1));

    assertEquals(2, event.getRowCount());
    assertTrue(event.hasRows());

    event.clearRows();
    assertEquals(0, event.getRowCount());
    assertFalse(event.hasRows());

    event.clearRows();
    assertEquals(0, event.getRowCount());
    assertFalse(event.hasRows());
  }

  @Test
  public void testGetRowsSet() {
    ITable table = mock(ITable.class);
    TableEvent e0 = new TableEvent(table, TableEvent.TYPE_ROWS_UPDATED);
    assertEquals(Collections.emptySet(), e0.getRowsSet());

    ITableRow r0 = mockRow(0);
    e0.setRows(Collections.singletonList(r0));
    assertEquals(Collections.singleton(r0), e0.getRowsSet());

    ITableRow r1 = mockRow(1);
    e0.setRows(Arrays.asList(r0, r1));
    assertEquals(CollectionUtility.hashSet(r0, r1), e0.getRowsSet());

    e0.setRows(Arrays.asList(r0, r1, r0));
    assertEquals(Arrays.asList(r0, r1, r0), e0.getRows());
    assertEquals(CollectionUtility.hashSet(r0, r1), e0.getRowsSet());
  }

  private List<ITableRow> mockRows(int... indexes) {
    List<ITableRow> rows = new ArrayList<>();
    for (int i : indexes) {
      rows.add(mockRow(i));
    }
    return rows;
  }

  private ITableRow mockRow(int rowIndex) {
    ITableRow row = mock(ITableRow.class, "MockRow[" + rowIndex + "]");
    when(row.getRowIndex()).thenReturn(rowIndex);
    return row;
  }
}
