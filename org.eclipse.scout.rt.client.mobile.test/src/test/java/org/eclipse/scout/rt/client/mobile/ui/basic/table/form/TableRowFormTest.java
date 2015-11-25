package org.eclipse.scout.rt.client.mobile.ui.basic.table.form;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.AbstractMobileTable;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.ClearTableSelectionFormCloseListener;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ClientTestRunner.class)
@RunWithSubject("anna")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class TableRowFormTest {
  private Table m_table;
  private ITableRowForm m_tableRowForm;

  @Before
  public void setUp() {
    m_table = new Table();
    ITableRow row = m_table.createRow();
    m_table.getString1EditableColumn().setValue(row, "value 1");
    m_table.getString2EditableWrapColumn().setValue(row, "value 2");
    m_table.getString3NotEditableColumn().setValue(row, "value 3");
    m_table.getString4NotEditableWrapColumn().setValue(row, "value 4");
    m_table.addRow(row);

    m_table.selectFirstRow();
  }

  @After
  public void tearDown() {
    m_tableRowForm.doClose();
  }

  @Test
  public void testLabelSet() {
    assertLabelsSet();
  }

  @Test
  public void testLabelChange() {
    try {
      m_table.getString1EditableColumn().m_headerText = "new header text";
      m_table.getString1EditableColumn().decorateHeaderCell();

      assertLabelsSet();

    }
    finally {
      m_table.getString1EditableColumn().m_headerText = m_table.getString1EditableColumn().getConfiguredHeaderText();
      m_table.getString1EditableColumn().decorateHeaderCell();
    }
  }

  @Test
  public void testLabelLineBreakRemoved() {
    try {
      m_table.getString1EditableColumn().m_headerText = "line\nbreak";
      m_table.getString1EditableColumn().decorateHeaderCell();

      boolean labelFound = false;
      for (IFormField field : m_tableRowForm.getAllFields()) {
        if ("line break".equals(field.getLabel())) {
          labelFound = true;
        }
      }
      Assert.assertTrue("Field label must not contain line breaks.", labelFound);

    }
    finally {
      m_table.getString1EditableColumn().m_headerText = m_table.getString1EditableColumn().getConfiguredHeaderText();
      m_table.getString1EditableColumn().decorateHeaderCell();
    }
  }

  @Test
  public void testMultiline() {
    for (IFormField field : m_tableRowForm.getAllFields()) {
      if ("column 2".equals(field.getLabel()) || "column 4".equals(field.getLabel())) {
        Assert.assertTrue("Field should be bigger in case of multiline", field.getGridDataHints().h > 1);
      }
      else if ("column 1".equals(field.getLabel()) || "column 3".equals(field.getLabel())) {
        Assert.assertTrue("Field should have gridh of 1", field.getGridDataHints().h == 1);
      }
    }

  }

  private void assertLabelsSet() {
    List<String> labels = new ArrayList<String>();
    for (IColumn<?> column : m_table.getColumns()) {
      labels.add(column.getHeaderCell().getText());
    }
    for (IFormField field : m_tableRowForm.getAllFields()) {
      labels.remove(field.getLabel());
    }
    Assert.assertTrue(labels.isEmpty());
  }

  private class Table extends AbstractMobileTable {

    public String1EditableColumn getString1EditableColumn() {
      return getColumnSet().getColumnByClass(String1EditableColumn.class);
    }

    public String2EditableColumn getString2EditableWrapColumn() {
      return getColumnSet().getColumnByClass(String2EditableColumn.class);
    }

    public String3NotEditableColumn getString3NotEditableColumn() {
      return getColumnSet().getColumnByClass(String3NotEditableColumn.class);
    }

    public String4NotEditableWrapColumn getString4NotEditableWrapColumn() {
      return getColumnSet().getColumnByClass(String4NotEditableWrapColumn.class);
    }

    @Override
    protected void execRowsSelected(List<? extends ITableRow> rows) {
      if (CollectionUtility.hasElements(rows)) {
        startTableRowForm(CollectionUtility.firstElement(rows));
      }
    }

    @Override
    protected ITableRowFormProvider createTableRowFormProvider() {
      return new DefaultTableRowFormProvider() {
        @Override
        public ITableRowForm createTableRowForm(ITableRow row) {
          ITableRowForm form = super.createTableRowForm(row);
          form.setShowOnStart(false); // Disable to avoid
          // ClientJob usage
          form.addFormListener(new ClearTableSelectionFormCloseListener(
              Table.this));
          m_tableRowForm = form;
          return form;
        }
      };
    }

    @Order(10)
    public class String1EditableColumn extends AbstractStringColumn {
      public String m_headerText;

      @Override
      protected void execDecorateHeaderCell(HeaderCell cell) {
        if (m_headerText != null) {
          cell.setText(m_headerText);
        }
      }

      @Override
      protected String getConfiguredHeaderText() {
        return "column 1";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected int getConfiguredWidth() {
        return 100;
      }
    }

    @Order(20)
    public class String2EditableColumn extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "column 2";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return true;
      }

      @Override
      protected boolean getConfiguredTextWrap() {
        return true;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

    @Order(30)
    public class String3NotEditableColumn extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "column 3";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return false;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

    @Order(40)
    public class String4NotEditableWrapColumn extends AbstractStringColumn {

      @Override
      protected String getConfiguredHeaderText() {
        return "column 4";
      }

      @Override
      protected boolean getConfiguredEditable() {
        return false;
      }

      @Override
      protected boolean getConfiguredTextWrap() {
        return true;
      }

      @Override
      protected int getConfiguredWidth() {
        return 200;
      }
    }

  }
}
