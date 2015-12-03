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
package org.eclipse.scout.rt.shared.data.form.fields.tablefield;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.3
 */
@SuppressWarnings("unused")
@RunWith(PlatformTestRunner.class)
public class TableFieldBeanDataTest {

  private TestingTableData m_table;
  private TestingTableData.TestingTableRowData m_row;

  @Before
  public void before() {
    m_table = new TestingTableData();
    m_row = m_table.addRow();
    m_row.setStreet("street");
    m_row.setCity("city");
  }

  @Test
  public void testRemoveByIndex() {
    m_table.removeRow(0);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testRemoveByIndexTwice() {
    m_table.removeRow(0);
    m_table.removeRow(0);
  }

  @Test
  public void testRemoveByRow() {
    assertTrue(m_table.removeRow(m_row));
    assertFalse(m_table.removeRow(m_row));
  }

  private static class TestingTableData extends AbstractTableFieldBeanData {
    private static final long serialVersionUID = 1L;

    public TestingTableData() {
    }

    @Override
    public TestingTableRowData[] getRows() {
      return (TestingTableRowData[]) super.getRows();
    }

    public void setRows(TestingTableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public TestingTableRowData addRow() {
      return (TestingTableRowData) super.addRow();
    }

    @Override
    public TestingTableRowData addRow(int rowState) {
      return (TestingTableRowData) super.addRow(rowState);
    }

    @Override
    public TestingTableRowData rowAt(int idx) {
      return (TestingTableRowData) super.rowAt(idx);
    }

    @Override
    public TestingTableRowData createRow() {
      return new TestingTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return TestingTableRowData.class;
    }

    public static class TestingTableRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public TestingTableRowData() {
      }

      public static final String PROP_STREET = "street";
      public static final String PROP_CITY = "city";
      private String m_street;
      private String m_city;

      public String getStreet() {
        return m_street;
      }

      public void setStreet(String street) {
        m_street = street;
      }

      public String getCity() {
        return m_city;
      }

      public void setCity(String city) {
        m_city = city;
      }
    }
  }
}
