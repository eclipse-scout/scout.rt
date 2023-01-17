/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
import org.eclipse.scout.rt.platform.holders.NVPair;
import org.eclipse.scout.rt.platform.holders.TableBeanHolderFilter;
import org.eclipse.scout.rt.server.TestJdbcServerSession;
import org.eclipse.scout.rt.server.jdbc.fixture.ContainerBean;
import org.eclipse.scout.rt.server.jdbc.fixture.FormDataWithArray;
import org.eclipse.scout.rt.server.jdbc.fixture.FormDataWithSet;
import org.eclipse.scout.rt.server.jdbc.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.jdbc.fixture.TableFieldBeanData;
import org.eclipse.scout.rt.server.jdbc.fixture.TableFieldBeanData.TableFieldBeanDataRowData;
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
public class SelectInputBindTest {

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna). Direct batch update.
   */
  @Test
  public void testBatchUpdateFromTableFieldBeanData() {
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
  public void testBatchUpdateFromTableFieldBeanDataInNVPair() {
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
  public void testBatchUpdateFromTableFieldBeanDataInMap() {
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
  public void testBatchUpdateFromTableFieldBeanDataInBean() {
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
  public void testBatchUpdateFromTableBeanHolderFilter() {
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
  public void testBatchUpdateFromTableBeanHolderFilterInNVPair() {
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
  public void testBatchUpdateFromTableBeanHolderFilterInMap() {
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
  public void testBatchUpdateFromTableBeanHolderFilterInBean() {
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
  public void testBatchUpdateFromArray() {
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
  public void testBatchUpdateFromArray2() {
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
  public void testBatchUpdateFromList() {
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
  public void testBatchUpdateFromSet() {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    Set<Long> roles = new HashSet<>();
    roles.add(5L);
    roles.add(6L);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", roles), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Batch update from a list in an holder.
   */
  @Test
  public void testBatchUpdateFromListInHolder() {
    SqlServiceMock sql = createSqlServiceMock();

    Long person = 9L;
    List<Long> roles = Arrays.asList(5L, 6L);
    Holder<List> holder = new Holder<>(List.class, roles);
    sql.update("UDPATE this_table SET v = :value where r = :{roles} and p = :personNr", new NVPair("personNr", person), new NVPair("roles", holder), new NVPair("value", "lorem"));
    assertExpectedProtocol2(sql);
  }

  /**
   * Direct batch update from a array in {@link AbstractValueFieldData}.
   */
  @Test
  public void testBatchUpdateFromArrayInValueField() {
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
  public void testBatchUpdateFromSetInValueField() {
    SqlServiceMock sql = createSqlServiceMock();

    Set<Long> roles = new HashSet<>();
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
  public void testEmptyBatchUpdateFromSetInValueField() {
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
