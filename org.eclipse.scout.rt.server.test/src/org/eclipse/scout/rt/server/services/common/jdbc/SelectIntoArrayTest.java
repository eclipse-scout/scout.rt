/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.jdbc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Map;

import org.eclipse.scout.commons.holders.BeanArrayHolder;
import org.eclipse.scout.commons.holders.ITableBeanHolder;
import org.eclipse.scout.commons.holders.ITableHolder;
import org.eclipse.scout.commons.holders.NVPair;
import org.eclipse.scout.rt.server.services.common.jdbc.fixture.ContainerBean;
import org.eclipse.scout.rt.server.services.common.jdbc.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.services.common.jdbc.fixture.TableFieldBeanData;
import org.eclipse.scout.rt.server.services.common.jdbc.fixture.TableFieldBeanData.TableFieldBeanDataRowData;
import org.eclipse.scout.rt.server.services.common.jdbc.fixture.TableFieldData;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.junit.Test;

/**
 * Test for {@link ISqlService} (using the mock {@link SqlServiceMock}).
 * Methods under test:
 * - {@link ISqlService#select(String, Object...)}.
 * - {@link ISqlService#selectInto(String, Object...)}.
 * With different types of arrays used as output bind.
 */
public class SelectIntoArrayTest {

  private static final Object[][] DATA = new Object[][]{
      new Object[]{true, 1, "abc"},
      new Object[]{null, 1, "abc"},
      new Object[]{true, null, "abc"},
      new Object[]{true, 1, null},
  };

