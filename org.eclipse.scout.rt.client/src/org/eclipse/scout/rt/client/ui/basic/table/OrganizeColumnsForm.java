package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.BooleanUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.CancelButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnSortingBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnSortingBox.AscendingButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnSortingBox.DescendingButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnSortingBox.WithoutButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.FilterBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.FilterBox.EditFilterButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.FilterBox.RemoveFilterButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ResetBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ResetBox.ResetAllButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ResetBox.ResetColumnFiltersButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ResetBox.ResetSortingButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ResetBox.ResetVisibilityButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.AddCustomColumnButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.DeselectAllButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.ModifyCustomColumnButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.MoveDownButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.MoveUpButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.RemoveCustomColumnButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ViewBox.SelectAllButton;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.OkButton;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilter;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu.ResetAllMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu.ResetColumnFiltersMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu.ResetSortingMenu;
import org.eclipse.scout.rt.client.ui.basic.table.menus.ResetColumnsMenu.ResetVisibilityMenu;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.AbstractFormHandler;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractCancelButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractLinkButton;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractOkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.services.common.bookmark.Bookmark;

public class OrganizeColumnsForm extends AbstractForm {

  ITable m_table;

  public OrganizeColumnsForm(ITable table) throws ProcessingException {
    super(false);
    m_table = table;
    callInitializer();
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("OrganizeTableColumnsTitle");
  }

  public AddCustomColumnButton getAddCustomColumnButton() {
    return getFieldByClass(AddCustomColumnButton.class);
  }

  public AscendingButton getAscendingButton() {
    return getFieldByClass(AscendingButton.class);
  }

  public CancelButton getCancelButton() {
    return getFieldByClass(CancelButton.class);
  }

  public void startModify() throws ProcessingException {
    startInternal(new OrganizeColumnsForm.ModifyHandler());
  }

  public ColumnSortingBox getColumnSortingBox() {
    return getFieldByClass(ColumnSortingBox.class);
  }

  public ColumnsTableField getColumnsTableField() {
    return getFieldByClass(ColumnsTableField.class);
  }

  public DescendingButton getDescendingButton() {
    return getFieldByClass(DescendingButton.class);
  }

  public DeselectAllButton getDeselectAllButton() {
    return getFieldByClass(DeselectAllButton.class);
  }

  public EditFilterButton getEditFilterButton() {
    return getFieldByClass(EditFilterButton.class);
  }

  public RemoveFilterButton getRemoveFilterButton() {
    return getFieldByClass(RemoveFilterButton.class);
  }

  public FilterBox getFilterBox() {
    return getFieldByClass(FilterBox.class);
  }

  public GroupBox getGroupBox() {
    return getFieldByClass(GroupBox.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public ModifyCustomColumnButton getModifyCustomColumnButton() {
    return getFieldByClass(ModifyCustomColumnButton.class);
  }

  public MoveDownButton getMoveDownButton() {
    return getFieldByClass(MoveDownButton.class);
  }

  public MoveUpButton getMoveUpButton() {
    return getFieldByClass(MoveUpButton.class);
  }

  public OkButton getOkButton() {
    return getFieldByClass(OkButton.class);
  }

  public RemoveCustomColumnButton getRemoveCustomColumnButton() {
    return getFieldByClass(RemoveCustomColumnButton.class);
  }

  public ResetAllButton getResetAllButton() {
    return getFieldByClass(ResetAllButton.class);
  }

  public ResetBox getResetBox() {
    return getFieldByClass(ResetBox.class);
  }

  public ResetColumnFiltersButton getResetColumnFilters() {
    return getFieldByClass(ResetColumnFiltersButton.class);
  }

  public ResetSortingButton getResetSortingButton() {
    return getFieldByClass(ResetSortingButton.class);
  }

  public ResetVisibilityButton getResetVisibilityButton() {
    return getFieldByClass(ResetVisibilityButton.class);
  }

  public SelectAllButton getSelectAllButton() {
    return getFieldByClass(SelectAllButton.class);
  }

  public ViewBox getViewBox() {
    return getFieldByClass(ViewBox.class);
  }

  public WithoutButton getWithoutButton() {
    return getFieldByClass(WithoutButton.class);
  }

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredGridColumnCount() {
      return 3;
    }

    @Override
    protected int getConfiguredGridW() {
      return 2;
    }

    @Override
    protected int getConfiguredWidthInPixel() {
      return 520;
    }

    @Order(10.0)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected int getConfiguredGridW() {
        return 1;
      }

      @Order(10.0)
      public class ColumnsTableField extends AbstractTableField<ColumnsTableField.Table> {

        @Override
        protected int getConfiguredGridH() {
          return 5;
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Columns");
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

                // Key
                getTable().getKeyColumn().setValue(row, col);

                // Visible
                getTable().getVisibleColumn().setValue(row, col.isVisible());

                // Column Title
                getTable().getTitleColumn().setValue(row, headerCell.getText());
                if (Platform.inDevelopmentMode() && col.isSortActive()) {
                  getTable().getTitleColumn().setValue(row, headerCell.getText() + " (" + col.getSortIndex() + ")");
                }

                // Custom Column
                if (col instanceof ICustomColumn<?>) {
                  row.getCellForUpdate(getTable().getCustomColumnColumn().getColumnIndex()).setIconId(AbstractIcons.TableCustomColumn);
                }

                // Sorting
                getTable().getSortingColumn().setValue(row, col);

                // Filter
                if (col.isColumnFilterActive()) {
                  row.getCellForUpdate(getTable().getFilterColumn().getColumnIndex()).setIconId(AbstractIcons.TableColumnFilterActive);
                }

                rowList.add(row);
              }
            }
          }
          try {
            getTable().setTableChanging(true);
            getTable().discardAllRows();
            getTable().addRows(rowList.toArray(new ITableRow[rowList.size()]));
          }
          finally {
            getTable().setTableChanging(false);
          }
        }

