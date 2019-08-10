/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that a {@link AbstractPageWithTable} having getConfiguredExpanded=true has visible children even if the child
 * pages are created after the first load.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AutoExpandPageWithTableTest {

  private static final int NUM_ROWS = 2;
  private boolean m_isLeaf = true;

  @Test
  public void testExecPageDataLoaded() {
    testWithLeafStatus(true); // test with page configured to be leaf: no child nodes are created
    testWithLeafStatus(false);// test with page configured to have children: child nodes are created if the table gets populated
  }

  private void testWithLeafStatus(boolean isLeaf) {
    m_isLeaf = isLeaf;

    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();
    IOutline outline = desktop.getOutline();

    PageWithTable page = (PageWithTable) outline.getActivePage();
    assertEquals(isLeaf, page.isLeaf());
    assertEquals(0, page.getChildNodeCount());

    page.m_isTableEmpty = false; // load data from now on
    page.reloadPage();
    assertEquals(isLeaf, page.isLeaf());
    if (isLeaf) {
      assertEquals(0, page.getChildNodeCount()); // no child nodes if configured to be a leaf
    }
    else {
      assertEquals(NUM_ROWS, page.getChildNodeCount());
    }
  }

  private class PageWithTableOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  private class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    private boolean m_isTableEmpty = true;

    @Override
    protected void execLoadData(SearchFilter filter) {
      final Object[][] data;
      if (m_isTableEmpty) {
        data = new Object[][]{};
      }
      else {
        data = new Object[NUM_ROWS][];
        for (int i = 0; i < NUM_ROWS; i++) {
          data[i] = new Object[]{Integer.toString(i)};
        }
      }
      importTableData(data);
    }

    @Override
    protected boolean getConfiguredLazyExpandingEnabled() {
      return false;
    }

    @Override
    protected boolean getConfiguredLeaf() {
      return m_isLeaf;
    }

    @Override
    protected boolean getConfiguredExpanded() {
      return true;
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
      return new ChildPageWithNode();
    }

    public class Table extends AbstractTable {
      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
      }
    }
  }

  private static class ChildPageWithNode extends AbstractPageWithNodes {

  }
}
