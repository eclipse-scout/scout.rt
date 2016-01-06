/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.ShowInvisibleColumnsForm.MainBox.GroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public class ShowInvisibleColumnsForm extends AbstractForm implements IShowInvisibleColumnsForm {

  private ITable m_table = null;

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

  @Order(10)
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
    public class GroupBox extends AbstractGroupBox {

      @Order(10)
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
        protected int getConfiguredLabelPosition() {
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
    public class OkButton extends AbstractOkButton {
    }

    @Order(20)
    public class CancelButton extends AbstractCancelButton {
    }

  }

  public class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execStore() {
      for (ITableRow row : getColumnsTableField().getTable().getCheckedRows()) {
        getColumnsTableField().getTable().getKeyColumn().getValue(row).setVisible(true);
      }
    }

    @Override
    protected void execLoad() {
      getColumnsTableField().reloadTableData();
    }

  }

}
