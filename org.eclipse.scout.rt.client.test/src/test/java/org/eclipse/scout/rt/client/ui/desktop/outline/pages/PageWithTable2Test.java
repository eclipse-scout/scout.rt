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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.PageWithTable2Test.SimpleTablePage.Table;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.PageWithTable2Test.SimpleTablePageData.SimpleTablePageRowData;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractPageWithTable}: importPageData, status Bug 419138
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithTable2Test {

  private static final Integer I1 = 5;
  private static final Integer I2 = 8;
  private static final Integer I3 = 1;
  private static final String S1 = "Lorem";
  private static final String S2 = "Ipsum";
  private static final String S3 = "Dolor";

  @Test
  public void testImportFormData1() throws Exception {
    Object[][] data = new Object[][]{new Object[]{5, S1}};
    SimpleTablePage page = new SimpleTablePage(false, data);

    Table table = page.getTable();
    assertEquals("row count", 1, table.getRowCount());
    assertEquals("IColumn value", I1, table.getIColumn().getValue(0));
    assertEquals("SColumn value", S1, table.getSColumn().getValue(0));
    assertEquals("Status", null, page.getTableStatus());
  }

  /**
   * Test with LimitedResult == true
   */
  @Test
  public void testImportFormData2() throws Exception {
    Object[][] data = new Object[][]{new Object[]{I1, S1}, new Object[]{I2, S2}, new Object[]{I3, S3}};
    SimpleTablePage page = new SimpleTablePage(true, data);

    Table table = page.getTable();
    assertEquals("row count", 3, table.getRowCount());
    assertEquals("Status - severity", IStatus.WARNING, page.getTableStatus().getSeverity());
    assertEquals("Status - message", TEXTS.get("MaxOutlineRowWarning", "3"), page.getTableStatus().getMessage());
  }

  public class SimpleTablePage extends AbstractPageWithTable<SimpleTablePage.Table> {

    private final SimpleTablePageData m_pageData;

    public SimpleTablePage(boolean limited, Object[][] data) {
      super(false);
      m_pageData = new SimpleTablePageData();
      for (Object[] objects : data) {
        SimpleTablePageRowData row = m_pageData.addRow();
        row.setI(TypeCastUtility.castValue(objects[0], Integer.class));
        row.setS(TypeCastUtility.castValue(objects[1], String.class));
      }
      m_pageData.setLimitedResult(limited);
      callInitializer();
      loadChildren();
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importPageData(m_pageData);
    }

    public class Table extends AbstractTable {

      public SColumn getSColumn() {
        return getColumnSet().getColumnByClass(SColumn.class);
      }

      public IColumn getIColumn() {
        return getColumnSet().getColumnByClass(IColumn.class);
      }

      @Order(10)
      public class IColumn extends AbstractIntegerColumn {
      }

      @Order(20)
      public class SColumn extends AbstractStringColumn {
      }
    }
  }

  public class SimpleTablePageData extends AbstractTablePageData {

    private static final long serialVersionUID = 1L;

    public SimpleTablePageData() {
    }

    @Override
    public SimpleTablePageRowData addRow() {
      return (SimpleTablePageRowData) super.addRow();
    }

    @Override
    public SimpleTablePageRowData addRow(int rowState) {
      return (SimpleTablePageRowData) super.addRow(rowState);
    }

    @Override
    public SimpleTablePageRowData createRow() {
      return new SimpleTablePageRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return SimpleTablePageRowData.class;
    }

    @Override
    public SimpleTablePageRowData[] getRows() {
      return (SimpleTablePageRowData[]) super.getRows();
    }

    @Override
    public SimpleTablePageRowData rowAt(int index) {
      return (SimpleTablePageRowData) super.rowAt(index);
    }

    public void setRows(SimpleTablePageRowData[] rows) {
      super.setRows(rows);
    }

    public class SimpleTablePageRowData extends AbstractTableRowData {

      private static final long serialVersionUID = 1L;
      public static final String i = "i";
      public static final String s = "s";
      private Integer m_i;
      private String m_s;

      public SimpleTablePageRowData() {
      }

      public Integer getI() {
        return m_i;
      }

      public void setI(Integer i) {
        m_i = i;
      }

      public String getS() {
        return m_s;
      }

      public void setS(String s) {
        m_s = s;
      }
    }
  }
}
