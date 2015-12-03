/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.form;

import static org.junit.Assert.assertArrayEquals;

import java.io.Serializable;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractObjectColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.form.StoreAndLoadXml2FormTest.TestForm.MainBox.GroupBox.TableField;
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
public class StoreAndLoadXml2FormTest {

  static final Object[][] TABLE_DATA = new Object[][]{
      new Object[]{1L, "One", new java.util.Date()},
      new Object[]{2L, "Two", new StoreAndLoadXml2FormTest.InnerClass()},
      new Object[]{3L, "Three", new StoreAndLoadXml2FormTest.InnerClass.InnerInnerClass()}
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

  public static class TestForm extends AbstractForm {

    public TestForm() {
      super();
    }

    @Override
    protected String getConfiguredTitle() {
      return "TestForm";
    }

    public void startModify() {
      startInternal(new ModifyHandler());
    }

    public TableField getTableField() {
      return getFieldByClass(TableField.class);
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
        public class TableField extends AbstractTableField<TableField.Table> {

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

          public class Table extends AbstractTable {

            public KeyColumn getKeyColumn() {
              return getColumnSet().getColumnByClass(KeyColumn.class);
            }

            public StringColumn getStringColumn() {
              return getColumnSet().getColumnByClass(StringColumn.class);
            }

            public CustomColumn getCustomColumn() {
              return getColumnSet().getColumnByClass(CustomColumn.class);
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
        getTableField().reloadTableData();
      }
    }
  }

  @Test
  public void test() throws Throwable {
    TestForm f = new TestForm();
    try {
      f.startModify();
      assertArrayEquals(TABLE_DATA, f.getTableField().getTable().getTableData());
      //store xml and clear
      String xml = f.storeToXmlString();
      f.getTableField().getTable().discardAllRows();
      assertArrayEquals(new Object[0][0], f.getTableField().getTable().getTableData());
      //load xml
      f.loadFromXmlString(xml);
      assertArrayEquals(TABLE_DATA, f.getTableField().getTable().getTableData());
    }
    finally {
      f.doClose();
    }
  }

}
