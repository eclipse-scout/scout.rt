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
package org.eclipse.scout.rt.client.ui.basic.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ReplaceTableTest.BaseForm.MainBox.FirstGroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.ReplaceTableTest.BaseForm.MainBox.FirstGroupBox.TableField;
import org.eclipse.scout.rt.client.ui.basic.table.ReplaceTableTest.BaseFormUsingTemplates.MainBox.TabBox;
import org.eclipse.scout.rt.client.ui.basic.table.ReplaceTableTest.BaseFormUsingTemplates.MainBox.TabBox.FirstGroupBox.TableField1;
import org.eclipse.scout.rt.client.ui.basic.table.ReplaceTableTest.BaseFormUsingTemplates.MainBox.TabBox.SecondGroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractLongColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tabbox.AbstractTabBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @since 3.8.2
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class ReplaceTableTest {

  @Test
  public void testColumnId() {
    assertEquals("Base", new BaseColumn().getColumnId());
    assertEquals("Base", new ExtendedTestColumn().getColumnId());
    assertEquals("ExtendedTestColumnWithoutReplace", new ExtendedTestColumnWithoutReplace().getColumnId());
    //
    assertEquals("Custom", new TestColumnWithCustomColumnId().getColumnId());
    assertEquals("Custom", new ExtendedTestColumnWithCustomColumnId().getColumnId());
  }

  @Test
  public void testBaseTable() throws Exception {
    BaseTable table = new BaseTable();

    assertEquals(2, table.getColumnCount());
    assertSame(table.getFirstColumn(), table.getColumnSet().getColumn(0));
    assertSame(table.getSecondColumn(), table.getColumnSet().getColumn(1));
  }

  @Test
  public void testExtendedTable() throws Exception {
    ExtendedTable table = new ExtendedTable();

    assertEquals(3, table.getColumnCount());
    assertSame(table.getFirstColumn(), table.getColumnSet().getColumn(0));
    assertSame(table.getThirdColumn(), table.getColumnSet().getColumn(1));
    assertSame(table.getSecondColumn(), table.getColumnSet().getColumn(2));

    assertSame(table.getFirstExColumn(), table.getFirstColumn());

    assertSame(table.getFirstExColumn(), table.getColumnSet().getColumnById("First"));
    assertEquals("First", table.getFirstColumn().getColumnId());
  }

  @Test
  public void testExtendedExtendedTable() throws Exception {
    ExtendedExtendedTable table = new ExtendedExtendedTable();

    assertEquals(3, table.getColumnCount());
    assertSame(table.getThirdColumn(), table.getColumnSet().getColumn(0));
    assertSame(table.getSecondColumn(), table.getColumnSet().getColumn(1));
    assertSame(table.getFirstColumn(), table.getColumnSet().getColumn(2));

    assertSame(table.getFirstExExColumn(), table.getFirstColumn());
    assertSame(table.getFirstExExColumn(), table.getFirstExColumn());

    assertSame(table.getFirstExExColumn(), table.getColumnSet().getColumnById("First"));
    assertEquals("First", table.getFirstColumn().getColumnId());
    assertEquals("First", table.getFirstExColumn().getColumnId());
  }

  @Test
  public void testBaseForm() throws Exception {
    BaseForm form = new BaseForm();

    assertEquals(1, form.getMainBox().getFieldCount());
    assertEquals(2, form.getTableField().getTable().getColumnCount());

    assertSame(form.getTableField().getTable().getFirstColumn(), form.getTableField().getTable().getColumnSet().getColumn(0));
    assertSame(form.getTableField().getTable().getSecondColumn(), form.getTableField().getTable().getColumnSet().getColumn(1));

    // menus
    assertEquals(3, form.getTableField().getTable().getMenus().size());

    assertSame(form.getTableField().getTable().getFirstMenu(), form.getTableField().getTable().getMenus().get(0));
    assertSame(form.getTableField().getTable().getSecondMenu(), form.getTableField().getTable().getMenus().get(1));
  }

  @Test
  public void testExtendedForm() throws Exception {
    ExtendedForm form = new ExtendedForm();

    assertEquals(1, form.getMainBox().getFieldCount());
    assertEquals(3, form.getTableExField().getTableEx().getColumnCount());

    ColumnSet columnSet = form.getTableField().getTable().getColumnSet();
    assertSame(form.getTableExField().getTableEx().getFirstExColumn(), columnSet.getColumn(0));
    assertSame(form.getTableExField().getTableEx().getThirdColumn(), columnSet.getColumn(1));
    assertSame(form.getTableExField().getTableEx().getSecondColumn(), columnSet.getColumn(2));

    assertSame(form.getTableExField().getTableEx().getFirstExColumn(), columnSet.getColumnByClass(BaseForm.MainBox.FirstGroupBox.TableField.Table.FirstColumn.class));
    assertSame(form.getTableExField().getTableEx().getFirstExColumn(), columnSet.getColumnByClass(ExtendedForm.TableExField.TableEx.FirstExColumn.class));

    assertSame(form.getTableExField().getTableEx().getFirstExColumn(), columnSet.getColumnById("First"));
    assertEquals("First", form.getTableExField().getTableEx().getFirstColumn().getColumnId());

    // menus
    assertEquals(4, form.getTableField().getTable().getMenus().size());

    assertSame(form.getTableExField().getTableEx().getFirstExMenu(), form.getTableField().getTable().getMenus().get(0));
    assertSame(form.getTableExField().getTableEx().getThirdMenu(), form.getTableField().getTable().getMenus().get(1));
    assertSame(form.getTableExField().getTableEx().getSecondMenu(), form.getTableField().getTable().getMenus().get(2));

    assertSame(form.getTableExField().getTableEx().getFirstExMenu(), form.getTableField().getTable().getMenuByClass(BaseForm.MainBox.FirstGroupBox.TableField.Table.FirstMenu.class));
    assertSame(form.getTableExField().getTableEx().getFirstExMenu(), form.getTableField().getTable().getMenuByClass(ExtendedForm.TableExField.TableEx.FirstExMenu.class));
  }

  @Test
  public void testExtendedFormUsingTemplates() throws Exception {
    ExtendedFormUsingTemplates form = new ExtendedFormUsingTemplates();
    assertEquals(1, form.getMainBox().getFieldCount());
    assertEquals(2, form.getTabBox().getFieldCount());

    // Form.MainBox.TabBox.FirstGroupBox.<AbstractTemplateTableField>
    assertEquals(4, form.getTableField1().getTable().getMenus().size());
    assertEquals(4, form.getTableFieldEx1().getTable().getMenus().size());

    assertSame(form.getTableField1().getTable().getFirstMenu(), form.getTableField1().getTable().getMenus().get(0));
    assertSame(form.getTableFieldEx1().getTableEx1().getThirdMenu1(), form.getTableField1().getTable().getMenus().get(1));
    assertSame(form.getTableFieldEx1().getTableEx1().getSecondMenuEx1(), form.getTableField1().getTable().getMenus().get(2));

    assertSame(form.getTableFieldEx1().getTableEx1().getSecondMenuEx1(), form.getTableField1().getTable().getMenuByClass(BaseFormUsingTemplates.MainBox.TabBox.FirstGroupBox.TableField1.Table1.SecondMenu.class));
    assertSame(form.getTableFieldEx1().getTableEx1().getSecondMenuEx1(), form.getTableField1().getTable().getMenuByClass(ExtendedFormUsingTemplates.TableFieldEx1.TableEx1.SecondMenuEx1.class));

    // Form.MainBox.TabBox.<AbstractTableBox>.<AbstractTemplateTableField>
    assertEquals(4, form.getSecondGroupBox().getInnerTableField().getTable().getMenus().size());
    assertEquals(4, form.getInnerTableFieldEx1().getTable().getMenus().size());

    assertSame(form.getInnerTableFieldEx1().getInnerTableEx1().getSecondMenuEx2(), form.getSecondGroupBox().getInnerTableField().getTable().getMenus().get(0));
    assertSame(form.getSecondGroupBox().getInnerTableField().getTable().getFirstMenu(), form.getSecondGroupBox().getInnerTableField().getTable().getMenus().get(1));
    assertSame(form.getInnerTableFieldEx1().getInnerTableEx1().getThirdMenu2(), form.getSecondGroupBox().getInnerTableField().getTable().getMenus().get(2));

    assertSame(form.getInnerTableFieldEx1().getInnerTableEx1().getSecondMenuEx2(),
        form.getSecondGroupBox().getInnerTableField().getTable().getMenuByClass(BaseFormUsingTemplates.MainBox.TabBox.SecondGroupBox.InnerTableField.InnerTable.SecondMenu.class));
    assertSame(form.getInnerTableFieldEx1().getInnerTableEx1().getSecondMenuEx2(), form.getSecondGroupBox().getInnerTableField().getTable().getMenuByClass(ExtendedFormUsingTemplates.InnerTableFieldEx1.InnerTableEx1.SecondMenuEx2.class));
  }

  @Test
  public void testTablePage() throws Exception {
    BaseTablePage tablePage = new BaseTablePage();
    assertEquals(4, tablePage.getTable().getColumnCount());
    assertSame(tablePage.getTable().getFirstColumn(), tablePage.getTable().getColumnSet().getColumn(0));
    assertSame(tablePage.getTable().getSecondColumn(), tablePage.getTable().getColumnSet().getColumn(1));
    assertSame(tablePage.getTable().getThirdColumn(), tablePage.getTable().getColumnSet().getColumn(2));
    assertSame(tablePage.getTable().getForthColumn(), tablePage.getTable().getColumnSet().getColumn(3));

    ExtendedTablePage extendedTablePage = new ExtendedTablePage();
    assertEquals(5, extendedTablePage.getTable().getColumnCount());
    assertSame(extendedTablePage.getTableEx().getSecondColumnEx(), extendedTablePage.getTable().getColumnSet().getVisibleColumn(0));
    assertSame(extendedTablePage.getTableEx().getFirstColumn(), extendedTablePage.getTable().getColumnSet().getVisibleColumn(1));
    assertSame(extendedTablePage.getTableEx().getThirdColumnEx(), extendedTablePage.getTable().getColumnSet().getVisibleColumn(2));
    assertSame(extendedTablePage.getTableEx().getForthColumn(), extendedTablePage.getTable().getColumnSet().getVisibleColumn(3));
    assertSame(extendedTablePage.getTableEx().getFifthColumn(), extendedTablePage.getTable().getColumnSet().getVisibleColumn(4));

    BaseTablePageUsingTemplates tablePageUsingTemplates = new BaseTablePageUsingTemplates();
    assertEquals(5, tablePageUsingTemplates.getTable().getColumnCount());
    assertSame(tablePageUsingTemplates.getTable().getFirstColumn(), tablePageUsingTemplates.getTable().getColumnSet().getColumn(0));
    assertSame(tablePageUsingTemplates.getTable().getSecondColumn(), tablePageUsingTemplates.getTable().getColumnSet().getColumn(1));
    assertSame(tablePageUsingTemplates.getTable().getThirdColumn(), tablePageUsingTemplates.getTable().getColumnSet().getColumn(2));
    assertSame(tablePageUsingTemplates.getTable().getForthColumn(), tablePageUsingTemplates.getTable().getColumnSet().getColumn(3));
    assertSame(tablePageUsingTemplates.getTableBase().getFifthColumn(), tablePageUsingTemplates.getTable().getColumnSet().getColumn(4));

    ExtendedTablePageUsingTemplates extendedTablePageUsingTemplates = new ExtendedTablePageUsingTemplates();
    assertEquals(6, extendedTablePageUsingTemplates.getTable().getColumnCount());
    assertSame(extendedTablePageUsingTemplates.getTableEx().getSecondColumnEx(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(0));
    assertSame(extendedTablePageUsingTemplates.getTableEx().getFirstColumn(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(1));
    assertSame(extendedTablePageUsingTemplates.getTableEx().getThirdColumnEx(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(2));
    assertSame(extendedTablePageUsingTemplates.getTableEx().getForthColumn(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(3));
    assertSame(extendedTablePageUsingTemplates.getTableEx().getFifthColumn(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(4));
    assertSame(extendedTablePageUsingTemplates.getTableEx().getSixthColumn(), extendedTablePageUsingTemplates.getTable().getColumnSet().getVisibleColumn(5));
  }

  public class BaseTable extends AbstractTable {

    public FirstColumn getFirstColumn() {
      return getColumnSet().getColumnByClass(FirstColumn.class);
    }

    public SecondColumn getSecondColumn() {
      return getColumnSet().getColumnByClass(SecondColumn.class);
    }

    @Order(10)
    public class FirstColumn extends AbstractStringColumn {
    }

    @Order(20)
    public class SecondColumn extends AbstractStringColumn {
    }
  }

  public class ExtendedTable extends BaseTable {

    public FirstExColumn getFirstExColumn() {
      return getColumnSet().getColumnByClass(FirstExColumn.class);
    }

    public ThirdColumn getThirdColumn() {
      return getColumnSet().getColumnByClass(ThirdColumn.class);
    }

    @Replace
    public class FirstExColumn extends FirstColumn {
    }

    @Order(15)
    public class ThirdColumn extends AbstractStringColumn {
    }
  }

  public class ExtendedExtendedTable extends ExtendedTable {

    public FirstExExColumn getFirstExExColumn() {
      return getColumnSet().getColumnByClass(FirstExExColumn.class);
    }

    @Replace
    @Order(50)
    public class FirstExExColumn extends FirstExColumn {
    }
  }

  public static class BaseForm extends AbstractForm {

    public BaseForm() {
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    public FirstGroupBox getFirstGroupBox() {
      return getFieldByClass(FirstGroupBox.class);
    }

    public TableField getTableField() {
      return getFieldByClass(TableField.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class FirstGroupBox extends AbstractGroupBox {

        @Order(10)
        public class TableField extends AbstractTableField<TableField.Table> {

          public class Table extends AbstractTable {

            public FirstColumn getFirstColumn() {
              return getColumnSet().getColumnByClass(FirstColumn.class);
            }

            public SecondColumn getSecondColumn() {
              return getColumnSet().getColumnByClass(SecondColumn.class);
            }

            public FirstMenu getFirstMenu() {
              return getMenuByClass(FirstMenu.class);
            }

            public SecondMenu getSecondMenu() {
              return getMenuByClass(SecondMenu.class);
            }

            @Order(10)
            public class FirstColumn extends AbstractStringColumn {
            }

            @Order(20)
            public class SecondColumn extends AbstractStringColumn {
            }

            @Order(10)
            public class FirstMenu extends AbstractMenu {
            }

            @Order(20)
            public class SecondMenu extends AbstractMenu {
            }
          }
        }
      }
    }
  }

  public static class ExtendedForm extends BaseForm {

    public ExtendedForm() {
    }

    public TableExField getTableExField() {
      return getFieldByClass(TableExField.class);
    }

    @Replace
    public class TableExField extends BaseForm.MainBox.FirstGroupBox.TableField {

      public TableExField(BaseForm.MainBox.FirstGroupBox container) {
        container.super();
      }

      public TableEx getTableEx() {
        return (TableEx) getTable();
      }

      public class TableEx extends Table {

        public FirstExColumn getFirstExColumn() {
          return getColumnSet().getColumnByClass(FirstExColumn.class);
        }

        public ThirdColumn getThirdColumn() {
          return getColumnSet().getColumnByClass(ThirdColumn.class);
        }

        public FirstExMenu getFirstExMenu() {
          return getMenuByClass(FirstExMenu.class);
        }

        public ThirdMenu getThirdMenu() {
          return getMenuByClass(ThirdMenu.class);
        }

        @Replace
        public class FirstExColumn extends FirstColumn {
        }

        @Order(15)
        public class ThirdColumn extends AbstractStringColumn {
        }

        @Replace
        public class FirstExMenu extends FirstMenu {
        }

        @Order(15)
        public class ThirdMenu extends AbstractMenu {
        }
      }
    }
  }

  public static class BaseColumn extends AbstractStringColumn {
  }

  @Replace
  public static class ExtendedTestColumn extends BaseColumn {
  }

  public static class ExtendedTestColumnWithoutReplace extends BaseColumn {
  }

  public static class TestColumnWithCustomColumnId extends AbstractColumn<Object> {
    @Override
    public String getColumnId() {
      return "Custom";
    }
  }

  @Replace
  public static class ExtendedTestColumnWithCustomColumnId extends TestColumnWithCustomColumnId {
  }

  public abstract static class AbstractTemplateTableField<TABLE extends AbstractTemplateTableField<TABLE>.Table> extends AbstractTableField<TABLE> {

    public class Table extends AbstractTable {

      @SuppressWarnings("unchecked")
      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      @SuppressWarnings("unchecked")
      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      @SuppressWarnings("unchecked")
      public FirstMenu getFirstMenu() {
        return getMenuByClass(FirstMenu.class);
      }

      @SuppressWarnings("unchecked")
      public SecondMenu getSecondMenu() {
        return getMenuByClass(SecondMenu.class);
      }

      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
      }

      @Order(10)
      public class FirstMenu extends AbstractMenu {
      }

      @Order(20)
      public class SecondMenu extends AbstractMenu {
      }
    }
  }

  public abstract static class AbstractTableBox extends AbstractGroupBox {

    public InnerTableField getInnerTableField() {
      return getFieldByClass(InnerTableField.class);
    }

    @Order(10)
    public class InnerTableField extends AbstractTemplateTableField<InnerTableField.InnerTable> {
      public class InnerTable extends AbstractTemplateTableField<InnerTableField.InnerTable>.Table {
      }
    }
  }

  public static class BaseFormUsingTemplates extends AbstractForm {

    public BaseFormUsingTemplates() {
    }

    public MainBox getMainBox() {
      return (MainBox) getRootGroupBox();
    }

    public TabBox getTabBox() {
      return getFieldByClass(TabBox.class);
    }

    public FirstGroupBox getFirstGroupBox() {
      return getFieldByClass(FirstGroupBox.class);
    }

    public SecondGroupBox getSecondGroupBox() {
      return getFieldByClass(SecondGroupBox.class);
    }

    public TableField1 getTableField1() {
      return getFieldByClass(TableField1.class);
    }

    @Order(10)
    public class MainBox extends AbstractGroupBox {
      @Order(10)
      public class TabBox extends AbstractTabBox {
        @Order(10)
        public class FirstGroupBox extends AbstractGroupBox {

          @Order(10)
          public class TableField1 extends AbstractTemplateTableField<TableField1.Table1> {
            public class Table1 extends AbstractTemplateTableField<TableField1.Table1>.Table {
            }
          }
        }

        @Order(20)
        public class SecondGroupBox extends AbstractTableBox {
        }
      }
    }
  }

  public static class ExtendedFormUsingTemplates extends BaseFormUsingTemplates {

    public ExtendedFormUsingTemplates() {
    }

    public TableFieldEx1 getTableFieldEx1() {
      return getFieldByClass(TableFieldEx1.class);
    }

    public InnerTableFieldEx1 getInnerTableFieldEx1() {
      return getFieldByClass(InnerTableFieldEx1.class);
    }

    @Replace
    public class TableFieldEx1 extends MainBox.TabBox.FirstGroupBox.TableField1 {

      public TableFieldEx1(MainBox.TabBox.FirstGroupBox container) {
        container.super();
      }

      public TableEx1 getTableEx1() {
        return (TableEx1) getTable();
      }

      @Replace
      public class TableEx1 extends MainBox.TabBox.FirstGroupBox.TableField1.Table1 {

        public SecondMenuEx1 getSecondMenuEx1() {
          return getMenuByClass(SecondMenuEx1.class);
        }

        public ThirdMenu1 getThirdMenu1() {
          return getMenuByClass(ThirdMenu1.class);
        }

        @Order(160)
        @Replace
        public class SecondMenuEx1 extends MainBox.TabBox.FirstGroupBox.TableField1.Table1.SecondMenu {

          @Override
          protected String getConfiguredText() {
            return "SecondMenuEx1";
          }
        }

        @Order(30)
        public class ThirdMenu1 extends AbstractMenu {
        }
      }
    }

    @Replace
    public class InnerTableFieldEx1 extends MainBox.TabBox.SecondGroupBox.InnerTableField {

      public InnerTableFieldEx1(MainBox.TabBox.SecondGroupBox container) {
        container.super();
      }

      public InnerTableEx1 getInnerTableEx1() {
        return (InnerTableEx1) getTable();
      }

      @Replace
      public class InnerTableEx1 extends MainBox.TabBox.SecondGroupBox.InnerTableField.InnerTable {

        public SecondMenuEx2 getSecondMenuEx2() {
          return getMenuByClass(SecondMenuEx2.class);
        }

        public ThirdMenu2 getThirdMenu2() {
          return getMenuByClass(ThirdMenu2.class);
        }

        @Order(1)
        @Replace
        public class SecondMenuEx2 extends MainBox.TabBox.SecondGroupBox.InnerTableField.InnerTable.SecondMenu {

          @Override
          protected String getConfiguredText() {
            return "SecondMenuEx2";
          }
        }

        @Order(30)
        public class ThirdMenu2 extends AbstractMenu {
        }
      }
    }
  }

  public static class BaseTablePage extends AbstractPageWithTable<BaseTablePage.Table> {

    public class Table extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      public ThirdColumn getThirdColumn() {
        return getColumnSet().getColumnByClass(ThirdColumn.class);
      }

      public ForthColumn getForthColumn() {
        return getColumnSet().getColumnByClass(ForthColumn.class);
      }

      @Order(10)
      public class FirstColumn extends AbstractLongColumn {
      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
      }

      @Order(30)
      public class ThirdColumn extends AbstractStringColumn {
      }

      @Order(40)
      public class ForthColumn extends AbstractStringColumn {
      }
    }
  }

  public static class ExtendedTablePage extends BaseTablePage {

    public TableEx getTableEx() {
      return (TableEx) getTable();
    }

    public class TableEx extends BaseTablePage.Table {

      public SecondColumnEx getSecondColumnEx() {
        return getColumnSet().getColumnByClass(SecondColumnEx.class);
      }

      public ThirdColumnEx getThirdColumnEx() {
        return getColumnSet().getColumnByClass(ThirdColumnEx.class);
      }

      public FifthColumn getFifthColumn() {
        return getColumnSet().getColumnByClass(FifthColumn.class);
      }

      @Replace
      @Order(1)
      // display at the first position
      public class SecondColumnEx extends SecondColumn {
      }

      @Replace
      public class ThirdColumnEx extends ThirdColumn {
      }

      @Order(60)
      public class FifthColumn extends AbstractStringColumn {
      }
    }
  }

  public abstract static class AbstractTemplateTablePage extends AbstractPageWithTable<AbstractTemplateTablePage.Table> {
    public class Table extends AbstractTable {

      public FirstColumn getFirstColumn() {
        return getColumnSet().getColumnByClass(FirstColumn.class);
      }

      public SecondColumn getSecondColumn() {
        return getColumnSet().getColumnByClass(SecondColumn.class);
      }

      public ThirdColumn getThirdColumn() {
        return getColumnSet().getColumnByClass(ThirdColumn.class);
      }

      public ForthColumn getForthColumn() {
        return getColumnSet().getColumnByClass(ForthColumn.class);
      }

      @Order(10)
      public class FirstColumn extends AbstractLongColumn {

        @Override
        protected boolean getConfiguredPrimaryKey() {
          return true;
        }
      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
      }

      @Order(30)
      public class ThirdColumn extends AbstractStringColumn {
      }

      @Order(40)
      public class ForthColumn extends AbstractStringColumn {
      }
    }
  }

  public static class BaseTablePageUsingTemplates extends AbstractTemplateTablePage {
    public TableBase getTableBase() {
      return (TableBase) getTable();
    }

    public class TableBase extends AbstractTemplateTablePage.Table {

      public FifthColumn getFifthColumn() {
        return getColumnSet().getColumnByClass(FifthColumn.class);
      }

      @Order(50)
      public class FifthColumn extends AbstractStringColumn {
      }
    }
  }

  public static class ExtendedTablePageUsingTemplates extends BaseTablePageUsingTemplates {

    public TableEx getTableEx() {
      return (TableEx) getTable();
    }

    public class TableEx extends BaseTablePageUsingTemplates.TableBase {

      public SecondColumnEx getSecondColumnEx() {
        return getColumnSet().getColumnByClass(SecondColumnEx.class);
      }

      public ThirdColumnEx getThirdColumnEx() {
        return getColumnSet().getColumnByClass(ThirdColumnEx.class);
      }

      public SixthColumn getSixthColumn() {
        return getColumnSet().getColumnByClass(SixthColumn.class);
      }

      @Replace
      @Order(1)
      public class SecondColumnEx extends SecondColumn {
      }

      @Replace
      public class ThirdColumnEx extends ThirdColumn {
      }

      @Order(60)
      public class SixthColumn extends AbstractStringColumn {
      }
    }
  }
}