        @Order(10.0)
        public class Table extends AbstractTable {

          @Override
          protected int getConfiguredDragType() {
            return IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER;
          }

          @Override
          protected int getConfiguredDropType() {
            return IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER;
          }

          @Override
          protected TransferObject execDrag(ITableRow[] rows) throws ProcessingException {
            return new JavaTransferObject(rows);
          }

          @Override
          protected void execDrop(ITableRow row, TransferObject transfer) throws ProcessingException {
            if (transfer != null && transfer instanceof JavaTransferObject) {
              Object localObject = ((JavaTransferObject) transfer).getLocalObject();
              if (localObject != null) {
                if (localObject instanceof ITableRow[]) {
                  ITableRow[] draggedRows = (ITableRow[]) localObject;
                  if (draggedRows != null && draggedRows.length > 0) {
                    ITableRow draggedRow = draggedRows[0];
                    if (draggedRow.getRowIndex() != row.getRowIndex()) {
                      // target row other than source row
                      try {
                        getTable().setTableChanging(true);
                        if (draggedRow.getRowIndex() < row.getRowIndex()) {
                          while (draggedRow.getRowIndex() <= row.getRowIndex()) {
                            moveDown(draggedRow);
                          }
                        }
                        else {
                          while (draggedRow.getRowIndex() >= row.getRowIndex()) {
                            moveUp(draggedRow);
                          }
                        }
                        updateColumnVisibilityAndOrder();
                      }
                      finally {
                        getTable().setTableChanging(false);
                      }
                    }
                  }
                }
              }
            }
          }

          @Override
          protected boolean getConfiguredAutoResizeColumns() {
            return true;
          }

          @Override
          protected boolean getConfiguredMultiSelect() {
            return false;
          }

          @Override
          protected boolean getConfiguredHeaderVisible() {
            return false;
          }

          public KeyColumn getKeyColumn() {
            return getColumnSet().getColumnByClass(KeyColumn.class);
          }

          public SortingColumn getSortingColumn() {
            return getColumnSet().getColumnByClass(SortingColumn.class);
          }

          public FilterColumn getFilterColumn() {
            return getColumnSet().getColumnByClass(FilterColumn.class);
          }

          public CustomColumnColumn getCustomColumnColumn() {
            return getColumnSet().getColumnByClass(CustomColumnColumn.class);
          }

          public TitleColumn getTitleColumn() {
            return getColumnSet().getColumnByClass(TitleColumn.class);
          }

          public VisibleColumn getVisibleColumn() {
            return getColumnSet().getColumnByClass(VisibleColumn.class);
          }

          @Override
          protected void execRowClick(ITableRow row) throws ProcessingException {
            if (row != null && getContextColumn() == getVisibleColumn() && getKeyColumn().getValue(row) != null) {
              Boolean oldValue = getVisibleColumn().getValue(row);
              setColumnVisible(row, !oldValue);
            }
          }

          @Override
          protected void execRowsSelected(ITableRow[] rows) throws ProcessingException {
            validateButtons();
          }

          @Order(10.0)
          public class SpaceKeyStroke extends AbstractKeyStroke {
            @Override
            protected String getConfiguredKeyStroke() {
              return "space";
            }

