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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.util.Collection;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.AbstractDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.testing.client.runner.ScoutClientTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests auto resize feature
 */
@RunWith(ScoutClientTestRunner.class)
public class ColumnAutoResizeTest {

  @Test
  public void testReloadPage_multipleSummaryColumns() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(new IOutline[]{new PageWithTableOutline()});
    desktop.setOutline(PageWithTableOutline.class);
    IOutline outline = desktop.getOutline();

    IPage page = outline.getActivePage();
    Assert.assertNotNull(page);
    Assert.assertTrue(page instanceof AbstractPageWithTable);
    ITable table = ((AbstractPageWithTable) page).getTable();
    Assert.assertTrue(table instanceof ColumnAutoResizeTest.PageWithTable.TestTable);
    ColumnAutoResizeTest.PageWithTable.TestTable testTable = (ColumnAutoResizeTest.PageWithTable.TestTable) table;
    IColumn col1 = testTable.getColumns()[0];
    int width1 = col1.getWidth();
    // when page is reloaded, the column width shall not be different afterwards
    page.reloadPage();
    int width2 = col1.getWidth();
    Assert.assertTrue(width1 == width2);
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(Collection<IPage> pageList) throws ProcessingException {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.TestTable> {

    @Override
    protected Object[][] execLoadTableData(SearchFilter filter) throws ProcessingException {
      return new Object[][]{new Object[]{"a", "b"}};
    }

    @Override
    protected IPage execCreateChildPage(ITableRow row) throws ProcessingException {
      return new PageWithNode();
    }

    public class TestTable extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredSummary() {
          return true;
        }

        @Override
        protected int getConfiguredWidth() {
          return 100;
        }

      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredSummary() {
          return true;
        }

        @Override
        protected int getConfiguredWidth() {
          return super.getConfiguredWidth();
        }
      }
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {
  }
}
