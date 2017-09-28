/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class AbstractPageWithTableTest {

  @Test
  public void doDisposeSearchForm() {
    // Setup
    final TestSearchForm searchForm = new TestSearchForm();
    final TestSearchForm searchFormMock = Mockito.spy(searchForm);
    AbstractPageWithTable<ITable> pageWithSearchForm = new AbstractPageWithTable<ITable>() {

      @Override
      protected ITable createTable() {
        return Mockito.mock(ITable.class);
      }

      @Override
      protected ISearchForm createSearchForm() {
        return searchFormMock;
      }
    };

    // Begin test
    pageWithSearchForm.ensureSearchFormCreated();
    pageWithSearchForm.ensureSearchFormStarted();
    Mockito.verify(searchFormMock, Mockito.times(0)).disposeFormInternal();

    pageWithSearchForm.dispose();
    Mockito.verify(searchFormMock, Mockito.times(1)).disposeFormInternal();
  }

  @Test
  public void testModelContextOfInitPageAndInitTable() {
    IForm mockForm = Mockito.mock(IForm.class);
    IOutline mockOutline = Mockito.mock(IOutline.class);
    ClientRunContexts
        .copyCurrent()
        .withOutline(mockOutline, true)
        .withForm(mockForm)
        .run(new IRunnable() {
          @Override
          public void run() throws Exception {
            IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
            assertNotNull(desktop);

            desktop.setAvailableOutlines(CollectionUtility.arrayList(new PageWithTableOutline()));
            desktop.setOutline(PageWithTableOutline.class);
            desktop.activateFirstPage();

            IOutline outline = desktop.getOutline();
            assertNotNull(outline);
            assertSame(PageWithTableOutline.class, outline.getClass());

            IPage<?> page = outline.getActivePage();
            assertNotNull(page);
            assertSame(ParentItemTablePage.class, page.getClass());
            ParentItemTablePage tablePage = (ParentItemTablePage) page;

            // init page
            ModelContext initPageContext = tablePage.getInitPageContext();
            assertNotNull(initPageContext);
            assertSame(desktop, initPageContext.getDesktop());
            assertSame(outline, initPageContext.getOutline());
            assertNull(initPageContext.getForm()); // no context form must be set

            // init table
            ModelContext initTableContext = tablePage.getInitTableContext();
            assertNotNull(initTableContext);
            assertSame(desktop, initTableContext.getDesktop());
            assertSame(outline, initTableContext.getOutline());
            assertNull(initTableContext.getForm()); // no context form must be set
          }
        });
  }

  public static class TestSearchForm extends AbstractSearchForm {

    @Override
    public void disposeFormInternal() {
      super.disposeFormInternal();
    }

    public class MainBox extends AbstractGroupBox {
    }
  }

  public static class PageWithTableOutline extends AbstractOutline {
    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new ParentItemTablePage());
    }
  }

  public static class ParentItemTablePage extends AbstractPageWithTable<ITable> {

    private final FinalValue<ModelContext> m_initPageContext = new FinalValue<>();
    private final FinalValue<ModelContext> m_initTableContext = new FinalValue<>();

    public ModelContext getInitPageContext() {
      return m_initPageContext.get();
    }

    public ModelContext getInitTableContext() {
      return m_initTableContext.get();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Parent page";
    }

    @Override
    protected void execInitPage() {
      m_initPageContext.set(ModelContext.copyCurrent());
      super.execInitPage();
    }

    @Override
    protected void execInitTable() {
      m_initTableContext.set(ModelContext.copyCurrent());
      super.execInitTable();
    }

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{
          {"one"},
          {"two"}
      });
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
      return new ItemNodePage();
    }

    public class Table extends AbstractTable {

      public IdColumn getIdColumn() {
        return getColumnSet().getColumnByClass(IdColumn.class);
      }

      @Order(1000)
      @ClassId("3c94b7fc-6a95-4ff8-8466-ff23d805f158")
      public class IdColumn extends AbstractStringColumn {
        @Override
        protected int getConfiguredWidth() {
          return 100;
        }
      }
    }
  }

  public static class ItemNodePage extends AbstractPageWithNodes {

    @Override
    protected String getConfiguredTitle() {
      return "Child page";
    }
  }
}