            @Override
            protected void execAction() throws ProcessingException {
              for (ITableRow row : getSelectedRows()) {
                Boolean oldValue = BooleanUtility.nvl(getVisibleColumn().getValue(row));
                setColumnVisible(row, !oldValue);
              }
            }
          }

          @Order(20.0)
          public class UpKeyStroke extends AbstractKeyStroke {
            @Override
            protected String getConfiguredKeyStroke() {
              return "alt-up";
            }

            @Override
            protected void execAction() throws ProcessingException {
              moveUp(getSelectedRow());
            }
          }

          @Order(30.0)
          public class DownKeyStroke extends AbstractKeyStroke {
            @Override
            protected String getConfiguredKeyStroke() {
              return "alt-down";
            }

            @Override
            protected void execAction() throws ProcessingException {
              moveDown(getSelectedRow());
            }
          }

          @Order(10.0)
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

          @Order(20.0)
          public class VisibleColumn extends AbstractBooleanColumn {

            @Override
            protected String getConfiguredHeaderText() {
              return TEXTS.get("Visible");
            }

            @Override
            protected int getConfiguredWidth() {
              return 20;
            }

          }

          @Order(30.0)
          public class TitleColumn extends AbstractStringColumn {

            @Override
            protected String getConfiguredHeaderText() {
              return TEXTS.get("Title");
            }

            @Override
            protected int getConfiguredWidth() {
              return 300;
            }

          }

          @Order(40.0)
          public class SortingColumn extends AbstractSortOrderColumn {

            @Override
            protected String getConfiguredHeaderText() {
              return TEXTS.get("ColumnSorting");
            }

            @Override
            protected int getConfiguredWidth() {
              return 20;
            }

          }

          @Order(50.0)
          public class FilterColumn extends AbstractStringColumn {

            @Override
            protected String getConfiguredHeaderText() {
              return TEXTS.get("ResetTableColumnFilter");
            }

            @Override
            protected int getConfiguredWidth() {
              return 20;
            }

          }

          @Order(60.0)
          public class CustomColumnColumn extends AbstractStringColumn {

