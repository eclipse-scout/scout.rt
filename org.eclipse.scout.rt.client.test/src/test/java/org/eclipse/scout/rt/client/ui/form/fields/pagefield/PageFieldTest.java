package org.eclipse.scout.rt.client.ui.form.fields.pagefield;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTableTest.TestSearchForm;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IPage;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.ISearchForm;
import org.eclipse.scout.rt.platform.holders.IntegerHolder;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageFieldTest {

  @Test
  public void testDispose() {
    final IntegerHolder pageDisposedCount = new IntegerHolder(0);
    final TestSearchForm searchForm = new TestSearchForm();
    final TestSearchForm searchFormMock = Mockito.spy(searchForm);
    final ITable table = new AbstractTable() {
    };
    AbstractPageWithTable<ITable> page = new AbstractPageWithTable<ITable>() {

      @Override
      protected ITable createTable() {
        return table;
      }

      @Override
      protected ISearchForm createSearchForm() {
        return searchFormMock;
      }

      @Override
      public void disposeInternal() {
        super.disposeInternal();
        pageDisposedCount.setValue(pageDisposedCount.getValue() + 1);
      }
    };

    PageField pageField = new PageField();
    pageField.initField();
    pageField.setPage(page);
    Mockito.verify(searchFormMock, Mockito.times(0)).disposeFormInternal();
    assertEquals(0, pageDisposedCount.getValue().intValue());

    pageField.disposeField();
    Mockito.verify(searchFormMock, Mockito.times(1)).disposeFormInternal();
    assertEquals(1, pageDisposedCount.getValue().intValue());
  }

  public static class PageField extends AbstractPageField<IPage> {
  }

}
