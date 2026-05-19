/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

/**
 * JUnit tests for {@link AbstractPage}
 *
 * @since 3.10.0
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageTest {
  private static final String TEST_PAGE_CLASS_ID = "TEST_CLASS_ID";

  @Test
  public void testClassIdAnnotatedPage() {
    PageWithClassId testPage = new PageWithClassId();
    assertEquals("classid should correspond to annotated id", TEST_PAGE_CLASS_ID, testPage.classId());
  }

  @Test
  public void testComputedClassId() {
    PageWithoutClassId testPageNoAnnotation = new PageWithoutClassId();
    String className = testPageNoAnnotation.getClass().getName();
    assertEquals("classid should correspond to annotated id", className, testPageNoAnnotation.classId());
  }

  /**
   * {@link AbstractPage#execPageActivated()}, if a ProcessingExeption is thrown
   */
  @Test(expected = ProcessingException.class)
  public void testPageActivatedProcessingException() {
    final IPage<?> testPage = new PageExceptionOnActivated();
    testPage.pageActivatedNotify();
  }

  /**
   * {@link AbstractPage#execPageActivated()} , if a Exeption is thrown
   */
  @Test(expected = RuntimeException.class)
  public void testPageActivatedException() {
    final IPage<?> testPage = new PageRuntimeExceptionOnActivated();
    testPage.pageActivatedNotify();
  }

  @Test
  public void testPageActivated() {
    final IPage<?> testPage = new P_Page();
    assertFalse(testPage.hasBeenActivated());
    assertFalse(testPage.isPageActive());

    testPage.pageActivatedNotify();
    assertTrue(testPage.hasBeenActivated());
    assertTrue(testPage.isPageActive());

    testPage.pageDeactivatedNotify();
    assertTrue(testPage.hasBeenActivated());
    assertFalse(testPage.isPageActive());
  }

  /**
   * {@link AbstractPage#execPageDeactivated()}, if a ProcessingExeption is thrown
   */
  @Test(expected = ProcessingException.class)
  public void testPageDeactivatedProcessingException() {
    final IPage<?> testPage = new PageExceptionOnDeactivated();
    testPage.pageDeactivatedNotify();
  }

  /**
   * {@link AbstractPage#execPageDeactivated()}, if a Exeption is thrown
   */
  @Test(expected = RuntimeException.class)
  public void testPageDeactivatedException() {
    final IPage<?> testPage = new PageRuntimeExceptionOnDeactivated();
    testPage.pageDeactivatedNotify();
  }

  @Test
  public void testIsSetDetailFormVisible() {
    AbstractPage<?> p = new P_Page();
    assertTrue(p.isDetailFormVisible());
    P_Outline outline = P_Outline.createMock();
    outline.addChildNode(outline.getRootNode(), p);
    p.setDetailFormVisible(false);
    assertFalse(p.isDetailFormVisible());
    Mockito.verify(outline).firePageChanged(Mockito.eq(p));
  }

  @Test
  public void testIsSetTableVisible() {
    AbstractPage<?> p = new P_Page();
    assertTrue(p.isTableVisible());
    P_Outline outline = P_Outline.createMock();
    outline.addChildNode(outline.getRootNode(), p);
    p.setTableVisible(false);
    assertFalse(p.isTableVisible());
    Mockito.verify(outline).firePageChanged(Mockito.eq(p));
  }

  @Test
  public void testCollapseLazyNodeOnReload() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();
    IOutline outline = desktop.getOutline();
    PageWithTable page = (PageWithTable) outline.getActivePage();

    assertEquals(3, page.getChildNodeCount());
    assertFalse(page.isExpanded());
    assertTrue(page.isExpandedLazy());

    // -----

    // Drill down to child node -> page should be expanded lazily

    outline.drillDown(page.getChildPage(0));
    assertTrue(page.isExpanded());
    assertTrue(page.isExpandedLazy());

    // -----

    // Reload page -> should be collapsed automatically

    outline.selectNode(page);
    page.reloadPage();

    assertEquals(3, page.getChildNodeCount());
    assertFalse(page.isExpanded());
    assertFalse(page.isExpandedLazy());

    // -----

    // Expand page non-lazily and reload -> page should stay expanded

    page.setExpanded(true, false);
    assertTrue(page.isExpanded());
    assertFalse(page.isExpandedLazy());

    page.reloadPage();
    assertEquals(3, page.getChildNodeCount());
    assertTrue(page.isExpanded());
    assertFalse(page.isExpandedLazy());
  }

  // ------------------------------------------------------------------

  class P_Page extends AbstractPage<ITable> {
    @Override
    protected ITable createTable() {
      return null;
    }
  }

  @ClassId(TEST_PAGE_CLASS_ID)
  class PageWithClassId extends P_Page {
  }

  class PageWithoutClassId extends P_Page {
  }

  class PageExceptionOnActivated extends P_Page {
    @Override
    protected void execPageActivated() {
      throw new ProcessingException();
    }
  }

  class PageRuntimeExceptionOnActivated extends P_Page {
    @Override
    protected void execPageActivated() {
      throw new RuntimeException();
    }
  }

  class PageExceptionOnDeactivated extends P_Page {
    @Override
    protected void execPageDeactivated() {
      throw new ProcessingException();
    }
  }

  class PageRuntimeExceptionOnDeactivated extends P_Page {
    @Override
    protected void execPageDeactivated() {
      throw new RuntimeException();
    }
  }

  public static class P_Outline extends AbstractOutline {

    public P_Outline() {
      super();
    }

    public P_Outline(boolean callInitializer) {
      super(callInitializer);
    }

    public static P_Outline createMock() {
      P_Outline mock = Mockito.spy(new P_Outline(false));
      mock.callInitializer();
      return mock;
    }
  }

  class PageWithTableOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    @Override
    protected void execLoadData(SearchFilter filter) {
      final Object[][] data = new Object[][]{
          new Object[]{"red"},
          new Object[]{"green"},
          new Object[]{"blue"}
      };
      importTableData(data);
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
      return new AbstractPageWithNodes() {
      };
    }

    public class Table extends AbstractTable {
      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
      }
    }
  }
}