            @Override
            protected int getConfiguredWidth() {
              return 20;
            }

          }

        }
      }

      @Order(20.0)
      public class ViewBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("ResetTableColumnsVisibility");
        }

        @Order(10.0)
        public class SelectAllButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ButtonSelectAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            for (ITableRow row : getColumnsTableField().getTable().getRows()) {
              setColumnVisible(row, true);
            }
          }

        }

        @Order(20.0)
        public class DeselectAllButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ButtonDeselectAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            for (ITableRow row : getColumnsTableField().getTable().getRows()) {
              setColumnVisible(row, false);
            }
          }

        }

        @Order(30.0)
        public class MoveUpButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ButtonMoveUp");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            moveUp(getColumnsTableField().getTable().getSelectedRow());
          }

        }

        @Order(40.0)
        public class MoveDownButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ButtonMoveDown");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() {
            moveDown(getColumnsTableField().getTable().getSelectedRow());
          }

        }

        @Order(50.0)
        public class AddCustomColumnButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("AddCustomColumnMenu");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execInitField() throws ProcessingException {
            setVisible(m_table.getTableCustomizer() != null);
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            if (m_table != null) {
              if (m_table.getTableCustomizer() != null) {
                ArrayList<String> existingColumns = new ArrayList<String>();
                for (IColumn c : m_table.getColumns()) {
                  existingColumns.add(c.getColumnId());
                }
                m_table.getTableCustomizer().addColumn();

                // find target row (selected row or first row)
                ITableRow targetOrderRow = getColumnsTableField().getTable().getSelectedRow();
                if (targetOrderRow == null && getColumnsTableField().getTable().getRowCount() > 0) {
                  targetOrderRow = getColumnsTableField().getTable().getRow(0);
                }
                if (targetOrderRow == null) {
                  return;
                }

                // find new row
                getColumnsTableField().reloadTableData();
                for (ITableRow newColumnRow : getColumnsTableField().getTable().getRows()) {
                  if (!existingColumns.contains(getColumnsTableField().getTable().getKeyColumn().getValue(newColumnRow).getColumnId())) {
                    // move new column
                    try {
                      getColumnsTableField().getTable().setTableChanging(true);
                      if (newColumnRow.getRowIndex() < targetOrderRow.getRowIndex()) {
                        while (newColumnRow.getRowIndex() < targetOrderRow.getRowIndex()) {
                          moveDown(newColumnRow);
                        }
                      }
                      else {
                        while (newColumnRow.getRowIndex() > targetOrderRow.getRowIndex()) {
                          moveUp(newColumnRow);
                        }
                      }
                      updateColumnVisibilityAndOrder();

                      // select new row
                      getColumnsTableField().getTable().selectRow(newColumnRow);
                    }
                    finally {
                      getColumnsTableField().getTable().setTableChanging(false);
                    }
                    break;
                  }
                }
              }
            }
          }

        }

        @Order(60.0)
        public class ModifyCustomColumnButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("SC_Label_Change");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            if (m_table != null) {
              if (m_table.getTableCustomizer() != null) {
                if (getColumnsTableField().getTable().getSelectedRow() != null) {
                  IColumn<?> selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(getColumnsTableField().getTable().getSelectedRow());
                  if (selectedCol instanceof ICustomColumn<?>) {
                    m_table.getTableCustomizer().modifyColumn((ICustomColumn<?>) selectedCol);
                  }
                }
              }
            }
            getColumnsTableField().reloadTableData();
          }

        }

        @Order(70.0)
        public class RemoveCustomColumnButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Remove");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            if (m_table != null) {
              if (m_table.getTableCustomizer() != null) {
                if (getColumnsTableField().getTable().getSelectedRow() != null) {
                  IColumn<?> selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(getColumnsTableField().getTable().getSelectedRow());
                  if (selectedCol instanceof ICustomColumn<?>) {
                    m_table.getTableCustomizer().removeColumn((ICustomColumn<?>) selectedCol);
                  }
                }
              }
            }
            getColumnsTableField().reloadTableData();
          }

        }
      }

      @Order(30.0)
      public class ColumnSortingBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("ColumnSorting");
        }

        @Order(10.0)
        public class AscendingButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Ascending");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            setSort(true);
          }

        }

        @Order(20.0)
        public class DescendingButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Descending");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            setSort(false);
          }

        }

        @Order(30.0)
        public class WithoutButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Without");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            setSort(null);
          }

        }
      }

      @Order(40.0)
      public class FilterBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("ResetTableColumnFilter");
        }

        @Order(10.0)
        public class EditFilterButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("EditFilterMenu");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            Integer selectedIndex = null;
            if (m_table != null && getColumnsTableField().getTable().getSelectedRow() != null) {
              selectedIndex = getColumnsTableField().getTable().getSelectedRow().getRowIndex();
              if (m_table.getColumnFilterManager() != null) {
                IColumn<?> col = getColumnsTableField().getTable().getKeyColumn().getValue(getColumnsTableField().getTable().getSelectedRow());
                if (col != null) {
                  m_table.getColumnFilterManager().showFilterForm(col, false);
                }
              }
            }
            getColumnsTableField().reloadTableData();
            if (selectedIndex != null) {
              getColumnsTableField().getTable().selectRow(selectedIndex);
            }
          }

        }

        @Order(20.0)
        public class RemoveFilterButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("Remove");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            Integer selectedIndex = null;
            if (m_table != null && getColumnsTableField().getTable().getSelectedRow() != null) {
              selectedIndex = getColumnsTableField().getTable().getSelectedRow().getRowIndex();
              if (m_table.getColumnFilterManager() != null) {
                IColumn<?> col = getColumnsTableField().getTable().getKeyColumn().getValue(getColumnsTableField().getTable().getSelectedRow());
                if (col != null) {
                  ITableColumnFilter<?> filter = m_table.getColumnFilterManager().getFilter(col);
                  m_table.getColumnFilterManager().getFilters().remove(filter);
                  m_table.applyRowFilters();
                }
              }
            }
            getColumnsTableField().reloadTableData();
            if (selectedIndex != null) {
              getColumnsTableField().getTable().selectRow(selectedIndex);
            }
          }

        }

      }

      @Order(50.0)
      public class ResetBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("FormReset");
        }

        @Order(10.0)
        public class ResetAllButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ResetTableColumnsAll");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            doResetAction(ResetAllMenu.class);
          }

        }

        @Order(20.0)
        public class ResetVisibilityButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ResetTableColumnsVisibility");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            doResetAction(ResetVisibilityMenu.class);
          }

        }

        @Order(30.0)
        public class ResetSortingButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ColumnSorting");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            doResetAction(ResetSortingMenu.class);
          }

        }

        @Order(40.0)
        public class ResetColumnFiltersButton extends AbstractLinkButton {

          @Override
          protected String getConfiguredLabel() {
            return TEXTS.get("ResetTableColumnFilter");
          }

          @Override
          protected boolean getConfiguredProcessButton() {
            return false;
          }

          @Override
          protected void execClickAction() throws ProcessingException {
            doResetAction(ResetColumnFiltersMenu.class);
          }

        }
      }

    }

    @Order(20.0)
    public class OkButton extends AbstractOkButton {
    }

    @Order(30.0)
    public class CancelButton extends AbstractCancelButton {
    }
  }

  private void updateColumnVisibilityAndOrder() {
    IColumn<?>[] visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues(getColumnsTableField().getTable().getVisibleColumn().findRows(true));
    m_table.getColumnSet().setVisibleColumns(visibleColumns);
  }

  private void setColumnVisible(ITableRow row, Boolean visible) throws ProcessingException {
    getColumnsTableField().getTable().getVisibleColumn().setValue(row, visible);
    getColumnsTableField().getTable().getKeyColumn().getValue(row).setVisible(visible);

    updateColumnVisibilityAndOrder();
  }

  private void moveUp(ITableRow row) {
    if (row != null && row.getRowIndex() - 1 >= 0) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), row.getRowIndex() - 1);
    }

    updateColumnVisibilityAndOrder();
  }

  private void moveDown(ITableRow row) {
    if (row != null && row.getRowIndex() + 1 < getColumnsTableField().getTable().getRowCount()) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), row.getRowIndex() + 1);
    }

    updateColumnVisibilityAndOrder();
  }

  private void validateButtons() {
    ITableRow selectedRow = getColumnsTableField().getTable().getSelectedRow();
    boolean selectedRowExists = selectedRow != null;
    boolean isCustomColumn = selectedRow != null && getColumnsTableField().getTable().getKeyColumn().getValue(selectedRow) instanceof ICustomColumn<?>;
    boolean selectedRowHasFilter = selectedRowExists && getColumnsTableField().getTable().getKeyColumn().getValue(selectedRow).isColumnFilterActive();

    getModifyCustomColumnButton().setEnabled(isCustomColumn);
    getRemoveCustomColumnButton().setEnabled(isCustomColumn);

    getMoveDownButton().setEnabled(selectedRowExists);
    getMoveUpButton().setEnabled(selectedRowExists);

    getAscendingButton().setEnabled(selectedRowExists);
    getDescendingButton().setEnabled(selectedRowExists);
    getWithoutButton().setEnabled(selectedRowExists);

    getEditFilterButton().setEnabled(selectedRowExists);
    getRemoveFilterButton().setEnabled(selectedRowHasFilter);
  }

  private void doResetAction(Class<? extends IMenu> action) throws ProcessingException {
    ResetColumnsMenu menu = new ResetColumnsMenu(m_table);
    List<IMenu> childs = menu.getChildActions();
    for (IMenu child : childs) {
      if (child.getClass().equals(action)) {
        child.doAction();
      }
    }
    getColumnsTableField().reloadTableData();
  }

  private void setSort(Boolean ascending) throws ProcessingException {
    ITableRow row = getColumnsTableField().getTable().getSelectedRow();
    if (row == null) {
      return;
    }

    try {
      getColumnsTableField().getTable().setTableChanging(true);
      IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(row);
      if (ascending == null) {
        m_table.getColumnSet().removeSortColumn(selectedCol);
      }
      else {
        if (m_table.getColumnSet().isSortColumn(selectedCol)) {
          m_table.getColumnSet().handleSortEvent(selectedCol, true);
        }
        else {
          m_table.getColumnSet().addSortColumn(selectedCol, ascending);
        }
      }
      m_table.sort();

      getColumnsTableField().reloadTableData();
      getColumnsTableField().getTable().selectRow(row.getRowIndex());
    }
    finally {
      getColumnsTableField().getTable().setTableChanging(false);
    }
  }

  @Override
  public void validateForm() throws ProcessingException {
    boolean oneColumnIsVisble = false;
    for (Boolean visible : getColumnsTableField().getTable().getVisibleColumn().getValues()) {
      if (BooleanUtility.nvl(visible)) {
        oneColumnIsVisble = true;
        break;
      }
    }

    if (!oneColumnIsVisble) {
      throw new VetoException(TEXTS.get("OrganizeTableColumnsMinimalColumnCountMessage"));
    }
  }

  public class ModifyHandler extends AbstractFormHandler {

    private Bookmark tableState;

    @Override
    protected void execLoad() throws ProcessingException {
      // save original state
      tableState = getDesktop().createBookmark();
      getColumnsTableField().reloadTableData();
    }

    @Override
    protected void execPostLoad() throws ProcessingException {
      validateButtons();
    }

    @Override
    protected void execStore() throws ProcessingException {
      // make changes persistent
      ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_table);
    }

    @Override
    protected void execFinally() throws ProcessingException {
      if (!isFormStored() && isSaveNeeded()) {
        // revert to original state
        getDesktop().activateBookmark(tableState, true);
      }
    }

  }
}
