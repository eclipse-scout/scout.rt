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
package org.eclipse.scout.rt.client.ui.desktop.outline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests bookmark activation to invisible outlines
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class BookmarkToInvisibleOutlineTest {

  @Test
  public void testBookmarkToInvisibleOutline() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(CollectionUtility.arrayList(new CockpitOutline(), new AdminOutline()));
    desktop.setOutline(CockpitOutline.class);
    Bookmark bm = desktop.createBookmark();
    //
    desktop.setOutline(AdminOutline.class);
    desktop.findOutline(CockpitOutline.class).setVisible(false);
    Thread.sleep(400);
    Exception err = null;
    try {
      desktop.activateBookmark(bm);
    }
    catch (Exception e) {
      err = e;
    }
    assertNotNull(err);
    assertEquals(desktop.findOutline(AdminOutline.class), desktop.getOutline());
  }

  public static class CockpitOutline extends AbstractOutline {
    @Override
    protected String getConfiguredTitle() {
      return "Cockpit";
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new CockpitMainTablePage());
      pageList.add(new CockpitSecondTablePage());
    }
  }

  public static class AdminOutline extends AbstractOutline {
    @Override
    protected String getConfiguredTitle() {
      return "Administration";
    }

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new AdminMainTablePage());
    }
  }

  public static class CockpitMainTablePage extends AbstractPageWithTable<CockpitMainTablePage.Table> {

    @Override
    protected String getConfiguredTitle() {
      return "Cockpit";
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{1L, "5 Tickets"},
          new Object[]{2L, "12 Appointments"},
          new Object[]{3L, "3 E-Mails"},});
    }

    public class Table extends AbstractTable {
      @Order(0)
      public class KeyColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }

        @Override
        protected boolean getConfiguredDisplayable() {
          return true;
        }
      }

      @Order(10)
      public class StringColumn extends AbstractStringColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "String";
        }
      }
    }
  }

  public static class CockpitSecondTablePage extends AbstractPageWithTable<CockpitMainTablePage.Table> {

    @Override
    protected String getConfiguredTitle() {
      return "Subsidiary";
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{1L, "Rated tickets"},
          new Object[]{2L, "Rated appointments"},
          new Object[]{3L, "Rated E-Mails"},});
    }

    public class Table extends AbstractTable {
      @Order(0)
      public class KeyColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }

        @Override
        protected boolean getConfiguredDisplayable() {
          return true;
        }
      }

      @Order(10)
      public class StringColumn extends AbstractStringColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "String";
        }
      }
    }
  }

  public static class AdminMainTablePage extends AbstractPageWithTable<CockpitMainTablePage.Table> {
    @Override
    protected String getConfiguredTitle() {
      return "Administration";
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          new Object[]{1L, "User Account"},
          new Object[]{2L, "Guest Account"},
          new Object[]{3L, "Supervisor Account"},});
    }

    public class Table extends AbstractTable {
      @Order(0)
      public class KeyColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }

        @Override
        protected boolean getConfiguredDisplayable() {
          return true;
        }
      }

      @Order(10)
      public class StringColumn extends AbstractStringColumn {
        @Override
        protected String getConfiguredHeaderText() {
          return "String";
        }
      }
    }
  }
}