  @Test
  public void testSelect() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    Object[][] data = sql.select("SELECT A,B,C FROM T WHERE D=0");
    Object[][] expectedData = DATA;
    assertNotNull(data);
    assertEquals(4, data.length);
    for (int i = 0; i < data.length; i++) {
      assertArrayEquals(expectedData[i], data[i]);
    }
  }

  @Test
  public void testSelectIntoBeanArray() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    BeanArrayHolder<MyBean> h = new BeanArrayHolder<SelectIntoArrayTest.MyBean>(MyBean.class);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{h.active},:{h.state},:{h.name}", new NVPair("h", h));
    MyBean[] a = h.getBeans();
    assertNotNull(a);
    assertEquals(4, a.length);
    for (int i = 0; i < a.length; i++) {
      a[i].assertValues(DATA[i]);
    }
  }

  @Test
  public void testSelectFromFormDataArray() throws Exception {
    SqlServiceMock sql = new SqlServiceMock();
    sql.setSqlStyle(new OracleSqlStyle());
    Object[][] expectedData = new Object[][]{
        new Object[]{true, 1, "abc"},
    };
    sql.setResultData(expectedData);
    //
    MyFormData f1 = new MyFormData();
    f1.getActive().setValue(true);
    f1.getState().setValue(1);
    f1.getName().setValue("abc");
    MyFormData f2 = new MyFormData();
    f2.getActive().setValue(null);
    f2.getState().setValue(null);
    f2.getName().setValue(null);
    //
    MyFormData[] h = new MyFormData[]{f1, f2};
    Object[][] data = sql.select("SELECT A,B,C FROM T WHERE A=:{h.active} AND B=:{h.state} AND C=:{h.name}", new NVPair("h", h));
    assertNotNull(data);
    assertEquals(2, data.length);
    assertArrayEquals(new Object[]{true, 1, "abc"}, data[0]);
    assertArrayEquals(new Object[]{true, 1, "abc"}, data[1]);
  }

  @Test
  public void testSelectIntoFormDataArray() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    Object[][] expectedData = DATA;
    BeanArrayHolder<MyFormData> h = new BeanArrayHolder<SelectIntoArrayTest.MyFormData>(MyFormData.class);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{h.active},:{h.state},:{h.name}", new NVPair("h", h));
    MyFormData[] a = h.getBeans();
    assertNotNull(a);
    assertEquals(4, a.length);
    for (int i = 0; i < a.length; i++) {
      a[i].assertValues(expectedData[i]);
    }
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna).
   * Direct select.
   */
  @Test
  public void testSelectIntoTableFieldData() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldData tableData = new TableFieldData();
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :active,:state,:name", tableData);
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna).
   * TableData is in NVPair bind.
   */
  @Test
  public void testSelectIntoTableFieldDataInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldData tableData = new TableFieldData();
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{table.active},:{table.state},:{table.name}", new NVPair("table", tableData));
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna).
   * TableData is in Map bind.
   */
  @Test
  public void testSelectIntoTableFieldDataInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldData tableData = new TableFieldData();
    Map<String, ?> map = Collections.singletonMap("table", tableData);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{table.active},:{table.state},:{table.name}", map);
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldData} is from type {@link ITableHolder} (existing before Luna).
   * TableData is in a bean (ContainerBean).
   */
  @Test
  public void testSelectIntoTableFieldDataInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    ContainerBean bean = new ContainerBean();
    bean.setTableFieldData(new TableFieldData());
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{tableFieldData.active},:{tableFieldData.state},:{tableFieldData.name}", bean);
    assertContainsData(bean.getTableFieldData());
  }

  private static void assertContainsData(TableFieldData tableData) {
    assertNotNull(tableData);
    assertEquals(4, tableData.getRowCount());
    for (int i = 0; i < tableData.getRowCount(); i++) {
      assertEquals("Active i=" + i, DATA[i][0], tableData.getActive(i));
      assertEquals("State i=" + i, DATA[i][1], tableData.getState(i));
      assertEquals("Name i=" + i, DATA[i][2], tableData.getName(i));
    }
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna).
   * Direct select.
   */
  @Test
  public void testSelectIntoTableFieldBeanData() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldBeanData tableData = new TableFieldBeanData();
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :active,:state,:name", tableData);
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna).
   * TableData is in NVPair bind.
   */
  @Test
  public void testSelectIntoTableFieldBeanDataInNVPair() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldBeanData tableData = new TableFieldBeanData();
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{table.active},:{table.state},:{table.name}", new NVPair("table", tableData));
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna).
   * TableData is in Map bind.
   */
  @Test
  public void testSelectIntoTableFieldBeanDataInMap() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    TableFieldBeanData tableData = new TableFieldBeanData();
    Map<String, ?> map = Collections.singletonMap("table", tableData);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{table.active},:{table.state},:{table.name}", map);
    assertContainsData(tableData);
  }

  /**
   * {@link TableFieldBeanData} is from type {@link ITableBeanHolder} (introduced with Luna).
   * TableData is in a bean (ContainerBean).
   */
  @Test
  public void testSelectIntoTableFieldBeanDataInBean() throws Exception {
    SqlServiceMock sql = createSqlServiceMock();
    //
    ContainerBean bean = new ContainerBean();
    bean.setTableFieldBeanData(new TableFieldBeanData());
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{tableFieldBeanData.active},:{tableFieldBeanData.state},:{tableFieldBeanData.name}", bean);
    assertContainsData(bean.getTableFieldBeanData());
  }

  private static void assertContainsData(TableFieldBeanData tableData) {
    assertNotNull(tableData);
    assertEquals(4, tableData.getRowCount());
    int i = 0;
    for (TableFieldBeanDataRowData row : tableData.getRows()) {
      assertEquals("Active i=" + i, DATA[i][0], row.getActive());
      assertEquals("State i=" + i, DATA[i][1], row.getState());
      assertEquals("Name i=" + i, DATA[i][2], row.getName());
      i++;
    }
  }

  private SqlServiceMock createSqlServiceMock() {
    SqlServiceMock sql = new SqlServiceMock();
    sql.setSqlStyle(new OracleSqlStyle());
    sql.setResultData(DATA);
    return sql;
  }

  public static class MyBean {
    private boolean m_active;
    private int m_state;
    private String m_name;

    public boolean isActive() {
      return m_active;
    }

    public void setActive(boolean active) {
      m_active = active;
    }

    public int getState() {
      return m_state;
    }

    public void setState(int state) {
      m_state = state;
    }

    public String getName() {
      return m_name;
    }

    public void setName(String name) {
      m_name = name;
    }

    void assertValues(Object[] row) {
      String expected = "" + (row[0] != null ? row[0] : "false") + "," + (row[1] != null ? row[1] : "0") + "," + (row[2] != null ? row[2] : "null");
      String actual = "" + m_active + "," + m_state + "," + m_name;
      assertEquals(expected, actual);
    }
  }

  public static class MyFormData extends AbstractFormData {
    private static final long serialVersionUID = 1L;

    public Active getActive() {
      return getFieldByClass(Active.class);
    }

    public State getState() {
      return getFieldByClass(State.class);
    }

    public Name getName() {
      return getFieldByClass(Name.class);
    }

    public static class Active extends AbstractValueFieldData<Boolean> {
      private static final long serialVersionUID = 1L;
    }

    public static class State extends AbstractValueFieldData<Integer> {
      private static final long serialVersionUID = 1L;
    }

    public static class Name extends AbstractValueFieldData<String> {
      private static final long serialVersionUID = 1L;
    }

    void assertValues(Object[] row) {
      String expected = "" + row[0] + "," + row[1] + "," + row[2];
      String actual = "" + getActive().getValue() + "," + getState().getValue() + "," + getName().getValue();
      assertEquals(expected, actual);
    }
  }
}
