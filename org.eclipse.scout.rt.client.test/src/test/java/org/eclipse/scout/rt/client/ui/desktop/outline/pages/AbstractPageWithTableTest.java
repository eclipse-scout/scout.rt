package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.mockito.Mockito;

import org.junit.Test;

public class AbstractPageWithTableTest {

  private AbstractPageWithTable<ITable> m_page = new AbstractPageWithTable<ITable>(false, null) {
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

  /**
   * This test checks whether or not all boolean flags are copied from the table-row to the new page object when the
   * virtual-node is resolved. Copying the rejectedByUser property solves the problem described in ticket #163450.
   */
  @Test
  public void testExecResolveVirtualChildNode() {
    doTestExecResolveVirtualChildNode(true);
    doTestExecResolveVirtualChildNode(false);
  }

  private void doTestExecResolveVirtualChildNode(boolean value) {
    VirtualPage vpage = new VirtualPage();
    ITableRow tableRow = Mockito.mock(ITableRow.class);
    Mockito.when(tableRow.isFilterAccepted()).thenReturn(value);
    Mockito.when(tableRow.isRejectedByUser()).thenReturn(value);
    Mockito.when(tableRow.isEnabled()).thenReturn(value);

    m_page.linkTableRowWithPage(tableRow, vpage);
    ITreeNode childPage = m_page.execResolveVirtualChildNode(vpage);

    assertEquals(value, childPage.isFilterAccepted());
    assertEquals(value, childPage.isRejectedByUser());
    assertEquals(value, childPage.isEnabled());
    assertEquals("fooBar", childPage.getCell().getText());
    assertSame(tableRow, m_page.getTableRowFor(childPage));
  }

}
