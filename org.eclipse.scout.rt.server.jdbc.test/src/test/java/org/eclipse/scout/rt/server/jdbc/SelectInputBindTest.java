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
package org.eclipse.scout.rt.server.jdbc;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.holders.ITableBeanHolder;
import org.eclipse.scout.rt.platform.holders.ITableBeanRowHolder;
import org.eclipse.scout.rt.platform.holders.ITableHolder;
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;
import org.eclipse.scout.rt.platform.holders.TableHolderFilter;
import org.eclipse.scout.rt.server.TestJdbcServerSession;
import org.eclipse.scout.rt.server.jdbc.fixture.ContainerBean;
import org.eclipse.scout.rt.server.jdbc.fixture.FormDataWithArray;
import org.eclipse.scout.rt.server.jdbc.fixture.FormDataWithSet;
import org.eclipse.scout.rt.server.jdbc.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.jdbc.fixture.TableFieldBeanData;
import org.eclipse.scout.rt.server.jdbc.fixture.TableFieldBeanData.TableFieldBeanDataRowData;
import org.eclipse.scout.rt.server.jdbc.fixture.TableFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link ISqlService} (using the mock {@link SqlServiceMock}). Different types of arrays used as input bind.
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestJdbcServerSession.class)
@RunWithSubject("default")
@SuppressWarnings("deprecation")
public class SelectInputBindTest {

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna). Direct batch update.
   */
  @Test
  public void testBatchUpdateFromTableFieldData() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(false);
    sql.update("UDPATE my_table SET a=:{active}, s=:{state} where n=:{name} ", tableData);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna). TableData for batch update is in
   * NVPair bind.
   */
  @Test
  public void testBatchUpdateFromTableFieldDataInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(false);
    sql.update("UDPATE my_table SET a=:{table.active}, s=:{table.state} where n=:{table.name} ", new NVPair("table", tableData));
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna). TableData for batch update is in
   * Map bind.
   */
  @Test
  public void testBatchUpdateFromTableFieldDataInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(false);
    Map<String, ?> map = Collections.singletonMap("table", tableData);
    sql.update("UDPATE my_table SET a=:{table.active}, s=:{table.state} where n=:{table.name} ", map);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna). TableData for batch update is in a
   * bean (ContainerBean).
   */
  @Test
  public void testBatchUpdateFromTableFieldDataInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(false);
    ContainerBean bean = new ContainerBean();
    bean.setTableFieldData(tableData);
    sql.update("UDPATE my_table SET a=:{tableFieldData.active}, s=:{tableFieldData.state} where n=:{tableFieldData.name} ", bean);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} in combination with {@link TableHolderFilter} (existing before Luna). Direct batch update.
   */
  @Test
  public void testBatchUpdateFromTableHolderFilter() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(true);
    TableHolderFilter filter = new TableHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    sql.update("UDPATE my_table SET a=:{active}, s=:{state} where n=:{name} ", filter);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} in combination with {@link TableHolderFilter} (existing before Luna). TableData for batch
   * update is in NVPair bind.
   */
  @Test
  public void testBatchUpdateFromTableHolderFilterInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(true);
    TableHolderFilter filter = new TableHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    sql.update("UDPATE my_table SET a=:{filter.active}, s=:{filter.state} where n=:{filter.name} ", new NVPair("filter", filter));
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} in combination with {@link TableHolderFilter} (existing before Luna). TableData for batch
   * update is in Map bind.
   */
  @Test
  public void testBatchUpdateFromTableHolderFilterInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(true);
    TableHolderFilter filter = new TableHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    Map<String, ?> map = Collections.singletonMap("filter", filter);
    sql.update("UDPATE my_table SET a=:{filter.active}, s=:{filter.state} where n=:{filter.name} ", map);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldData} in combination with {@link TableHolderFilter} (existing before Luna). TableData for batch
   * update is in a bean (ContainerBean).
   */
  @Test
  public void testBatchUpdateFromTableHolderFilterInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldData tableData = createTableFieldData(true);
    TableHolderFilter filter = new TableHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    ContainerBean bean = new ContainerBean();
    bean.setTableHolderFilter(filter);
    sql.update("UDPATE my_table SET a=:{TableHolderFilter.active}, s=:{TableHolderFilter.state} where n=:{TableHolderFilter.name} ", bean);
    assertExpectedProtocol(sql);
  }

  private TableFieldData createTableFieldData(boolean withAdditionalRows) {
    TableFieldData tableData = new TableFieldData();
    if (withAdditionalRows) {
      createRow(tableData, ITableBeanRowHolder.STATUS_INSERTED, false, 6, "xxx");
    }
    createRow(tableData, ITableBeanRowHolder.STATUS_UPDATED, true, 3, "lorem");
    if (withAdditionalRows) {
      createRow(tableData, ITableBeanRowHolder.STATUS_DELETED, false, 8, "yyy");
    }
    createRow(tableData, ITableBeanRowHolder.STATUS_UPDATED, false, 6, "ipsum");
    if (withAdditionalRows) {
      createRow(tableData, ITableBeanRowHolder.STATUS_INSERTED, true, 2, "zzz");
    }
    return tableData;
  }

  private void createRow(TableFieldData tableData, int rowStatus, Boolean active, Integer state, String name) {
    int row;
    row = tableData.addRow(rowStatus);
    tableData.setActive(row, active);
    tableData.setState(row, state);
    tableData.setName(row, name);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna). Direct batch update.
   */
  @Test
  public void testBatchUpdateFromTableFieldBeanData() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(false);
    sql.update("UDPATE my_table SET a=:{active}, s=:{state} where n=:{name} ", tableData);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna). TableData for batch update
   * is in NVPair bind.
   */
  @Test
  public void testBatchUpdateFromTableFieldBeanDataInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(false);
    sql.update("UDPATE my_table SET a=:{table.active}, s=:{table.state} where n=:{table.name} ", new NVPair("table", tableData));
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna). TableData for batch update
   * is in Map bind.
   */
  @Test
  public void testBatchUpdateFromTableFieldBeanDataInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(false);
    Map<String, ?> map = Collections.singletonMap("table", tableData);
    sql.update("UDPATE my_table SET a=:{table.active}, s=:{table.state} where n=:{table.name} ", map);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna). TableData for batch update
   * is in a bean (ContainerBean).
   */
  @Test
  public void testBatchUpdateFromTableFieldBeanDataInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(false);
    ContainerBean bean = new ContainerBean();
    bean.setTableFieldBeanData(tableData);
    sql.update("UDPATE my_table SET a=:{tableFieldBeanData.active}, s=:{tableFieldBeanData.state} where n=:{tableFieldBeanData.name} ", bean);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} in combination with {@link TableBeanHolderFilter} (introduced with Luna). Direct batch
   * update.
   */
  @Test
  public void testBatchUpdateFromTableBeanHolderFilter() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(true);
    TableBeanHolderFilter filter = new TableBeanHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    sql.update("UDPATE my_table SET a=:{active}, s=:{state} where n=:{name} ", filter);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} in combination with {@link TableBeanHolderFilter} (introduced with Luna). TableData for
   * batch update is in NVPair bind.
   */
  @Test
  public void testBatchUpdateFromTableBeanHolderFilterInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(true);
    TableBeanHolderFilter filter = new TableBeanHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    sql.update("UDPATE my_table SET a=:{filter.active}, s=:{filter.state} where n=:{filter.name} ", new NVPair("filter", filter));
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} in combination with {@link TableBeanHolderFilter} (introduced with Luna). TableData for
   * batch update is in Map bind.
   */
  @Test
  public void testBatchUpdateFromTableBeanHolderFilterInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(true);
    TableBeanHolderFilter filter = new TableBeanHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    Map<String, ?> map = Collections.singletonMap("filter", filter);
    sql.update("UDPATE my_table SET a=:{filter.active}, s=:{filter.state} where n=:{filter.name} ", map);
    assertExpectedProtocol(sql);
  }

  /**
   * {@link TableFieldBeanData} in combination with {@link TableBeanHolderFilter} (introduced with Luna). TableData for
   * batch update is in a bean (ContainerBean).
   */
  @Test
  public void testBatchUpdateFromTableBeanHolderFilterInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    TableFieldBeanData tableData = createTableFieldBeanData(true);
    TableBeanHolderFilter filter = new TableBeanHolderFilter(tableData, ITableBeanRowHolder.STATUS_UPDATED);
    ContainerBean bean = new ContainerBean();
    bean.setTableBeanHolderFilter(filter);
    sql.update("UDPATE my_table SET a=:{TableBeanHolderFilter.active}, s=:{TableBeanHolderFilter.state} where n=:{TableBeanHolderFilter.name} ", bean);
    assertExpectedProtocol(sql);
  }

  private TableFieldBeanData createTableFieldBeanData(boolean withAdditionalRows) {
    TableFieldBeanData tableBeanData = new TableFieldBeanData();
    if (withAdditionalRows) {
      createRow(tableBeanData, ITableBeanRowHolder.STATUS_INSERTED, false, 6, "xxx");
    }
    createRow(tableBeanData, ITableBeanRowHolder.STATUS_UPDATED, true, 3, "lorem");
    if (withAdditionalRows) {
      createRow(tableBeanData, ITableBeanRowHolder.STATUS_DELETED, false, 8, "yyy");
    }
    createRow(tableBeanData, ITableBeanRowHolder.STATUS_UPDATED, false, 6, "ipsum");
    if (withAdditionalRows) {
      createRow(tableBeanData, ITableBeanRowHolder.STATUS_INSERTED, true, 2, "zzz");
    }
    return tableBeanData;
  }

  private void createRow(TableFieldBeanData tableBeanData, int rowStatus, Boolean active, Integer state, String name) {
    TableFieldBeanDataRowData row = tableBeanData.addRow(rowStatus);
    row.setActive(active);
    row.setState(state);
    row.setName(name);
  }

  private static SqlServiceMock createSqlServiceMock() {
    SqlServiceMock sql = new SqlServiceMock();
    sql.clearProtocol();
    return sql;
  }

  private static final String EXPECTED_PROTOCOL = "Connection.prepareStatement(UDPATE my_table SET a = ?, s = ? where n = ?)\n"
      + "PreparedStatement.setObject(1, 1, 4)\n"
      + "PreparedStatement.setObject(2, 3, 4)\n"
      + "PreparedStatement.setObject(3, lorem, 12)\n"
      + "Connection.prepareStatement(UDPATE my_table SET a = ?, s = ? where n = ?)\n"
      + "PreparedStatement.setObject(1, 0, 4)\n"
      + "PreparedStatement.setObject(2, 6, 4)\n"
      + "PreparedStatement.setObject(3, ipsum, 12)\n";

  private static void assertExpectedProtocol(SqlServiceMock sql) {
    assertEquals(EXPECTED_PROTOCOL, sql.getProtocol().toString());
  }

  /**
   * Batch update from an array.
   */
  @Test
  public void testBatchUpdateFromArray() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    Long[] roles = new Long[]{5L, 6L};
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", roles), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * This test is similar to {@link #testBatchUpdateFromArray()}. It ensure that
   * {@link #assertExpectedProtocol2(SqlServiceMock)} does not care about the order of the elements in the roles array
   * (because in a set you can not ensure the order of the entries in the set)
   */
  @Test
  public void testBatchUpdateFromArray2() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    Long[] roles = new Long[]{6L, 5L};
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", roles), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Batch update from a list.
   */
  @Test
  public void testBatchUpdateFromList() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    List<Long> roles = Arrays.asList(5L, 6L);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", roles), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Direct batch update from a set.
   */
  @Test
  public void testBatchUpdateFromSet() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    Set<Long> roles = new HashSet<Long>();
    roles.add(5L);
    roles.add(6L);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", roles), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Batch update from a list in an holder.
   */
  @Test
  public void testBatchUpdateFromListInHolder() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    List<Long> roles = Arrays.asList(5L, 6L);
    Holder<List> holder = new Holder<List>(List.class, roles);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", holder), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Direct batch update from a array in {@link AbstractValueFieldData}.
   */
  @Test
  public void testBatchUpdateFromArrayInValueField() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    FormDataWithArray formData = new FormDataWithArray();
    formData.getPersonNr().setValue(9L);
    formData.getValue().setValue("lorem");
    formData.getRoles().setValue(new Long[]{5L, 6L});
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", formData);
    assertExpectedProtocol2(sql);
  }

  /**
   * Direct batch update from a array in {@link AbstractValueFieldData}.
   */
  @Test
  public void testBatchUpdateFromSetInValueField() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    Set<Long> roles = new HashSet<Long>();
    roles.add(5L);
    roles.add(6L);

    FormDataWithSet formData = new FormDataWithSet();
    formData.getPersonNr().setValue(9L);
    formData.getValue().setValue("lorem");
    formData.getRoles().setValue(roles);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", formData);
    assertExpectedProtocol2(sql);
  }

  /**
   * Direct batch update from a array in {@link AbstractValueFieldData}.
   */
  @Test
  public void testEmptyBatchUpdateFromSetInValueField() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();

    FormDataWithSet formData = new FormDataWithSet();
    formData.getPersonNr().setValue(9L);
    formData.getValue().setValue("lorem");
    formData.getRoles().setValue(Collections.<Long> emptySet());
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", formData);
    assertEquals("", sql.getProtocol().toString());

  }

  private static final String PREPARE_STATEMENT = "Connection.prepareStatement(UDPATE this_table SET v = ? where r = ? and p = ?)\n";
  private static final String OBJECTS_RECORD_1 = "PreparedStatement.setObject(1, lorem, 12)\n"
      + "PreparedStatement.setObject(2, 5, -5)\n"
      + "PreparedStatement.setObject(3, 9, -5)\n";
  private static final String OBJECTS_RECORD_2 = "PreparedStatement.setObject(1, lorem, 12)\n"
      + "PreparedStatement.setObject(2, 6, -5)\n"
      + "PreparedStatement.setObject(3, 9, -5)\n";

  private static final String EXPECTED_PROTOCOL_2_V1 = PREPARE_STATEMENT + OBJECTS_RECORD_1 + PREPARE_STATEMENT + OBJECTS_RECORD_2;
  private static final String EXPECTED_PROTOCOL_2_V2 = PREPARE_STATEMENT + OBJECTS_RECORD_2 + PREPARE_STATEMENT + OBJECTS_RECORD_1;

  private static void assertExpectedProtocol2(SqlServiceMock sql) {
    String actual = sql.getProtocol().toString();
    if (actual.startsWith(PREPARE_STATEMENT + OBJECTS_RECORD_1)) {
      assertEquals(EXPECTED_PROTOCOL_2_V1, actual);
    }
    else {
      assertEquals(EXPECTED_PROTOCOL_2_V2, actual);
    }
  }
}
