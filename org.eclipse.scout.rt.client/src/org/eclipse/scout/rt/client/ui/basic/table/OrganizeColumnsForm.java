/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.TreeMap;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.CloseButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ButtonsBox.SelectAllButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ButtonsBox.SelectNoneButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.ScoutTexts;

public class OrganizeColumnsForm extends AbstractForm {
  ITable m_table;
  TreeMap<String, Boolean> m_origValues = new TreeMap<String, Boolean>();

  public OrganizeColumnsForm(ITable table) throws ProcessingException {
    super(false);
    m_table = table;
    callInitializer();
  }

  @Override
  protected String getConfiguredTitle() {
    return ScoutTexts.get("OrganizeTableColumnsTitle");
  }

  @Override
  protected boolean getConfiguredModal() {
    return true;
  }

  public void startModify() throws ProcessingException {
    startInternal(new ModifyHandler());
  }

  public MainBox getMainBox() {
    return (MainBox) getRootGroupBox();
  }

  public CloseButton getCloseButton() {
    return getFieldByClass(CloseButton.class);
  }

  /*
   * Field accessors
   */
  public ColumnsTableField getColumnsTableField() {
    return getFieldByClass(ColumnsTableField.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public SelectAllButton getSelectAllButton() {
    return getFieldByClass(SelectAllButton.class);
  }

  public SelectNoneButton getSelectNoneButton() {
    return getFieldByClass(SelectNoneButton.class);
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridW() {
      return 1;
    }

    @Order(10)
    public class GroupBox extends AbstractGroupBox {
      @Override
      protected int getConfiguredGridColumnCount() {
        return 2;
      }

      @Order(10)
      public class ColumnsTableField extends AbstractTableField<ColumnsTableField.Table> {
        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected int getConfiguredGridH() {
          return 8;
        }

        @Override
        protected boolean getConfiguredGridUseUiWidth() {
          return true;
        }

        @Override
        protected String getConfiguredLabel() {
          return ScoutTexts.get("Columns");
        }

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected void execReloadTableData() throws ProcessingException {
          ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
          for (IColumn<?> col : m_table.getColumnSet().getAllColumnsInUserOrder()) {
            if (col.isDisplayable()) {
              if (col.isVisible() || col.isVisibleGranted()) {
                IHeaderCell headerCell = col.getHeaderCell();
                TableRow row = new TableRow(getTable().getColumnSet());
                getTable().getCheckBoxColumn().setValue(row, col.isVisible());
                getTable().getKeyColumn().setValue(row, col);
                getTable().getTextColumn().setValue(row, headerCell.getText());
                row.setIconId(headerCell.getIconId());
                rowList.add(row);
              }
            }
          }
          getTable().discardAllRows();
          getTable().addRows(rowList.toArray(new ITableRow[rowList.size()]));
          for (int i = 0; i < getTable().getRowCount(); i++) {
            ITableRow row = getTable().getRow(i);
            Boolean b = (Boolean) row.getCell(getTable().getCheckBoxColumn()).getValue();
            IColumn<?> col = (IColumn<?>) row.getCell(getTable().getKeyColumn()).getValue();
            m_origValues.put(col.getClass().getName(), b);
          }
        }

        @Order(10)
        public class Table extends AbstractTable {
          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          @Override
          protected boolean getConfiguredHeaderVisible() {
            return false;
          }

          @Override
          protected boolean getConfiguredMultiSelect() {
            return false;
          }

          @Override
          protected void execRowClick(ITableRow row) throws ProcessingException {
            if (row != null && getContextColumn() == getCheckBoxColumn()) {
              Boolean oldValue = getCheckBoxColumn().getValue(row);
              getCheckBoxColumn().setValue(row, !oldValue);
            }
          }

          public CheckBoxColumn getCheckBoxColumn() {
            return getColumnSet().getColumnByClass(CheckBoxColumn.class);
          }

          public KeyColumn getKeyColumn() {
            return getColumnSet().getColumnByClass(KeyColumn.class);
          }

          public TextColumn getTextColumn() {
            return getColumnSet().getColumnByClass(TextColumn.class);
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
          public class CheckBoxColumn extends AbstractBooleanColumn {
            @Override
            protected int getConfiguredWidth() {
              return 20;
            }
          }

          @Order(30)
          public class TextColumn extends AbstractStringColumn {
            @Override
            protected int getConfiguredWidth() {
              return 180;
            }
          }

          @Order(10)
          public class SpaceKeyStroke extends AbstractKeyStroke {
            @Override
            protected String getConfiguredKeyStroke() {
              return "space";
            }

            @Override
            protected void execAction() throws ProcessingException {
              for (ITableRow row : getSelectedRows()) {
                Boolean b = getCheckBoxColumn().getValue(row);
                if (b == null) {
                  b = false;
                }
                b = !b;
                getCheckBoxColumn().setValue(row, b);
              }
            }
          }
        }
      }

      @Order(20)
      public class ButtonsBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected boolean getConfiguredBorderVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected int getConfiguredGridH() {
          return 4;
        }

        @Override
        protected double getConfiguredGridWeightY() {
          return 0;
        }

        @Override
        protected double getConfiguredGridWeightX() {
          return 0;
        }

        @Override
        protected boolean getConfiguredGridUseUiWidth() {
          return true;
        }

        @Order(10)
        public class MoveUpButton extends AbstractButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("ButtonMoveUp");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected boolean getConfiguredFillHorizontal() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiWidth() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiHeight() {
            return true;
          }

          @Override
          protected void execClickAction() {
            ITableRow row = getColumnsTableField().getTable().getSelectedRow();
            if (row != null && row.getRowIndex() - 1 >= 0) {
              getColumnsTableField().getTable().moveRow(row.getRowIndex(), row.getRowIndex() - 1);
            }
          }
        }

        @Order(20)
        public class MoveDownButton extends AbstractButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("ButtonMoveDown");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected boolean getConfiguredFillHorizontal() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiWidth() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiHeight() {
            return true;
          }

          @Override
          protected void execClickAction() {
            ITableRow row = getColumnsTableField().getTable().getSelectedRow();
            if (row != null && row.getRowIndex() + 1 < getColumnsTableField().getTable().getRowCount()) {
              getColumnsTableField().getTable().moveRow(row.getRowIndex(), row.getRowIndex() + 1);
            }
          }
        }

        /**
         * Button "select all"
         */
        @Order(30)
        public class SelectAllButton extends AbstractButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("ButtonSelectAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected boolean getConfiguredFillHorizontal() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiWidth() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiHeight() {
            return true;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            getColumnsTableField().getTable().getCheckBoxColumn().fill(true);
          }
        }

        /**
         * Button "select none"
         */
        @Order(40)
        public class SelectNoneButton extends AbstractButton {

          @Override
          protected String getConfiguredLabel() {
            return ScoutTexts.get("ButtonDeselectAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected boolean getConfiguredFillHorizontal() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiWidth() {
            return true;
          }

          @Override
          protected boolean getConfiguredGridUseUiHeight() {
            return true;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            getColumnsTableField().getTable().getCheckBoxColumn().fill(false);
          }
        }
      }
    }

    /**
     * Button "ok"
     */
    @Order(40)
    public class OkButton extends AbstractButton {

      @Override
      protected int getConfiguredSystemType() {
        return IButton.SYSTEM_TYPE_OK;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("OkButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("OkButtonTooltip");
      }
    }

    /**
     * Button "close"
     */
    @Order(50)
    public class CloseButton extends AbstractButton {

      @Override
      protected int getConfiguredSystemType() {
        return IButton.SYSTEM_TYPE_CLOSE;
      }

      @Override
      protected String getConfiguredLabel() {
        return ScoutTexts.get("CloseButton");
      }

      @Override
      protected String getConfiguredTooltipText() {
        return ScoutTexts.get("CloseButtonTooltip");
      }
    }

  }// end main box

  /**
   * Handler for "Modify"
   */
  private class ModifyHandler extends AbstractFormHandler {

    @Override
    protected void execLoad() throws ProcessingException {
      getColumnsTableField().reloadTableData();
    }

    @Override
    protected void execPostLoad() throws ProcessingException {
      touch();
    }

    @Override
    protected void execStore() throws ProcessingException {
      IColumn<?>[] visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues(getColumnsTableField().getTable().getCheckBoxColumn().findRows(true));
      m_table.getColumnSet().setVisibleColumns(visibleColumns);
      // make changes persistent
      ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_table);
    }
  }// end private handler
}
