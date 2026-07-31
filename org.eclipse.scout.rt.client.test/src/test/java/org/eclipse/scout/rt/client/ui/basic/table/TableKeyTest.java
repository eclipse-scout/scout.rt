/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CompositeObject;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link AbstractTable}
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableKeyTest {

  private void expectRowOrder(Object[] primaryKeys, List<ITableRow> rows) {
    assertArrayEquals(primaryKeys, rows.stream().map(row -> row.getKeyValues().get(0)).toArray());
  }

  private void expectRowOrder(CompositeObject[] multiValuedPrimaryKeys, List<ITableRow> rows) {
    assertArrayEquals(multiValuedPrimaryKeys, rows.stream().map(row -> new CompositeObject(row.getKeyValues().toArray())).toArray());
  }

  @Test
  public void testKeyAndParentKey() {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    rows.add(table.createRow(new Object[]{3, 1}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new Object[]{1, 3, 2}, rows);
    assertEquals(rows.get(0), table.getRowByKey(table.getRowKeys(rows.get(0))));
    assertEquals(rows.get(1), table.getRowByKey(table.getRowKeys(rows.get(1))));
    assertEquals(rows.get(2), table.getRowByKey(table.getRowKeys(rows.get(2))));
    assertNull(table.findParentRow(rows.get(0)));
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    assertNull(table.findParentRow(rows.get(2)));
  }

  @Test
  public void testKeyAndParentKeyWithMultipleKeyColumn() {
    P_MultiplePrimaryKeyColumnTable table = new P_MultiplePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{1, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{3, "Key3", 1, "Key1", null}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(3, "Key3"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2")
    }, rows);
    assertArrayEquals(new Object[]{null, null}, rows.get(0).getParentKeyValues().toArray());
    assertArrayEquals(rows.get(0).getKeyValues().toArray(), rows.get(1).getParentKeyValues().toArray());
    assertArrayEquals(new Object[]{null, null}, rows.get(2).getParentKeyValues().toArray());
    assertArrayEquals(new Object[]{null, null}, rows.get(3).getParentKeyValues().toArray());
    assertArrayEquals(new Object[]{null, null}, rows.get(4).getParentKeyValues().toArray());
    assertNull(table.findParentRow(rows.get(0)));
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    assertNull(table.findParentRow(rows.get(2)));
    assertNull(table.findParentRow(rows.get(3)));
    assertNull(table.findParentRow(rows.get(4)));
  }

  @Test
  public void testModifyParentsPrimaryKey() {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    rows.add(table.createRow(new Object[]{3, 1}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new Object[]{1, 3, 2}, rows);
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    // update primary key of parent row
    rows.get(1).getCellForUpdate(table.getParentKeyColumn()).setValue(2);
    rows = table.getRows();
    expectRowOrder(new Object[]{1, 2, 3}, rows);
    rows.get(0).getCellForUpdate(table.getPrimaryKeyColumn()).setValue(33);
    rows = table.getRows();
    expectRowOrder(new Object[]{33, 2, 3}, rows);
    assertEquals(rows.get(1), table.findParentRow(rows.get(2)));
    // cannot change the key of a row that is referenced by a child row
    try {
      rows.get(1).getCellForUpdate(table.getPrimaryKeyColumn()).setValue(44);
      fail("Expected exception");
    }
    catch (IllegalStateException e) {
      // ok
    }
  }

  @Test
  public void testModifyParentsPrimaryKeyWithMultipleKeyColumn() {
    P_MultiplePrimaryKeyColumnTable table = new P_MultiplePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{1, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{3, "Key3", 1, "Key1", null}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(3, "Key3"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2")
    }, rows);
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    // update primary key of parent row
    rows.get(1).getCellForUpdate(table.getParentKey2Column()).setValue("Key2");
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(3, "Key3"), // <--
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2")
    }, rows);
    rows.get(0).getCellForUpdate(table.getPrimaryKey2Column()).setValue("NewKey1");
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "NewKey1"), // <--
        new CompositeObject(1, "Key2"),
        new CompositeObject(3, "Key3"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2")
    }, rows);
    // cannot change the key of a row that is referenced by a child row
    try {
      rows.get(1).getCellForUpdate(table.getPrimaryKey2Column()).setValue("NewKey2");
      fail("Expected exception");
    }
    catch (IllegalStateException e) {
      // ok
    }
  }

  @Test
  public void testReParenting() {
    P_SinglePrimaryKeyColumnTable table = new P_SinglePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, null}));
    rows.add(table.createRow(new Object[]{2, null}));
    rows.add(table.createRow(new Object[]{3, 1}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new Object[]{1, 3, 2}, rows);
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    // update primary key of parent row
    rows.get(1).getCellForUpdate(table.getParentKeyColumn()).setValue(2);
    rows = table.getRows();
    expectRowOrder(new Object[]{1, 2, 3}, rows);
    assertEquals(rows.get(1), table.findParentRow(rows.get(2)));
  }

  @Test
  public void testReParentingWithMultipleKey() {
    P_MultiplePrimaryKeyColumnTable table = new P_MultiplePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{1, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{3, "Key3", 1, "Key1", null}));
    table.replaceRows(rows);
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(3, "Key3"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2")
    }, rows);
    assertEquals(rows.get(0), table.findParentRow(rows.get(1)));
    // update primary key of parent row
    rows.get(1).getCellForUpdate(table.getParentKey1Column()).setValue(2);
    table.updateAllRows();
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(3, "Key3"), // <--
        new CompositeObject(2, "Key2")
    }, rows);
    assertEquals(rows.get(2), table.findParentRow(rows.get(3)));
    rows.get(3).getCellForUpdate(table.getParentKey2Column()).setValue("Key2");
    rows = table.getRows();
    expectRowOrder(new CompositeObject[]{
        new CompositeObject(1, "Key1"),
        new CompositeObject(1, "Key2"),
        new CompositeObject(2, "Key1"),
        new CompositeObject(2, "Key2"),
        new CompositeObject(3, "Key3") // <--
    }, rows);
    assertEquals(rows.get(3), table.findParentRow(rows.get(4)));
  }

  @Test
  public void testUpdateNoneKeyCell() throws Exception {
    P_MultiplePrimaryKeyColumnTable table = new P_MultiplePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{1, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{3, "Key3", 1, "Key1", null}));
    table.replaceRows(rows);
    rows = table.getRows();

    TreeSet<CompositeObject> oldKeys = new TreeSet<>(getRowByKeyMap(table).keySet());
    rows.get(0).getCellForUpdate(table.getFirstColumn()).setValue("Some new text");
    assertSameContent(oldKeys, new TreeSet<>(getRowByKeyMap(table).keySet()));
  }

  @Test
  public void testUpdateKeyCell() throws Exception {
    P_MultiplePrimaryKeyColumnTable table = new P_MultiplePrimaryKeyColumnTable();
    table.init();
    List<ITableRow> rows = new ArrayList<>();
    rows.add(table.createRow(new Object[]{1, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{1, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key1", null, null, null}));
    rows.add(table.createRow(new Object[]{2, "Key2", null, null, null}));
    rows.add(table.createRow(new Object[]{3, "Key3", 1, "Key1", null}));
    table.replaceRows(rows);
    rows = table.getRows();

    TreeSet<CompositeObject> oldKeys = new TreeSet<>(getRowByKeyMap(table).keySet());
    rows.get(2).getCellForUpdate(table.getPrimaryKey1Column()).setValue(33);

    assertNotSameContent(oldKeys, new TreeSet<>(getRowByKeyMap(table).keySet()));
  }

  private void assertSameContent(Collection<?> a, Collection<?> b) {
    assertEquals(a.size(), b.size());
    Iterator<?> it1 = a.iterator();
    Iterator<?> it2 = b.iterator();
    while (it1.hasNext()) {
      assertSame(it1.next(), it2.next());
    }
  }

  private void assertNotSameContent(Collection<?> a, Collection<?> b) {
    assertEquals(a.size(), b.size());
    Iterator<?> it1 = a.iterator();
    Iterator<?> it2 = b.iterator();
    while (it1.hasNext()) {
      if (it1.next() != it2.next()) {
        return;
      }
    }
    fail("expected not to be same");
  }

  private Map<CompositeObject, ITableRow> getRowByKeyMap(AbstractTable table) throws Exception {
    Field f = AbstractTable.class.getDeclaredField("m_rowsByKey");
    f.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<CompositeObject, ITableRow> rows = (Map<CompositeObject, ITableRow>) f.get(table);
    return rows.entrySet().stream().collect(Collectors.<Entry<CompositeObject, ITableRow>, CompositeObject, ITableRow> toMap(e -> e.getKey(), e -> e.getValue()));
  }

  public static class P_SinglePrimaryKeyColumnTable extends AbstractTable {

    public ParentKeyColumn getParentKeyColumn() {
      return getColumnSet().getColumnByClass(ParentKeyColumn.class);
    }

    public PrimaryKeyColumn getPrimaryKeyColumn() {
      return getColumnSet().getColumnByClass(PrimaryKeyColumn.class);
    }

    @Order(100)
    @ClassId("b77c53e1-97e1-4fd0-9618-8119e85463a1")
    public class PrimaryKeyColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(200)
    @ClassId("a12f0fd4-7c1d-4e10-b186-6d21b5363fb8")
    public class ParentKeyColumn extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredParentKey() {
        return true;
      }
    }
  }

  public static class P_MultiplePrimaryKeyColumnTable extends AbstractTable {

    public PrimaryKey2Column getPrimaryKey2Column() {
      return getColumnSet().getColumnByClass(PrimaryKey2Column.class);
    }

    public ParentKey2Column getParentKey2Column() {
      return getColumnSet().getColumnByClass(ParentKey2Column.class);
    }

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public ParentKey1Column getParentKey1Column() {
      return getColumnSet().getColumnByClass(ParentKey1Column.class);
    }

    public PrimaryKey1Column getPrimaryKey1Column() {
      return getColumnSet().getColumnByClass(PrimaryKey1Column.class);
    }

    @Order(1000)
    @ClassId("0c31b54b-aca2-4f3e-9ce7-2509a35f0a30")
    public class PrimaryKey1Column extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(2000)
    @ClassId("8665eacb-6f6b-4fc1-b54e-37c85d9ee224")
    public class PrimaryKey2Column extends AbstractStringColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }
    }

    @Order(3000)
    @ClassId("53e63bca-5287-4a36-8116-d55f97c5221b")
    public class ParentKey1Column extends AbstractIntegerColumn {
      @Override
      protected boolean getConfiguredParentKey() {
        return true;
      }
    }

    @Order(4000)
    @ClassId("499eef40-b14d-4177-9a08-bf818b076475")
    public class ParentKey2Column extends AbstractStringColumn {
      @Override
      protected boolean getConfiguredParentKey() {
        return true;
      }
    }

    @Order(5000)
    @ClassId("a743972f-d0ab-472c-ad6f-e742c638f359")
    public class FirstColumn extends AbstractStringColumn {
    }
  }
}
