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
package org.eclipse.scout.commons.holders;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.commons.holders.fixture.SqlServiceMock;
import org.eclipse.scout.rt.server.services.common.jdbc.style.OracleSqlStyle;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.junit.Test;

/**
 * Test bean array and form data array binds, requiring a server for database stuff
 */
public class SelectIntoArrayTest {

  @Test
  public void testSelect() throws Exception {
    SqlServiceMock sql = new SqlServiceMock();
    sql.setSqlStyle(new OracleSqlStyle());
    Object[][] expectedData = new Object[][]{
        new Object[]{true, 1, "abc"},
        new Object[]{null, 1, "abc"},
        new Object[]{true, null, "abc"},
        new Object[]{true, 1, null},
    };
    sql.setResultData(expectedData);
    //
    Object[][] data = sql.select("SELECT A,B,C FROM T WHERE D=0");
    assertNotNull(data);
    assertEquals(4, data.length);
    for (int i = 0; i < data.length; i++) {
      assertArrayEquals(expectedData[i], data[i]);
    }
  }

  @Test
  public void testSelectIntoBeanArray() throws Exception {
    SqlServiceMock sql = new SqlServiceMock();
    sql.setSqlStyle(new OracleSqlStyle());
    Object[][] expectedData = new Object[][]{
        new Object[]{true, 1, "abc"},
        new Object[]{null, 1, "abc"},
        new Object[]{true, null, "abc"},
        new Object[]{true, 1, null},
    };
    sql.setResultData(expectedData);
    //
    BeanArrayHolder<MyBean> h = new BeanArrayHolder<SelectIntoArrayTest.MyBean>(MyBean.class);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{h.active},:{h.state},:{h.name}", new NVPair("h", h));
    MyBean[] a = h.getBeans();
    assertNotNull(a);
    assertEquals(4, a.length);
    for (int i = 0; i < a.length; i++) {
      a[i].assertValues(expectedData[i]);
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
    SqlServiceMock sql = new SqlServiceMock();
    sql.setSqlStyle(new OracleSqlStyle());
    Object[][] expectedData = new Object[][]{
        new Object[]{true, 1, "abc"},
        new Object[]{null, 1, "abc"},
        new Object[]{true, null, "abc"},
        new Object[]{true, 1, null},
    };
    sql.setResultData(expectedData);
    //
    BeanArrayHolder<MyFormData> h = new BeanArrayHolder<SelectIntoArrayTest.MyFormData>(MyFormData.class);
    sql.selectInto("SELECT A,B,C FROM T WHERE D=0 INTO :{h.active},:{h.state},:{h.name}", new NVPair("h", h));
    MyFormData[] a = h.getBeans();
    assertNotNull(a);
    assertEquals(4, a.length);
    for (int i = 0; i < a.length; i++) {
      a[i].assertValues(expectedData[i]);
    }
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
