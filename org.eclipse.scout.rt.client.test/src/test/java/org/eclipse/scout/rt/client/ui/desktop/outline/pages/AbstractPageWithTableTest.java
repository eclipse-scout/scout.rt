package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
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

  /**
   * This test checks whether or not all boolean flags are copied from the table-row to the new page object when the
   * virtual-node is resolved. Copying the rejectedByUser property solves the problem described in ticket #163450.
   */
  @Test
  public void testExecResolveVirtualChildNode() {
    AbstractPageWithTable<ITable> page = new AbstractPageWithTable<ITable>(false, null) {
      @Override
      protected ITable initTable() {
        Cell statusCell = new Cell();
        statusCell.setText("fooBar");

        ITable table = Mockito.mock(ITable.class);
        Mockito.when(table.getSummaryCell(Mockito.any(ITableRow.class))).thenReturn(statusCell);
        return table;
      }

      @Override
      protected IPage<ITable> createChildPageInternal(ITableRow row) {
        return new AbstractPage<ITable>(false) {
          @Override
          protected ITable initTable() {
            return null;
          }
        };
      }
    };

    doTestExecResolveVirtualChildNode(page, true);
    doTestExecResolveVirtualChildNode(page, false);
  }

  private void doTestExecResolveVirtualChildNode(AbstractPageWithTable<ITable> page, boolean value) {
    VirtualPage vpage = new VirtualPage();
    ITableRow tableRow = Mockito.mock(ITableRow.class);
    Mockito.when(tableRow.isFilterAccepted()).thenReturn(value);
    Mockito.when(tableRow.isRejectedByUser()).thenReturn(value);
    Mockito.when(tableRow.isEnabled()).thenReturn(value);

    page.linkTableRowWithPage(tableRow, vpage);
    ITreeNode childPage = page.execResolveVirtualChildNode(vpage);

    assertEquals(value, childPage.isFilterAccepted());
    assertEquals(value, childPage.isRejectedByUser());
    assertEquals(value, childPage.isEnabled());
    assertEquals("fooBar", childPage.getCell().getText());
    assertSame(tableRow, page.getTableRowFor(childPage));
  }

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
