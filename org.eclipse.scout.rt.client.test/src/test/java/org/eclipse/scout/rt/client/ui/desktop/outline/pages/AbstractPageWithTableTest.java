package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
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
      protected ITable initTable() {
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

  public static class TestSearchForm extends AbstractSearchForm {

    @Override
    public void disposeFormInternal() {
      super.disposeFormInternal();
    }

    public class MainBox extends AbstractGroupBox {
    }
  }
}
