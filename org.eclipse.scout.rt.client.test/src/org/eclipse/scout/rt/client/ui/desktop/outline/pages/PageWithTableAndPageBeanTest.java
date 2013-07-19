/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.PageWithTableAndPageBeanTest.PageWithTableData.PageWithTableRowData;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.page.AbstractTablePageData;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.10.0-M1
 */
@RunWith(ScoutClientTestRunner.class)
public class PageWithTableAndPageBeanTest {

  private static final String SECOND_COL_CONTENT = "second col";
  private static final String FIRST_COL_CONTENT = "first col";

  @Test
  public void testExecLoadDataInvokesExecLoadTableData() throws Exception {
    PageWithTable p = prepareTest(true);
    assertEquals(0, p.getTable().getRowCount());
  }

  @Test
  public void testExecLoadData() throws Exception {
    PageWithTable p = prepareTest(false);
    assertEquals(1, p.getTable().getRowCount());
    assertEquals(FIRST_COL_CONTENT, p.getTable().getFirstColumn().getValue(0));
    assertEquals(SECOND_COL_CONTENT, p.getTable().getSecondColumn().getValue(0));

    p.reloadPage();
    assertEquals(1, p.getTable().getRowCount());
    assertEquals(FIRST_COL_CONTENT, p.getTable().getFirstColumn().getValue(0));
    assertEquals(SECOND_COL_CONTENT, p.getTable().getSecondColumn().getValue(0));

    p.reloadPage();
    assertEquals(1, p.getTable().getRowCount());
    assertEquals(FIRST_COL_CONTENT, p.getTable().getFirstColumn().getValue(0));
    assertEquals(SECOND_COL_CONTENT, p.getTable().getSecondColumn().getValue(0));
  }

  private PageWithTable prepareTest(boolean invokeSuperExecLoadData) {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(new IOutline[]{new PageWithTableOutline(invokeSuperExecLoadData)});
    desktop.setOutline(PageWithTableOutline.class);

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithTableOutline.class, outline.getClass());

    IPage page = outline.getActivePage();
    assertNotNull(page);
    assertSame(PageWithTable.class, page.getClass());

    return (PageWithTable) page;
  }

  public static class PageWithTableOutline extends AbstractOutline {

    private final boolean m_invokeSuperExecLoadData;

    public PageWithTableOutline(boolean invokeSuperExecLoadData) {
      super(false);
      m_invokeSuperExecLoadData = invokeSuperExecLoadData;
      callInitializer();
    }

    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable(m_invokeSuperExecLoadData));
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {
    private final boolean m_invokeSuperExecLoadData;

    public PageWithTable(boolean invokeSuperExecLoadData) {
      super(false);
      m_invokeSuperExecLoadData = invokeSuperExecLoadData;
      callInitializer();
    }

    @Override
    protected void execLoadData(SearchFilter filter) throws ProcessingException {
      if (m_invokeSuperExecLoadData) {
        super.execLoadData(filter);
      }
      else {
        PageWithTableData pageData = new PageWithTableData();
        PageWithTableRowData row = pageData.addRow();
        row.setFirst(FIRST_COL_CONTENT);
        row.setSecond(SECOND_COL_CONTENT);
        getTable().importFromTableBeanData(pageData);
      }
    }

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      if (!m_invokeSuperExecLoadData) {
        Assert.fail("execLoadTableData must not be called!");
      }
      return new Object[0][];
    }

    @Override
    protected IPage execCreateChildPage(ITableRow row) throws ProcessingException {
      return new PageWithNode();
    }

    public class Table extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      @Order(10)
      public class FirstColumn extends AbstractStringColumn {

      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {

      }
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {
  }

  public static class PageWithTableData extends AbstractTablePageData {
    private static final long serialVersionUID = 1L;

    public PageWithTableData() {
    }

    @Override
    public PageWithTableRowData[] getRows() {
      return (PageWithTableRowData[]) super.getRows();
    }

    public void setRows(PageWithTableRowData[] rows) {
      super.setRows(rows);
    }

    @Override
    public PageWithTableRowData addRow() {
      return (PageWithTableRowData) super.addRow();
    }

    @Override
    public PageWithTableRowData addRow(int rowState) {
      return (PageWithTableRowData) super.addRow(rowState);
    }

    @Override
    public PageWithTableRowData rowAt(int idx) {
      return (PageWithTableRowData) super.rowAt(idx);
    }

    @Override
    public PageWithTableRowData createRow() {
      return new PageWithTableRowData();
    }

    @Override
    public Class<? extends AbstractTableRowData> getRowType() {
      return PageWithTableRowData.class;
    }

    public static class PageWithTableRowData extends AbstractTableRowData {
      private static final long serialVersionUID = 1L;

      public PageWithTableRowData() {
      }

      public static final String first = "first";
      public static final String second = "second";
      private String m_first;
      private String m_second;

      public String getFirst() {
        return m_first;
      }

      public void setFirst(String first) {
        m_first = first;
      }

      public String getSecond() {
        return m_second;
      }

      public void setSecond(String second) {
        m_second = second;
      }
    }
  }
}
