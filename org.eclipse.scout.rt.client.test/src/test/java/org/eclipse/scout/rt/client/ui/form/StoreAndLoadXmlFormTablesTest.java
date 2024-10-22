/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlFormTablesTest.FullTestForm.MainBox.GroupBox.FullTableField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlFormTablesTest.FullTestForm.MainBox.GroupBox.FullTableField.FullTestTable;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlFormTablesTest.PartialTestForm.MainBox.GroupBox.PartialTableField;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXmlFormTablesTest.PartialTestForm.MainBox.GroupBox.PartialTableField.PartialTestTable;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCloseButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Load and store of tables containing custom values and classes (classloading issues)
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class StoreAndLoadXmlFormTablesTest {

  static final Object[][] TABLE_DATA = new Object[][]{
      new Object[]{1L, "One", new java.util.Date()},
      new Object[]{2L, "Two", new StoreAndLoadXmlFormTablesTest.InnerClass()},
      new Object[]{3L, "Three", new StoreAndLoadXmlFormTablesTest.InnerClass.InnerInnerClass()}
  };

  public static class InnerClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String m_value = "Inner Level 1";

    @Override
    public int hashCode() {
      return m_value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || obj.getClass() != this.getClass()) {
        return false;
      }
      return ((InnerClass) obj).m_value.equals(this.m_value);
    }

    public static class InnerInnerClass implements Serializable {
      private static final long serialVersionUID = 2L;

      private final String m_value = "Inner Level 2";

      @Override
      public int hashCode() {
        return m_value.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
          return false;
        }
        return ((InnerInnerClass) obj).m_value.equals(this.m_value);
      }
    }
  }

  public static class AbstractTestTable extends AbstractTable {

    public AbstractTestTable.KeyColumn getKeyColumn() {
      return getColumnSet().getColumnByClass(AbstractTestTable.KeyColumn.class);
    }

    public AbstractTestTable.StringColumn getStringColumn() {
      return getColumnSet().getColumnByClass(AbstractTestTable.StringColumn.class);
    }

    @Order(10)
    public class KeyColumn extends AbstractLongColumn {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }

      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }

    @Order(20)
    public class StringColumn extends AbstractStringColumn {
      @Override
      protected String getConfiguredHeaderText() {
        return "String";
      }

      @Override
      protected int getConfiguredWidth() {
        return 100;
      }
    }
  }

  public static class TestFormWithoutTable extends AbstractForm {

    @Order(10)
    public class MainBox extends AbstractGroupBox {
    }
  }

  public static class FullTestForm extends AbstractForm {

    @Override
    protected String getConfiguredTitle() {
      return "FullTestForm";
    }

    public void startModify() {
      startInternal(new ModifyHandler());
    }

    public FullTableField getFullTableField() {
      return getFieldByClass(FullTableField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridColumnCount() {
        return 2;
      }

      @Order(10)
      public class GroupBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 2;
        }

        @Order(10)
        public class FullTableField extends AbstractTableField<FullTestTable> {

          @Override
          protected int getConfiguredGridW() {
            return 2;
          }

          @Override
          protected int getConfiguredGridH() {
            return 6;
          }

          @Override
          protected void execReloadTableData() {
            Object[][] data = TABLE_DATA;
            getTable().replaceRowsByMatrix(data);
            super.execReloadTableData();
          }

          public class FullTestTable extends AbstractTestTable {

            public FullTestTable.CustomColumn getCustomColumn() {
              return getColumnSet().getColumnByClass(FullTestTable.CustomColumn.class);
            }

            @Order(30)
            public class CustomColumn extends AbstractObjectColumn {
              @Override
              protected String getConfiguredHeaderText() {
                return "Custom";
              }

              @Override
              protected int getConfiguredWidth() {
                return 100;
              }
            }
          }
        }
      }

      @Order(20)
      public class CloseButton extends AbstractCloseButton {
      }
    }

    public class ModifyHandler extends AbstractFormHandler {
      @Override
      protected void execLoad() {
        getFullTableField().reloadTableData();
      }
    }
  }

  public static class PartialTestForm extends AbstractForm {

    @Override
    protected String getConfiguredTitle() {
      return "PartialTestForm";
    }

    public PartialTableField getPartialTableField() {
      return getFieldByClass(PartialTableField.class);
    }

    public MainBox getMainBox() {
      return getFieldByClass(MainBox.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridColumnCount() {
        return 2;
      }

      @Order(10)
      public class GroupBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 2;
        }

        @Order(10)
        public class PartialTableField extends AbstractTableField<PartialTestTable> {

          public class PartialTestTable extends AbstractTestTable {
          }
        }
      }
    }
  }

  @Test
  public void testLoadTableData() {
    FullTestForm f = new FullTestForm();
    try {
      f.startModify();
      assertArrayEquals(TABLE_DATA, f.getFullTableField().getTable().getTableData());
      //store xml and clear
      String xml = f.storeToXmlString();
      f.getFullTableField().getTable().discardAllRows();
      assertArrayEquals(new Object[0][0], f.getFullTableField().getTable().getTableData());
      //load xml
      assertTrue(f.loadFromXmlString(xml));
      assertArrayEquals(TABLE_DATA, f.getFullTableField().getTable().getTableData());
    }
    finally {
      f.doClose();
    }
  }

  @Test
  public void testLoadInvalidTableData() {
    FullTestForm sourceForm = new FullTestForm();
    PartialTestForm targetForm = new PartialTestForm();
    try {
      sourceForm.startModify();
      assertArrayEquals(TABLE_DATA, sourceForm.getFullTableField().getTable().getTableData());
      String xml = sourceForm.storeToXmlString();

      //load xml
      targetForm.start();
      assertArrayEquals(new Object[0][0], targetForm.getPartialTableField().getTable().getTableData());
      assertFalse(targetForm.loadFromXmlString(xml));
      assertArrayEquals(new Object[0][0], targetForm.getPartialTableField().getTable().getTableData());
    }
    finally {
      targetForm.doClose();
      sourceForm.doClose();
    }
  }

  @Test
  public void testLoadUnknownTableWithValue() {
    FullTestForm sourceForm = new FullTestForm();
    TestFormWithoutTable targetForm = new TestFormWithoutTable();
    try {
      sourceForm.startModify();
      assertArrayEquals(TABLE_DATA, sourceForm.getFullTableField().getTable().getTableData());
      String xml = sourceForm.storeToXmlString();

      //load xml
      targetForm.start();
      assertFalse(targetForm.loadFromXmlString(xml));
    }
    finally {
      targetForm.doClose();
      sourceForm.doClose();
    }
  }

  @Test
  public void testLoadUnknownTableWithoutValue() {
    FullTestForm sourceForm = new FullTestForm();
    TestFormWithoutTable targetForm = new TestFormWithoutTable();
    try {
      sourceForm.start();
      assertArrayEquals(new Object[0][0], sourceForm.getFullTableField().getTable().getTableData());
      String xml = sourceForm.storeToXmlString();

      //load xml
      targetForm.start();
      assertTrue(targetForm.loadFromXmlString(xml));
    }
    finally {
      targetForm.doClose();
      sourceForm.doClose();
    }
  }
}
