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
package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenuSeparator;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.AddCustomColumnEmptySpaceMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.RemoveFilterMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.RemoveMenu;
import org.eclipse.scout.rt.client.ui.basic.table.OrganizeColumnsForm.MainBox.GroupBox.ProfilesBox.ProfilesTableField;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.JavaTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.AbstractLinkButton;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.HorizontalGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.PlatformExceptionTranslator;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.security.CreateCustomColumnPermission;
import org.eclipse.scout.rt.shared.security.UpdateCustomColumnPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrganizeColumnsForm extends AbstractForm implements IOrganizeColumnsForm {

  private static final Logger LOG = LoggerFactory.getLogger(OrganizeColumnsForm.class);

  public enum ConfigType {
    DEFAULT, CUSTOM
  }

  private final ITable m_organizedTable;

  protected boolean m_loading;

  private boolean m_dirty;

  public OrganizeColumnsForm(ITable table) {
    super(false);
    m_organizedTable = table;
    m_dirty = true;
    callInitializer();
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getRootGroupBox().setScrollable(true);
  }

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("TableOrganize");
  }

  public ColumnsTableField getColumnsTableField() {
    return getFieldByClass(ColumnsTableField.class);
  }

  public ProfilesTableField getProfilesTableField() {
    return getFieldByClass(ProfilesTableField.class);
  }

  public GroupBox getGroupBox() {
    return getFieldByClass(GroupBox.class);
  }

  public MainBox getMainBox() {
    return getFieldByClass(MainBox.class);
  }

  public ITable getOrganizedTable() {
    return m_organizedTable;
  }

  @Order(10)
  public class MainBox extends AbstractGroupBox {

    @Order(10)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
        return HorizontalGroupBoxBodyGrid.class;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 5;
      }

      @Order(5)
      public class ProfilesBox extends AbstractGroupBox {

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected int getConfiguredGridW() {
          return 2;
        }

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("SavedSettings");
        }

        @Override
        protected boolean getConfiguredStatusVisible() {
          return false;
        }

        @Order(10)
        public class ProfilesTableField extends AbstractTableField<ProfilesTableField.Table> {

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
          protected String getConfiguredLabel() {
            return TEXTS.get("Configurations");
          }

          @Override
          protected boolean getConfiguredStatusVisible() {
            return false;
          }

          @Override
          protected void execReloadTableData() {
            List<ITableRow> rowList = new ArrayList<ITableRow>();
            ClientUIPreferences prefs = ClientUIPreferences.getInstance();

            // create default config rows
            TableRow row = new TableRow(getTable().getColumnSet());
            getTable().getConfigNameColumn().setValue(row, TEXTS.get("DefaultSettings"));
            getTable().getConfigTypeColumn().setValue(row, ConfigType.DEFAULT);
            rowList.add(row);

            // create custom config rows
            if (prefs != null) {
              Set<String> configs = prefs.getAllTableColumnsConfigs(getOrganizedTable());
              for (String config : configs) {
                row = new TableRow(getTable().getColumnSet());
                getTable().getConfigNameColumn().setValue(row, config);
                getTable().getConfigTypeColumn().setValue(row, ConfigType.CUSTOM);
                rowList.add(row);
              }
            }

            try {
              getTable().setTableChanging(true);
              getTable().discardAllRows();
              rowList = getTable().addRows(rowList);
              for (ITableRow configRow : getTable().getRows()) {
                configRow.getCellForUpdate(getTable().getConfigNameColumn()).setEditable(false);
              }
            }
            finally {
              getTable().setTableChanging(false);
            }

          }

          protected void resetAll() {
            try {
              getOrganizedTable().setTableChanging(true);
              //
              getOrganizedTable().resetColumns();
              TableUserFilterManager m = getOrganizedTable().getUserFilterManager();
              if (m != null) {
                m.reset();
              }
              ITableCustomizer cst = getOrganizedTable().getTableCustomizer();
              if (cst != null) {
                cst.removeAllColumns();
              }
            }
            finally {
              getOrganizedTable().setTableChanging(false);
            }
            getColumnsTableField().reloadTableData();
          }

          protected void resetView() {
            try {
              getOrganizedTable().setTableChanging(true);
              //
              getOrganizedTable().resetColumnVisibilities();
              getOrganizedTable().resetColumnWidths();
              getOrganizedTable().resetColumnOrder();
              ITableCustomizer cst = getOrganizedTable().getTableCustomizer();
              if (cst != null) {
                cst.removeAllColumns();
              }
            }
            finally {
              getOrganizedTable().setTableChanging(false);
            }
            getColumnsTableField().reloadTableData();
          }

          public class Table extends AbstractTable {

            @Override
            protected Class<? extends IMenu> getConfiguredDefaultMenu() {
              return ApplyMenu.class;
            }

            @Override
            protected boolean getConfiguredHeaderVisible() {
              return false;
            }

            @Override
            protected boolean getConfiguredAutoResizeColumns() {
              return true;
            }

            public ConfigNameColumn getConfigNameColumn() {
              return getColumnSet().getColumnByClass(ConfigNameColumn.class);
            }

            public ConfigTypeColumn getConfigTypeColumn() {
              return getColumnSet().getColumnByClass(ConfigTypeColumn.class);
            }

            @Override
            protected void execRowsSelected(List<? extends ITableRow> rows) {
              getMenuByClass(DeleteMenu.class).setVisible(!isDefaultConfigSelected());
              getMenuByClass(RenameMenu.class).setVisible(!isDefaultConfigSelected());
              getMenuByClass(UpdateMenu.class).setVisible(!isDefaultConfigSelected());

              getMenuByClass(DeleteMenu.class).setEnabled(!isDefaultConfigSelected());
              getMenuByClass(RenameMenu.class).setEnabled(!isDefaultConfigSelected());
              getMenuByClass(UpdateMenu.class).setEnabled(!isDefaultConfigSelected());
            }

            @Order(10)
            public class ConfigNameColumn extends AbstractStringColumn {
              @Override
              protected boolean getConfiguredEditable() {
                return true;
              }

              @Override
              protected int getConfiguredSortIndex() {
                return 1;
              }

              @Override
              protected IFormField execPrepareEdit(ITableRow row) {
                IStringField field = (IStringField) super.execPrepareEdit(row);
                return field;
              }

              @Override
              protected void execCompleteEdit(ITableRow row, IFormField editingField) {
                String oldValue = getConfigNameColumn().getValue(row);
                super.execCompleteEdit(row, editingField);
                String newValue = ((IStringField) editingField).getValue();
                if (!StringUtility.isNullOrEmpty(newValue)) {
                  switch (getConfigTypeColumn().getValue(row)) {
                    case CUSTOM:
                      ClientUIPreferences prefs = ClientUIPreferences.getInstance();
                      prefs.renameTableColumnsConfig(getOrganizedTable(), oldValue, newValue);
                      break;
                    case DEFAULT:
                    default:
                      throw new IllegalStateException("Rows of configType " + getConfigTypeColumn().getValue(row).name() + " should never be editable.");
                  }
                }
                else {
                  if (getConfigTypeColumn().getValue(row) == ConfigType.CUSTOM) {
                    getConfigNameColumn().setValue(row, oldValue);
                  }
                }
                row.getCellForUpdate(getConfigNameColumn()).setEditable(false);
                getTable().sort();
              }

            }

            @Order(20)
            public class ConfigTypeColumn extends AbstractColumn<ConfigType> {
              @Override
              protected boolean getConfiguredDisplayable() {
                return false;
              }

              @Override
              protected int getConfiguredSortIndex() {
                return 0;
              }

            }

            @Order(10)
            public class NewMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
              }

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("New");
              }

              @Override
              protected void execAction() {
                String newConfigName = newConfigName();
                storeCurrentStateAsConfig(newConfigName);
                ITableRow newRow = new TableRow(getTable().getColumnSet());
                getTable().getConfigTypeColumn().setValue(newRow, ConfigType.CUSTOM);
                getTable().getConfigNameColumn().setValue(newRow, newConfigName);
                try {
                  getTable().setTableChanging(true);
                  newRow = getTable().addRow(newRow);
//                  newRow.getCellForUpdate(getTable().getConfigNameColumn()).setEditable(false);
                  getTable().selectRow(newRow);
                  getMenuByClass(RenameMenu.class).doAction();
                }
                finally {
                  getTable().setTableChanging(false);
                }
              }

            }

            @Order(20)
            public class ApplyMenu extends AbstractMenu {

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("Load");
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return combineKeyStrokes(IKeyStroke.ENTER);
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected void execAction() {
                if (getConfigTypeColumn().getSelectedValue() == ConfigType.DEFAULT) {
                  resetAll();
                }
                else {
                  String configName = getConfigNameColumn().getSelectedValue();
                  applyAll(configName);
                  getColumnsTableField().reloadTableData();
                }
                getTable().deselectAllEnabledRows();
              }

            }

            protected boolean isOnlyCustomConfigsSelected() {
              for (ITableRow row : getSelectedRows()) {
                if (row.getCell(getConfigTypeColumn()).getValue() != ConfigType.CUSTOM) {
                  return false;
                }
              }
              return true;
            }

            protected String newConfigName() {
              int profileNr = 1;
              String baseName = TEXTS.get("New") + " ";
              while (getColumnSet().getColumnByClass(ConfigNameColumn.class).getValues().contains(baseName + profileNr)) {
                ++profileNr;
              }
              return baseName + profileNr;
            }

            protected boolean isDefaultConfigSelected() {
              for (ITableRow row : getSelectedRows()) {
                if (row.getCell(getConfigTypeColumn()).getValue() == ConfigType.DEFAULT) {
                  return true;
                }
              }
              return false;
            }

            @Order(30)
            public class UpdateMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("Update");
              }

              @Override
              protected void execAction() {
                List<ITableRow> rows = getSelectedRows();
                for (ITableRow row : rows) {
                  if (getConfigTypeColumn().getValue(row) == ConfigType.CUSTOM) {
                    String config = getConfigNameColumn().getValue(row);
                    deleteConfig(config);
                    storeCurrentStateAsConfig(config);
                  }
                }
                getTable().deselectAllEnabledRows();
              }
            }

            @Order(40)
            public class DeleteMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.MultiSelection, TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("DeleteMenu");
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return IKeyStroke.DELETE;
              }

              @Override
              protected void execAction() {
                List<ITableRow> rows = getSelectedRows();
                deleteRows(rows);
                for (ITableRow row : rows) {
                  if (getConfigTypeColumn().getValue(row) == ConfigType.CUSTOM) {
                    String config = getConfigNameColumn().getValue(row);
                    deleteConfig(config);
                  }
                }
              }
            }

            @Order(50)
            public class RenameMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("Rename");
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return combineKeyStrokes(IKeyStroke.CONTROL, IKeyStroke.ENTER);
              }

              @Override
              protected void execAction() {
                getSelectedRow().getCellForUpdate(getConfigNameColumn()).setEditable(true);
                getTable().requestFocusInCell(getConfigNameColumn(), getSelectedRow());
              }

            }
          }

        }
      }

      @Order(10)
      public class ColumnsGroupBox extends AbstractGroupBox {

        @Override
        protected String getConfiguredLabel() {
          return TEXTS.get("Columns");
        }

        @Override
        protected int getConfiguredGridW() {
          return 3;
        }

        @Override
        protected int getConfiguredGridColumnCount() {
          return 1;
        }

        @Override
        protected boolean getConfiguredStatusVisible() {
          return false;
        }

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
            List<ITableRow> rowList = new ArrayList<ITableRow>();
            for (IColumn<?> col : getOrganizedTable().getColumnSet().getAllColumnsInUserOrder()) {
              if (col.isDisplayable()) {
                if (col.isVisible() || col.isVisibleGranted()) {
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

                  // grouping and sorting
                  StringBuilder sb = new StringBuilder();
                  if (col.isSortActive()) {
                    sb.append(col.isSortAscending() ? "\u2191" : "\u2193");
                    sb.append(col.getSortIndex() + 1);
                  }
                  if (col.isGroupingActive()) {
                    sb.append(TEXTS.get("GroupingAbbreviation"));
                  }
                  getTable().getGroupAndSortColumn().setValue(row, sb.toString());

                  // CustomColumn
                  if (col instanceof ICustomColumn<?>) {
                    getTable().getCustomColumnColumn().setValue(row, TEXTS.get("CustomColumAbbreviation"));
                  }

                  // filter
                  if (col.isColumnFilterActive()) {
                    getTable().getFilterColumn().setValue(row, TEXTS.get("FilterAbbreviation"));
                  }

                  //width
                  getTable().getWidthColumn().setValue(row, col.getWidth());

                  rowList.add(row);
                }
              }
            }
            try {
              getTable().setTableChanging(true);
              getTable().discardAllRows();
              rowList = getTable().addRows(rowList);

              // check visible columns
              for (ITableRow row : rowList) {
                getTable().checkRow(row, getTable().getKeyColumn().getValue(row).isVisible());
              }
            }
            finally {
              getTable().setTableChanging(false);
            }
            enableDisableMenus();
          }

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
            protected TransferObject execDrag(List<ITableRow> rows) {
              return new JavaTransferObject(rows);
            }

            @Override
            protected void execDrop(ITableRow row, TransferObject transfer) {
              if (row != null && transfer != null && transfer instanceof JavaTransferObject) {
                List<ITableRow> draggedRows = ((JavaTransferObject) transfer).getLocalObjectAsList(ITableRow.class);
                if (CollectionUtility.hasElements(draggedRows)) {
                  ITableRow draggedRow = CollectionUtility.firstElement(draggedRows);
                  if (draggedRow.getRowIndex() != row.getRowIndex()) {
                    // target row other than source row
                    try {
                      getTable().setTableChanging(true);
                      if (draggedRow.getRowIndex() < row.getRowIndex()) {
                        moveDown(draggedRow, row.getRowIndex());
                      }
                      else {
                        moveUp(draggedRow, row.getRowIndex());
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

            @Override
            protected boolean getConfiguredAutoResizeColumns() {
              return true;
            }

            @Override
            protected boolean getConfiguredHeaderVisible() {
              return false;
            }

            public KeyColumn getKeyColumn() {
              return getColumnSet().getColumnByClass(KeyColumn.class);
            }

            public GroupAndSortColumn getGroupAndSortColumn() {
              return getColumnSet().getColumnByClass(GroupAndSortColumn.class);
            }

            public FilterColumn getFilterColumn() {
              return getColumnSet().getColumnByClass(FilterColumn.class);
            }

            public CustomColumnColumn getCustomColumnColumn() {
              return getColumnSet().getColumnByClass(CustomColumnColumn.class);
            }

            public WidthColumn getWidthColumn() {
              return getColumnSet().getColumnByClass(WidthColumn.class);
            }

            public BehindScrollbarColumn getBehindScrollbarColumn() {
              return getColumnSet().getColumnByClass(BehindScrollbarColumn.class);
            }

            public TitleColumn getTitleColumn() {
              return getColumnSet().getColumnByClass(TitleColumn.class);
            }

            @Override
            protected boolean getConfiguredCheckable() {
              return true;
            }

            @Override
            protected void execRowsChecked(Collection<? extends ITableRow> rows) {
              if (isFormLoading()) {
                return;
              }
              for (ITableRow row : rows) {
                setColumnVisible(row, row.isChecked());
              }
            }

            @Override
            protected void execRowsSelected(List<? extends ITableRow> rows) {
              enableDisableMenus();
              refreshMenus();
            }

            @Override
            protected void execInitTable() {
              getMenuByClass(AddCustomColumnMenu.class).setVisiblePermission(new CreateCustomColumnPermission());
              getMenuByClass(AddCustomColumnMenu.class).setVisible(getOrganizedTable().getTableCustomizer() != null);
              getMenuByClass(AddCustomColumnEmptySpaceMenu.class).setVisiblePermission(new CreateCustomColumnPermission());
              getMenuByClass(AddCustomColumnEmptySpaceMenu.class).setVisible(getOrganizedTable().getTableCustomizer() != null);
              getMenuByClass(ModifyCustomColumnMenu.class).setVisiblePermission(new UpdateCustomColumnPermission());
              getMenuByClass(ModifyCustomColumnMenu.class).setVisible(getOrganizedTable().getTableCustomizer() != null);

              getWidthColumn().setVisible(!getOrganizedTable().isAutoResizeColumns());
            }

            protected void refreshMenus() {
              ITableRow selectedRow = getColumnsTableField().getTable().getSelectedRow();
              IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(selectedRow);
              if (selectedCol == null) {
                return;
              }
              // sort
              if (selectedCol.isSortActive() && selectedCol.isSortAscending()) {
                getMenuByClass(SortAscAdditionalMenu.class).setText("x");
              }
              else {
                getMenuByClass(SortAscAdditionalMenu.class).setText("+");
              }
              if (selectedCol.isSortActive() && !selectedCol.isSortAscending()) {
                getMenuByClass(SortDescAdditionalMenu.class).setText("x");
              }
              else {
                getMenuByClass(SortDescAdditionalMenu.class).setText("+");
              }
              // group
              if (selectedCol.isGroupingActive()) {
                getMenuByClass(GroupAdditionalMenu.class).setText("x");
              }
              else {
                getMenuByClass(GroupAdditionalMenu.class).setText("+");
              }
            }

            protected void sortSelectedColumn(boolean multiSort, boolean ascending) {
              ITableRow row = getColumnsTableField().getTable().getSelectedRow();
              try {
                getColumnsTableField().getTable().setTableChanging(true);
                IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(row);
                if ((ascending && selectedCol.isSortActive() && selectedCol.isSortAscending())
                    || (!ascending && selectedCol.isSortActive() && !selectedCol.isSortAscending())) {
                  getOrganizedTable().getColumnSet().removeSortColumn(selectedCol);
                }
                else {
                  getOrganizedTable().getColumnSet().handleSortEvent(selectedCol, multiSort, ascending);
                }
                getOrganizedTable().sort();

                getColumnsTableField().reloadTableData();
                getColumnsTableField().getTable().selectRow(row.getRowIndex());
              }
              finally {
                getColumnsTableField().getTable().setTableChanging(false);
              }
              refreshMenus();
            }

            protected void groupSelectedColumn(boolean multiGroup) {
              ITableRow row = getColumnsTableField().getTable().getSelectedRow();
              try {
                getColumnsTableField().getTable().setTableChanging(true);
                IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(row);
                if ((selectedCol.isGroupingActive())) {
                  getOrganizedTable().getColumnSet().removeGroupColumn(selectedCol);
                }
                else {
                  boolean ascending = true;
                  if (selectedCol.isSortActive()) {
                    ascending = selectedCol.isSortAscending();
                  }
                  getOrganizedTable().getColumnSet().handleGroupingEvent(selectedCol, multiGroup, ascending);
                }
                getOrganizedTable().sort();

                getColumnsTableField().reloadTableData();
                getColumnsTableField().getTable().selectRow(row.getRowIndex());
              }
              finally {
                getColumnsTableField().getTable().setTableChanging(false);
              }
              refreshMenus();
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

            @Order(30)
            public class TitleColumn extends AbstractStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("Title");
              }

              @Override
              protected int getConfiguredWidth() {
                return 200;
              }

            }

            @Order(40)
            public class GroupAndSortColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

            }

            @Order(50)
            public class FilterColumn extends AbstractStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("ResetTableColumnFilter");
              }

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

            }

            @Order(60)
            public class CustomColumnColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

            }

            @Order(70)
            public class WidthColumn extends AbstractIntegerColumn {

              @Override
              protected boolean getConfiguredEditable() {
                return true;
              }

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

              @Override
              protected void execCompleteEdit(ITableRow row, IFormField editingField) {
                super.execCompleteEdit(row, editingField);
                getKeyColumn().getValue(row).setWidth(getWidthColumn().getValue(row));
              }

            }

            // prevents the scrollbar from overlapping the WidthColumn
            @Order(80)
            public class BehindScrollbarColumn extends AbstractStringColumn {

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

              @Override
              protected int getConfiguredWidth() {
                return 10;
              }

            }

            @Order(10)
            public class MoveUpMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.CaretUp;
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return AbstractAction.combineKeyStrokes(IKeyStroke.ALT, IKeyStroke.UP);
              }

              @Override
              protected void execAction() {
                for (ITableRow row : getSelectedRows()) {
                  if (canMoveUp(row)) {
                    moveUp(row);
                  }
                }
              }

              protected boolean canMoveUp(ITableRow candidateRow) {
                for (ITableRow row : getSelectedRows()) {
                  if (candidateRow != row && row.getRowIndex() == candidateRow.getRowIndex() - 1) {
                    return false;
                  }
                }
                return true;
              }
            }

            @Order(20)
            public class MoveDownMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return AbstractAction.combineKeyStrokes(IKeyStroke.ALT, IKeyStroke.DOWN);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.CaretDown;
              }

              @Override
              protected void execAction() {
                List<ITableRow> selectedRows = getSelectedRows();
                Collections.reverse(selectedRows);
                for (ITableRow row : selectedRows) {
                  if (canMoveDown(row)) {
                    moveDown(row);
                  }
                }
              }

              protected boolean canMoveDown(ITableRow candidateRow) {
                for (ITableRow row : getSelectedRows()) {
                  if (candidateRow != row && row.getRowIndex() == candidateRow.getRowIndex() + 1) {
                    return false;
                  }
                }
                return true;
              }
            }

            @Order(30)
            public class MenuSeparator3 extends AbstractMenuSeparator {
            }

            @Order(40)
            public class SortAscMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowUp;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(false, true);
              }
            }

            @Order(50)
            public class SortDescMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowDown;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(false, false);
              }
            }

            @Order(60)
            public class SortAscAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowUp;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(true, true);
              }
            }

            @Order(70)
            public class SortDescAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowDown;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(true, false);
              }
            }

            @Order(80)
            public class MenuSeparator1 extends AbstractMenuSeparator {
            }

            @Order(90)
            public class GroupMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Group;
              }

              @Override
              protected void execAction() {
                groupSelectedColumn(false);
              }
            }

            @Order(100)
            public class GroupAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return getOrganizedTable().isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Group;
              }

              @Override
              protected void execAction() {
                groupSelectedColumn(true);
              }
            }

            @Order(110)
            public class MenuSeparator2 extends AbstractMenuSeparator {
            }

            @Order(130)
            public class AddCustomColumnEmptySpaceMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Plus;
              }

              @Override
              protected void execAction() {
                addColumnWithCustomizer();
              }
            }

            @Order(130)
            public class AddCustomColumnMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Plus;
              }

              @Override
              protected void execAction() {
                addColumnWithCustomizer();
              }

            }

            protected void addColumnWithCustomizer() {
              if (getOrganizedTable() != null) {
                if (getOrganizedTable().getTableCustomizer() != null) {
                  ArrayList<String> existingColumns = new ArrayList<String>();
                  for (IColumn c : getOrganizedTable().getColumns()) {
                    if (c.isVisible()) {
                      existingColumns.add(c.getColumnId());
                    }
                  }
                  getOrganizedTable().getTableCustomizer().addColumn();
                  moveNewColumnsBeforeSelection(existingColumns);
                }
              }
            }

            public void moveNewColumnsBeforeSelection(ArrayList<String> existingColumns) {
              ITableRow insertBeforeThisRow = getColumnsTableField().getTable().getSelectedRow();
              if (insertBeforeThisRow == null && getColumnsTableField().getTable().getRowCount() > 0) {
                insertBeforeThisRow = getColumnsTableField().getTable().getRow(0);
              }
              getColumnsTableField().reloadTableData();
              if (insertBeforeThisRow == null) {
                return;
              }
              int insertBeforeRowIndex = insertBeforeThisRow.getRowIndex();
              // find new rows
              for (ITableRow columnRow : getColumnsTableField().getTable().getRows()) {
                if (!existingColumns.contains(getColumnsTableField().getTable().getKeyColumn().getValue(columnRow).getColumnId())) {
                  // move new column
                  try {
                    getColumnsTableField().getTable().setTableChanging(true);
                    if (columnRow.getRowIndex() < insertBeforeRowIndex) {
                      moveDown(columnRow, insertBeforeRowIndex);
                    }
                    else {
                      moveUp(columnRow, insertBeforeRowIndex);
                    }
                    ++insertBeforeRowIndex;
                    updateColumnVisibilityAndOrder();

                    // select new row
                    getColumnsTableField().getTable().selectRow(columnRow);
                  }
                  finally {
                    getColumnsTableField().getTable().setTableChanging(false);
                  }
                }
              }
            }

            @Order(140)
            public class RemoveMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Minus;
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return AbstractKeyStroke.DELETE;
              }

              @Override
              protected void execAction() {
                if (getOrganizedTable() != null) {
                  for (ITableRow selectedRow : getSelectedRows()) {
                    IColumn<?> selectedCol = getKeyColumn().getValue(selectedRow);
                    if (selectedCol instanceof ICustomColumn<?>) {
                      if (getOrganizedTable().getTableCustomizer() != null) {
                        getOrganizedTable().getTableCustomizer().removeColumn((ICustomColumn<?>) selectedCol);
                      }
                    }

                  }
                }
                reloadTableData();
              }
            }

            @Order(150)
            public class ModifyCustomColumnMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Pencil;
              }

              @Override
              protected void execAction() {
                if (getOrganizedTable() != null) {
                  if (getOrganizedTable().getTableCustomizer() != null) {
                    if (getColumnsTableField().getTable().getSelectedRow() != null) {
                      IColumn<?> selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(getColumnsTableField().getTable().getSelectedRow());
                      if (selectedCol instanceof ICustomColumn<?>) {
                        getOrganizedTable().getTableCustomizer().modifyColumn((ICustomColumn<?>) selectedCol);
                      }
                    }
                  }
                }
                getColumnsTableField().reloadTableData();
              }

            }

            @Order(160)
            public class RemoveFilterMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("FilterAbbreviation") + " x";
              }

              @Override
              protected void execAction() {
                if (getOrganizedTable() != null) {
                  for (ITableRow selectedRow : getSelectedRows()) {
                    IColumn<?> selectedCol = getKeyColumn().getValue(selectedRow);
                    if (selectedCol.isColumnFilterActive()) {
                      getOrganizedTable().getUserFilterManager().removeFilterByKey(selectedCol);
                    }
                  }
                }
                reloadTableData();
              }
            }

          }
        }
      }

    }

    @Order(80)
    public class CopyWidthsOfColumnsButton extends AbstractLinkButton {

      public static final String COLUMN_COPY_CLIPBOARD_IDENTIFIER = "dev.table.menu.column.width.copy.ident";

      @Override
      protected String getConfiguredLabel() {
        return "Dev: " + TEXTS.get("CopyWidthsOfColumnsMenu");
      }

      @Override
      protected boolean getConfiguredProcessButton() {
        return false;
      }

      @Override
      protected void execInitField() {
        // This button is only visible in development mode
        setVisibleGranted(Platform.get().inDevelopmentMode());
      }

      @Override
      protected void execClickAction() {
        try {
          StringBuilder sb = new StringBuilder();

          // Add an identifier for fast identification
          sb.append(COLUMN_COPY_CLIPBOARD_IDENTIFIER);
          sb.append("\n");

          // only visible columns are of interest
          for (IColumn<?> column : getOrganizedTable().getColumnSet().getVisibleColumns()) {
            sb.append(column.getClass().getName());
            sb.append("\t");
            sb.append(column.getWidth());
            sb.append("\n");
          }

          // calling the service to write the buffer to the clipboard
          IClipboardService svc = BEANS.opt(IClipboardService.class);
          if (svc == null) {
            LOG.info(sb.toString());
            MessageBoxes.createOk().withBody(TEXTS.get("SeeLogFileForColumnWidthsOutput")).show();
          }
          else {
            svc.setTextContents(sb.toString());
          }
        }
        catch (RuntimeException e) {
          throw BEANS.get(PlatformExceptionTranslator.class).translate(e)
              .withContextInfo("button", getLabel());
        }
      }
    }

  }

  public void updateColumnVisibilityAndOrder() {
    List<IColumn<?>> visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues(getColumnsTableField().getTable().getCheckedRows());
    getOrganizedTable().getColumnSet().setVisibleColumns(visibleColumns);
    ClientUIPreferences.getInstance().setAllTableColumnPreferences(getOrganizedTable());
  }

  public void setColumnVisible(ITableRow row, Boolean visible) {
    getColumnsTableField().getTable().checkRow(row, visible);

    updateColumnVisibilityAndOrder();
  }

  public void moveUp(ITableRow row) {
    moveUp(row, row.getRowIndex() - 1);
  }

  public void moveUp(ITableRow row, int targetIndex) {
    if (row != null && targetIndex >= 0) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), targetIndex);
    }
    updateColumnVisibilityAndOrder();
  }

  public void moveDown(ITableRow row) {
    moveDown(row, row.getRowIndex() + 1);
  }

  public void moveDown(ITableRow row, int targetIndex) {
    if (row != null && targetIndex < getColumnsTableField().getTable().getRowCount()) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), targetIndex);
    }
    updateColumnVisibilityAndOrder();
  }

  public void enableDisableMenus() {
    boolean isCustomColumnSelected = false;
    boolean isFilterActive = false;
    Table tableFieldTable = getColumnsTableField().getTable();
    List<ITableRow> selectedRows = tableFieldTable.getSelectedRows();
    for (ITableRow row : selectedRows) {
      if (tableFieldTable.getKeyColumn().getValue(row).isColumnFilterActive()) {
        isFilterActive = true;
      }
      if (tableFieldTable.getKeyColumn().getValue(row) instanceof ICustomColumn<?>) {
        isCustomColumnSelected = true;
      }
    }
    tableFieldTable.getMenuByClass(ModifyCustomColumnMenu.class).setEnabled(isCustomColumnSelected);
    tableFieldTable.getMenuByClass(ModifyCustomColumnMenu.class).setVisible(isCustomColumnSelected);
    tableFieldTable.getMenuByClass(RemoveMenu.class).setEnabled(isCustomColumnSelected);
    tableFieldTable.getMenuByClass(RemoveMenu.class).setVisible(isCustomColumnSelected);
    tableFieldTable.getMenuByClass(RemoveFilterMenu.class).setEnabled(isFilterActive);
    tableFieldTable.getMenuByClass(RemoveFilterMenu.class).setVisible(isFilterActive);
    tableFieldTable.getMenuByClass(AddCustomColumnEmptySpaceMenu.class).setEnabled(getOrganizedTable().getTableCustomizer() != null && tableFieldTable.getSelectedRows().size() == 0);
    tableFieldTable.getMenuByClass(AddCustomColumnEmptySpaceMenu.class).setVisible(getOrganizedTable().getTableCustomizer() != null && tableFieldTable.getSelectedRows().size() == 0);
  }

  @Override
  public void reload() {
    m_loading = true;
    try {
      getColumnsTableField().reloadTableData();
      getProfilesTableField().reloadTableData();
    }
    finally {
      m_loading = false;
      m_dirty = false;
    }
  }

  @Override
  public void setDirty(boolean dirty) {
    m_dirty = dirty;
  }

  @Override
  public boolean isDirty() {
    return m_dirty;
  }

  @Override
  public boolean isFormLoading() {
    return super.isFormLoading() || m_loading;
  }

  public void storeCurrentStateAsConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    prefs.addTableColumnsConfig(getOrganizedTable(), configName);
    prefs.setAllTableColumnPreferences(getOrganizedTable(), configName);
    if (getOrganizedTable().getTableCustomizer() != null) {
      prefs.setTableCustomizerData(getOrganizedTable().getTableCustomizer(), configName);
    }
  }

  public void deleteConfig(String config) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (prefs != null) {
      prefs.removeTableColumnsConfig(getOrganizedTable(), config);
    }
  }

  public void applyAll(String configName) {
    applyViewForConfig(configName);
    getOrganizedTable().getColumnSet().applySortingAndGrouping(configName);
  }

  public void applyViewForConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (getOrganizedTable().getTableCustomizer() != null) {
      byte[] tableCustomizerData = prefs.getTableCustomizerData(getOrganizedTable().getTableCustomizer(), configName);
      if (tableCustomizerData != null) {
        getOrganizedTable().getTableCustomizer().removeAllColumns();
        getOrganizedTable().getTableCustomizer().setSerializedData(tableCustomizerData);
      }
      if (getOrganizedTable().getReloadHandler() != null) {
        getOrganizedTable().resetColumnConfiguration();
        getOrganizedTable().getReloadHandler().reload();
      }
    }
    for (IColumn<?> col : getOrganizedTable().getColumnSet().getColumns()) {
      col.setVisible(prefs.getTableColumnVisible(col, col.isInitialVisible(), configName));
      col.setWidth(prefs.getTableColumnWidth(col, col.getInitialWidth(), configName));
      col.setVisibleColumnIndexHint(prefs.getTableColumnViewIndex(col, col.getInitialSortIndex(), configName));
      if (col instanceof INumberColumn) {
        ((INumberColumn) col).setBackgroundEffect(prefs.getTableColumnBackgroundEffect(col, ((INumberColumn) col).getInitialBackgroundEffect(), configName));
      }
    }
  }
}
