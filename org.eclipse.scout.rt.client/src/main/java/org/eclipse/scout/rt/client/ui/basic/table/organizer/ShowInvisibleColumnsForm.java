/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IDisplayParent;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ShowInvisibleColumnsForm.MainBox.GroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

@ClassId("2d5554d8-68f1-49ab-a0aa-638f3de3e1fe")
public class ShowInvisibleColumnsForm extends AbstractForm implements IShowInvisibleColumnsForm {

  private ITable m_table = null;

  private IColumn<?> m_insertAfterColumn = null;

  public ShowInvisibleColumnsForm(ITable table) {
    m_table = table;
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("ShowColumns");
  }

  @Override
  public void startModify() {
    startInternal(new ModifyHandler());
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public ColumnsTableField getColumnsTableField() {
    return getFieldByClass(ColumnsTableField.class);
  }

  /**
   * Configures if for non-root entity selection, multiple attribute may be selected or not.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if more then one attribute can be checked on a non-root entity, {@code false} otherwise.
   */
  protected boolean getConfiguredAllowMultiAttributeSelect() {
    return true;
  }

  protected int getConfiguredMainBoxGridColumnCount() {
    return 1;
  }

  @Override
  protected IDisplayParent getConfiguredDisplayParent() {
    return getDesktop();
  }

  @Order(10)
  @ClassId("60f3556f-6252-4933-9c35-342a7e0684ae")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return getConfiguredMainBoxGridColumnCount();
    }

    @Override
    protected void execInitField() {
      super.execInitField();
      setStatusVisible(false);
    }

    @Order(10)
    @ClassId("ee038f0e-bd8a-4ab5-9af6-fbb2e6a7ff00")
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
      @ClassId("9aa78206-cbf3-406f-a8c5-0783bfd1de9b")
      public class ColumnsTableField extends AbstractTableField<ColumnsTableField.Table> {
        @Override
        protected int getConfiguredGridH() {
          return 6;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected byte getConfiguredLabelPosition() {
          return LABEL_POSITION_TOP;
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected boolean getConfiguredStatusVisible() {
          return false;
        }

        @Override
        protected void execReloadTableData() {
          List<ITableRow> rowList = new ArrayList<>();
          for (IColumn<?> col : m_table.getColumnSet().getAllColumnsInUserOrder()) {
            if (col.isDisplayable() && col.isVisibleGranted() && !col.isVisible()) {
              IHeaderCell headerCell = col.getHeaderCell();
              TableRow row = new TableRow(getTable().getColumnSet());

              // Key
              getTable().getKeyColumn().setValue(row, col);

              // Column Title
              String columnTitle = headerCell.getText();
              if (StringUtility.isNullOrEmpty(columnTitle)) {
                columnTitle = headerCell.getTooltipText();
                row.setFont(FontSpec.parse("ITALIC"));
              }
              getTable().getTitleColumn().setValue(row, columnTitle);

              rowList.add(row);
            }
          }
          try {
            getTable().setTableChanging(true);
            getTable().discardAllRows();
            rowList = getTable().addRows(rowList);

          }
          finally {
            getTable().setTableChanging(false);
          }
        }

        @ClassId("251571e3-58b2-4711-91ae-3d438988481e")
        public class Table extends AbstractTable {

          @Override
          protected boolean getConfiguredCheckable() {
            return true;
          }

          @Override
          protected boolean getConfiguredHeaderVisible() {
            return false;
          }

          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          public KeyColumn getKeyColumn() {
            return getColumnSet().getColumnByClass(KeyColumn.class);
          }

          public TitleColumn getTitleColumn() {
            return getColumnSet().getColumnByClass(TitleColumn.class);
          }

          @Order(10)
          @ClassId("da373e46-147f-4298-99ff-34c5f83c4029")
          public class KeyColumn extends AbstractColumn<IColumn<?>> {

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
          @ClassId("fdef9323-d352-4802-9069-d6a3005a8f15")
          public class TitleColumn extends AbstractStringColumn {

            @Override
            protected int getConfiguredWidth() {
              return 200;
            }

          }

        }

      }
    }

    @Order(15)
    @ClassId("44339c5f-8885-4f3a-bd6d-8996ad9900a9")
    public class OkButton extends AbstractOkButton {
    }

    @Order(20)
    @ClassId("f6b3f35e-f935-4cd3-94cf-a085ab5dd6b2")
    public class CancelButton extends AbstractCancelButton {
    }

  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execStore() {
      ArrayList<IColumn<?>> newCols = new ArrayList<>();
      for (ITableRow row : getColumnsTableField().getTable().getCheckedRows()) {
        IColumn<?> col = getColumnsTableField().getTable().getKeyColumn().getValue(row);
        newCols.add(col);
        col.setVisible(true);
      }
      if (m_insertAfterColumn == null || newCols.size() == 0) {
        return;
      }
      ColumnSet colSet = newCols.get(0).getTable().getColumnSet();
      List<IColumn<?>> newOrder = new ArrayList<>();
      List<IColumn<?>> visibleColumns = colSet.getVisibleColumns();
      int position = 0;
      int posInsertAfter = -1;
      int newPosFirst = -1;
      int newPosLast = -1;
      for (IColumn<?> col : visibleColumns) {
        if (newCols.contains(col)) {
          if (posInsertAfter != -1 && posInsertAfter < newOrder.size() - 1) {
            newPosFirst = newPosFirst == -1 ? posInsertAfter + 1 : newPosFirst;
            newPosLast = newPosLast == -1 ? posInsertAfter + 1 : newPosLast + 1;
            newOrder.add(newPosLast, col);
          }
          else {
            newOrder.add(col);
            newPosLast = position;
            newPosFirst = newPosFirst == -1 ? position : newPosFirst;
          }
        }
        else if (newPosFirst != -1 && posInsertAfter == -1) {
          newOrder.add(newPosFirst, col);
          posInsertAfter = m_insertAfterColumn.equals(col) ? newPosFirst : posInsertAfter;
          newPosFirst++;
          newPosLast++;
        }
        else {
          newOrder.add(col);
          posInsertAfter = m_insertAfterColumn.equals(col) ? position : posInsertAfter;
        }
        position++;
      }
      int i = 0;
      for (IColumn<?> col : newOrder) {
        col.setVisibleColumnIndexHint(i);
        i++;
      }
      colSet.setVisibleColumns(newOrder);
      ClientUIPreferences.getInstance().setAllTableColumnPreferences(newCols.get(0).getTable());

    }

    @Override
    protected void execLoad() {
      getColumnsTableField().reloadTableData();
    }

  }

  @Override
  public IShowInvisibleColumnsForm withInsertAfterColumn(IColumn<?> insertAfterColumn) {
    m_insertAfterColumn = insertAfterColumn;
    return this;
  }

}
