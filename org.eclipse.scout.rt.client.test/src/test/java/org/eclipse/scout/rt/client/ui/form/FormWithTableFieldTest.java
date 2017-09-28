/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertEquals;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.FormWithTableFieldTest.TableForm.MainBox.LoremField;
import org.eclipse.scout.rt.client.ui.form.FormWithTableFieldTest.TableForm.MainBox.LoremField.Table;
import org.eclipse.scout.rt.client.ui.form.FormWithTableFieldTest.TableFormData.Lorem;
import org.eclipse.scout.rt.client.ui.form.FormWithTableFieldTest.TableFormData.Lorem.LoremRowData;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test import/export formData in a Form where a TableField and Button has the same "formData Id".
 *
 * @See Bugzilla 435680
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class FormWithTableFieldTest {

  private TableForm m_tableForm;

  @Before
  public void before() {
    m_tableForm = new TableForm();
  }

  @After
  public void after() {
    if (m_tableForm == null || !m_tableForm.isShowing()) {
      return;
    }
    m_tableForm.doClose();
  }

  @Test
  public void testImportFormData() {
    TableForm form = m_tableForm;
    form.startModify();
    assertEquals(1, form.getLoremField().getTable().getRowCount());
  }

  @Test
  public void testExportFormData() {
    TableForm form = m_tableForm;
    form.startNew();
    Table table = form.getLoremField().getTable();
    assertEquals(0, table.getRowCount());
    ITableRow row = table.createRow();
    row = table.addRow(row);
    table.getNameColumn().setValue(row, "Ipsum");
    assertEquals(1, table.getRowCount());

    TableFormData formData = new TableFormData();
    form.exportFormData(formData);
    assertEquals(1, formData.getLorem().getRowCount());

  }

  @FormData(value = TableFormData.class, sdkCommand = FormData.SdkCommand.CREATE)
  public class TableForm extends AbstractForm {

    /**
     * @throws org.eclipse.scout.rt.platform.exception.ProcessingException
     */
    public TableForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "Form with table import";
    }

    /**
     * @return the LoremField
     */
    public LoremField getLoremField() {
      return getFieldByClass(LoremField.class);
    }

    /**
     * @throws org.eclipse.scout.rt.platform.exception.ProcessingException
     */
    public void startModify() {
      startInternal(new ModifyHandler());
    }

    /**
     * @throws org.eclipse.scout.rt.platform.exception.ProcessingException
     */
    public void startNew() {
      startInternal(new NewHandler());
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Order(10)
      public class LoremField extends AbstractTableField<Table> {

        @Override
        protected int getConfiguredGridH() {
          return 5;
        }

        public class Table extends AbstractTable {

          /**
           * @return the NameColumn
           */
          public NameColumn getNameColumn() {
            return getColumnSet().getColumnByClass(NameColumn.class);
          }

          @Order(20)
          public class NameColumn extends AbstractStringColumn {
          }
        }
      }

      @Order(20)
      public class OkButton extends AbstractOkButton {
      }

      @Order(30)
      public class CancelButton extends AbstractCancelButton {
      }

      @Order(40)
      public class LoremButton extends AbstractButton {
        @Override
        protected String getConfiguredLabel() {
          return "Lorem";
        }
      }
    }

    public class ModifyHandler extends AbstractFormHandler {

      @Override
      protected void execLoad() {
        TableFormData formData = new TableFormData();
        Lorem table = formData.getLorem();
        LoremRowData rowData = table.addRow();
        rowData.setName("Hello");
        importFormData(formData);
      }
    }

    public class NewHandler extends AbstractFormHandler {
    }
  }

  public static class TableFormData extends AbstractFormData {

    private static final long serialVersionUID = 1L;

    public TableFormData() {
    }

    public Lorem getLorem() {
      return getFieldByClass(Lorem.class);
    }

    public static class Lorem extends AbstractTableFieldBeanData {

      private static final long serialVersionUID = 1L;

      @Override
      public LoremRowData addRow() {
        return (LoremRowData) super.addRow();
      }

      @Override
      public LoremRowData addRow(int rowState) {
        return (LoremRowData) super.addRow(rowState);
      }

      @Override
      public LoremRowData createRow() {
        return new LoremRowData();
      }

      @Override
      public Class<? extends AbstractTableRowData> getRowType() {
        return LoremRowData.class;
      }

      @Override
      public LoremRowData[] getRows() {
        return (LoremRowData[]) super.getRows();
      }

      @Override
      public LoremRowData rowAt(int index) {
        return (LoremRowData) super.rowAt(index);
      }

      public void setRows(LoremRowData[] rows) {
        super.setRows(rows);
      }

      public static class LoremRowData extends AbstractTableRowData {

        private static final long serialVersionUID = 1L;
        public static final String name = "name";
        private String m_name;

        public String getName() {
          return m_name;
        }

        public void setName(String newName) {
          m_name = newName;
        }
      }
    }
  }
}
