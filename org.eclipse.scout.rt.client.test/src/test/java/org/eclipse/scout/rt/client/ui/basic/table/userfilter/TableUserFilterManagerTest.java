/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.userfilter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.userfilter.IUserFilterState;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 6.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableUserFilterManagerTest {

  @Test
  public void setEmptyDataOnEmptyFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    final byte[] expectedData = createEmptyFilterManagerData();

    ufm.setSerializedData(expectedData);
    assertTrue(ufm.getFilters().isEmpty());
    assertArrayEquals(ufm.getSerializedData(), expectedData);
  }

  @Test
  public void setNameColumnDataOnEmptyFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    final byte[] expectedData = createNameColumnFilterManagerData();

    ufm.setSerializedData(expectedData);
    Collection<IUserFilterState> filters = ufm.getFilters();
    assertEquals(1, filters.size());
    assertArrayEquals(expectedData, ufm.getSerializedData());
  }

  @Test
  public void setKeyColumnDataOnEmptyFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    final byte[] expectedData = createKeyColumnFilterManagerData();

    ufm.setSerializedData(expectedData);
    Collection<IUserFilterState> filters = ufm.getFilters();
    assertEquals(1, filters.size());
    assertArrayEquals(expectedData, ufm.getSerializedData());
  }

  @Test
  public void setEmptyDataOnNameColumnFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    ufm.setSerializedData(createNameColumnFilterManagerData());

    final byte[] expectedData = createEmptyFilterManagerData();
    ufm.setSerializedData(expectedData);
    assertTrue(ufm.getFilters().isEmpty());
    assertArrayEquals(expectedData, ufm.getSerializedData());
  }

  @Test
  public void setKeyColumnDataOnNameColumnFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    ufm.setSerializedData(createNameColumnFilterManagerData());

    final byte[] expectedData = createKeyColumnFilterManagerData();
    ufm.setSerializedData(expectedData);
    assertEquals(1, ufm.getFilters().size());
    assertArrayEquals(expectedData, ufm.getSerializedData());
  }

  @Test
  public void setNameColumnDataOnNameColumnFilterManager() {
    TestTable table = createTable();
    TableUserFilterManager ufm = table.getUserFilterManager();
    ufm.setSerializedData(createNameColumnFilterManagerData());

    final byte[] expectedData = createNameColumnFilterManagerData();
    ufm.setSerializedData(expectedData);
    assertEquals(1, ufm.getFilters().size());
    assertArrayEquals(expectedData, ufm.getSerializedData());
  }

  protected TestTable createTable() {
    TestTable table = new TestTable();
    table.initTable();
    return table;
  }

  protected byte[] createEmptyFilterManagerData() {
    return createTable().getUserFilterManager().getSerializedData();
  }

  protected byte[] createNameColumnFilterManagerData() {
    TestTable table = createTable();
    TextColumnUserFilterState filter = new TextColumnUserFilterState(table.getNameColumn());
    filter.setSelectedValues(CollectionUtility.<Object> hashSet("first row", "third row"));
    TableUserFilterManager ufm = table.getUserFilterManager();
    ufm.addFilter(filter);
    return ufm.getSerializedData();
  }

  protected byte[] createKeyColumnFilterManagerData() {
    TestTable table = createTable();
    TextColumnUserFilterState filter = new TextColumnUserFilterState(table.getKeyColumn());
    filter.setSelectedValues(CollectionUtility.<Object> hashSet(1L, 3L));
    TableUserFilterManager ufm = table.getUserFilterManager();
    ufm.addFilter(filter);
    return ufm.getSerializedData();
  }

  static class TestTable extends AbstractTable {

    public NameColumn getNameColumn() {
      return getColumnSet().getColumnByClass(NameColumn.class);
    }

    public KeyColumn getKeyColumn() {
      return getColumnSet().getColumnByClass(KeyColumn.class);
    }

    @Order(1000)
    @ClassId("09a7e880-09f0-4849-835e-3ca8919c1a13")
    public class KeyColumn extends AbstractLongColumn {
    }

    @Order(2000)
    @ClassId("b29a3504-e3a2-42e6-9684-3e102567d2b8")
    public class NameColumn extends AbstractStringColumn {
    }
  }
}
