/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.junit.Test;

/**
 * Tests for {@link AbstractBeanColumn}
 */
public class AbstractBeanColumnTest {

  private TestTable initTestTable() {
    TestTable table = new TestTable();
    table.addRowByArray(new Object[]{new Bean("b testLine", "ab testLine"), new ComparableBean("b testLine", "ab testLine")});
    table.addRowByArray(new Object[]{new Bean("a testLine", "ba testLine"), new ComparableBean("a testLine", "ba testLine")});
    return table;
  }

  @Test
  public void testSortBeanColumn() {
    TestTable table = initTestTable();

    List<ITableRow> rows = table.getRows();
    int compare = table.getTestBeanColumn().compareTableRows(rows.get(0), rows.get(1));
    assertTrue(compare == 1);

    table.getTestBeanColumn().setInitialAlwaysIncludeSortAtBegin(true);
    table.getTestBeanColumn().setInitialSortIndex(0);
    table.getColumnSet().resetSortingAndGrouping();
    table.sort();

    Bean firstBean = table.getTestBeanColumn().getValue(0);
    assertTrue(StringUtility.equalsIgnoreCase(firstBean.m_line1, "a testLine"));
  }

  @Test
  public void testSortComparableBeanColumn() {
    TestTable table = initTestTable();

    List<ITableRow> rows = table.getRows();
    int compare = table.getTestComparableBeanColumn().compareTableRows(rows.get(0), rows.get(1));
    assertTrue(compare == -1);

    table.getTestComparableBeanColumn().setInitialAlwaysIncludeSortAtBegin(true);
    table.getTestComparableBeanColumn().setInitialSortIndex(0);
    table.getColumnSet().resetSortingAndGrouping();
    table.sort();
    ComparableBean firstComparableBean = table.getTestComparableBeanColumn().getValue(0);
    assertTrue(StringUtility.equalsIgnoreCase(firstComparableBean.m_line1, "b testLine"));
  }

  public class TestTable extends AbstractTable {

    public TestBeanColumn getTestBeanColumn() {
      return getColumnSet().getColumnByClass(TestBeanColumn.class);
    }

    public TestComparableBeanColumn getTestComparableBeanColumn() {
      return getColumnSet().getColumnByClass(TestComparableBeanColumn.class);
    }

    @Order(10)
    public class TestBeanColumn extends AbstractBeanColumn<Bean> {
      @Override
      protected String getPlainText(ITableRow row) {
        Bean bean = getValue(row);
        return StringUtility.join(" ", bean.getLine1(), bean.getLine2());
      }
    }

    @Order(20)
    public class TestComparableBeanColumn extends AbstractBeanColumn<ComparableBean> {
      @Override
      protected String getPlainText(ITableRow row) {
        ComparableBean bean = getValue(row);
        return StringUtility.join(" ", bean.getLine1(), bean.getLine2());
      }
    }
  }

  public static class Bean {
    private String m_line1;
    private String m_line2;

    public Bean(String line1, String line2) {
      m_line1 = line1;
      m_line2 = line2;
    }

    public String getLine1() {
      return m_line1;
    }

    public void setLine1(String line1) {
      m_line1 = line1;
    }

    public String getLine2() {
      return m_line2;
    }

    public void setLine2(String line2) {
      m_line2 = line2;
    }
  }

  public static class ComparableBean implements Comparable<ComparableBean> {
    private String m_line1;
    private String m_line2;

    public ComparableBean(String line1, String line2) {
      m_line1 = line1;
      m_line2 = line2;
    }

    public String getLine1() {
      return m_line1;
    }

    public void setLine1(String line1) {
      m_line1 = line1;
    }

    public String getLine2() {
      return m_line2;
    }

    public void setLine2(String line2) {
      m_line2 = line2;
    }

    @Override
    public int compareTo(ComparableBean o) {
      int c = StringUtility.compareIgnoreCase(getLine2(), o.getLine2());
      if (c == 0) {
        c = StringUtility.compareIgnoreCase(getLine1(), o.getLine1());
      }
      return c;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_line1 == null) ? 0 : m_line1.hashCode());
      result = prime * result + ((m_line2 == null) ? 0 : m_line2.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      ComparableBean other = (ComparableBean) obj;
      return StringUtility.equalsIgnoreCase(m_line1, other.m_line1) && StringUtility.equalsIgnoreCase(m_line2, other.m_line2);
    }
  }
}
