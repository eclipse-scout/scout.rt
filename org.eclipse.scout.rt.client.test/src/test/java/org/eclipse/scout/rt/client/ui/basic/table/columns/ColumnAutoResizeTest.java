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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithNodes;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests auto resize feature
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ColumnAutoResizeTest {

  @Test
  public void testReloadPage_multipleSummaryColumns() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();
    IOutline outline = desktop.getOutline();

    IPage<?> page = outline.getActivePage();
    assertNotNull(page);
    assertTrue(page instanceof AbstractPageWithTable);
    ITable table = ((AbstractPageWithTable) page).getTable();
    assertTrue(table instanceof ColumnAutoResizeTest.PageWithTable.TestTable);
    ColumnAutoResizeTest.PageWithTable.TestTable testTable = (ColumnAutoResizeTest.PageWithTable.TestTable) table;
    IColumn col1 = CollectionUtility.firstElement(testTable.getColumns());
    int width1 = col1.getWidth();
    // when page is reloaded, the column width shall not be different afterwards
    page.reloadPage();
    int width2 = col1.getWidth();
    assertTrue(width1 == width2);
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.TestTable> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{new Object[]{"a", "b"}});
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
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
