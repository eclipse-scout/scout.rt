/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractAlphanumericSortingStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractIntegerColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.INumberColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.AddColumnEmptySpaceMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.AddColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.GroupAdditionalMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.GroupMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.MoveDownMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.MoveUpMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.RemoveFilterMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.RemoveMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ProfilesBox.ProfilesTableField;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.IReloadReason;
import org.eclipse.scout.rt.client.ui.dnd.IDNDSupport;
import org.eclipse.scout.rt.client.ui.dnd.JavaTransferObject;
import org.eclipse.scout.rt.client.ui.dnd.TransferObject;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.AbstractGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.internal.HorizontalGroupBoxBodyGrid;
import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("5bd26d3c-604d-4991-a246-7fff74e32faa")
public class OrganizeColumnsForm extends AbstractForm implements IOrganizeColumnsForm {

  private static final Logger LOG = LoggerFactory.getLogger(OrganizeColumnsForm.class);
  private static final String UNICODE_ARROW_UP = "\u2191";
  private static final String UNICODE_ARROW_DOWN = "\u2193";
  private static final String VISIBLE_DIMENSION_HIERARCHICAL = "dim_hierarchical";

  public enum ConfigType {
    DEFAULT, CUSTOM
  }

  private final ITable m_organizedTable;

  protected boolean m_loading;

  private PropertyChangeListener m_organizedTablePropertyListener = new P_OrganizeColumnTablePropertyListener();

  public OrganizeColumnsForm(ITable table) {
    super(false);
    m_organizedTable = table;
    callInitializer();
  }

  @Override
  protected void execInitForm() {
    getOrganizedTable().addPropertyChangeListener(ITable.PROP_HIERARCHICAL_ROWS, m_organizedTablePropertyListener);
  }

  @Override
  protected void execDisposeForm() {
    getOrganizedTable().removePropertyChangeListener(ITable.PROP_HIERARCHICAL_ROWS, m_organizedTablePropertyListener);
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    getRootGroupBox().setScrollable(true);
    updateGroupingMenuVisibility();
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

  public GroupAdditionalMenu getGroupAdditionalMenu() {
    return getColumnsTableField().getTable().getMenuByClass(GroupAdditionalMenu.class);
  }

  public GroupMenu getGroupMenu() {
    return getColumnsTableField().getTable().getMenuByClass(GroupMenu.class);
  }

  public ITable getOrganizedTable() {
    return m_organizedTable;
  }

  protected void updateGroupingMenuVisibility() {
    boolean hierarchicalTable = getOrganizedTable().isHierarchical();
    getGroupMenu().setVisible(!hierarchicalTable, VISIBLE_DIMENSION_HIERARCHICAL);
    getGroupAdditionalMenu().setVisible(!hierarchicalTable, VISIBLE_DIMENSION_HIERARCHICAL);
  }

  @Order(10)
  @ClassId("d9f2e54a-cb41-4453-8ce9-ba41b8e247bd")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredWidthInPixel() {
      return 880;
    }

    @Override
    protected int getConfiguredHeightInPixel() {
      return 350;
    }

    @Order(10)
    @ClassId("abaf2e0c-1c14-4b99-81dc-8b83453f5766")
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
        return HorizontalGroupBoxBodyGrid.class;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 5;
      }

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Order(5)
      @ClassId("698da86a-d878-439e-9a1c-da7b63d4f2e3")
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
        @ClassId("f96ddd7f-634b-486b-a4be-fbb69b5162e4")
        public class ProfilesTableField extends AbstractTableField<ProfilesTableField.Table> {

          @Override
          protected int getConfiguredGridH() {
            return 3;
          }

          @Override
          protected boolean getConfiguredGridUseUiHeight() {
            return UserAgentUtility.isMobileDevice();
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
          protected String getConfiguredLabel() {
            return TEXTS.get("Configurations");
          }

          @Override
          protected boolean getConfiguredStatusVisible() {
            return false;
          }

          @Override
          protected void execReloadTableData() {
            List<ITableRow> rowList = new ArrayList<>();
            ClientUIPreferences prefs = ClientUIPreferences.getInstance();

            // create default config rows
            TableRow row = new TableRow(getTable().getColumnSet());
            getTable().getConfigNameColumn().setValue(row, TEXTS.get("DefaultSettings"));
            getTable().getConfigTypeColumn().setValue(row, ConfigType.DEFAULT);
            rowList.add(row);

            // create custom config rows
            if (prefs != null) {
              Set<String> configs = prefs.getAllTableColumnsConfigs(m_organizedTable);
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

          @ClassId("359e1e7e-26f0-411d-baf1-2ba9f554212d")
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
              getMenuByClass(DeleteMenu.class).setVisible(!isDefaultConfigSelected() && !rows.isEmpty());
              getMenuByClass(RenameMenu.class).setVisible(!isDefaultConfigSelected() && !rows.isEmpty());
              getMenuByClass(UpdateMenu.class).setVisible(!isDefaultConfigSelected() && !rows.isEmpty());

              getMenuByClass(DeleteMenu.class).setEnabled(!isDefaultConfigSelected() && !rows.isEmpty());
              getMenuByClass(RenameMenu.class).setEnabled(!isDefaultConfigSelected() && !rows.isEmpty());
              getMenuByClass(UpdateMenu.class).setEnabled(!isDefaultConfigSelected() && !rows.isEmpty());
            }

            @Order(10)
            @ClassId("f607ab39-e616-4b27-b7c4-f6e437b6b1a3")
            public class ConfigNameColumn extends AbstractAlphanumericSortingStringColumn {
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
                      prefs.renameTableColumnsConfig(m_organizedTable, oldValue, newValue);
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
            @ClassId("d84a8d65-59c2-449d-b375-7a2f2da1844b")
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
            @ClassId("f830ba85-1629-407e-9935-e241713a35c7")
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
                  getTable().selectRow(newRow);
                  getMenuByClass(RenameMenu.class).rename();
                }
                finally {
                  getTable().setTableChanging(false);
                }
              }

            }

            @Order(20)
            @ClassId("203277ac-846d-4781-8547-32e0530c9521")
            public class ApplyMenu extends AbstractMenu {

              @Override
              protected String getConfiguredText() {
                return TEXTS.get("Load");
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return IKeyStroke.ENTER;
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
            @ClassId("d0415e79-3f85-49f8-ab64-f87bc3a8363b")
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
            @ClassId("8325ee81-5ae4-4b82-b27f-26cab198c77c")
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
            @ClassId("1ae293da-528c-4e37-abfa-497e9c1892a6")
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
                rename();
              }

              protected void rename() {
                getSelectedRow().getCellForUpdate(getConfigNameColumn()).setEditable(true);
                getTable().requestFocusInCell(getConfigNameColumn(), getSelectedRow());
              }
            }
          }

        }
      }

      @Order(10)
      @ClassId("952e2572-c7ed-400f-a8d0-a6b445bc1e41")
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

        @Override
        protected String getConfiguredMenuBarPosition() {
          return IGroupBox.MENU_BAR_POSITION_TITLE;
        }

        @Order(10)
        @ClassId("eefd05cf-b8b6-4c07-82c9-91aaafe9b8b6")
        public class ColumnsTableField extends AbstractTableField<Table> {

          @Override
          protected int getConfiguredGridH() {
            return 3;
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
            Table columnsTable = getTable();
            List<ITableRow> rowList = createColumnsTableRows(columnsTable);
            try {
              columnsTable.setTableChanging(true);
              columnsTable.discardAllRows();
              rowList = columnsTable.addRows(rowList);

              // check visible columns
              for (ITableRow row : rowList) {
                columnsTable.checkRow(row, columnsTable.getKeyColumn().getValue(row).isVisible());
              }
            }
            finally {
              columnsTable.setTableChanging(false);
            }
            enableDisableMenus();
          }

          @ClassId("76937f06-5cc2-4281-9eae-28b59d7bd770")
          public class Table extends AbstractTable {

            @Override
            protected int getConfiguredDropType() {
              return IDNDSupport.TYPE_JAVA_ELEMENT_TRANSFER;
            }

            @Override
            protected boolean getConfiguredTextFilterEnabled() {
              return false;
            }

            @Override
            protected void execDrop(ITableRow row, TransferObject transfer) {
              if (row != null && transfer instanceof JavaTransferObject) {
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
              getWidthColumn().setVisible(!m_organizedTable.isAutoResizeColumns());
            }

            protected void refreshMenus() {
              ITableRow selectedRow = getColumnsTableField().getTable().getSelectedRow();
              IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(selectedRow);
              if (selectedCol == null) {
                return;
              }
              // sort
              if (selectedCol.isSortActive() && selectedCol.isSortAscending()) {
                getMenuByClass(SortAscAdditionalMenu.class).setIconId(AbstractIcons.LongArrowUpRemove);
              }
              else {
                getMenuByClass(SortAscAdditionalMenu.class).setIconId(AbstractIcons.LongArrowUpPlus);
              }
              if (selectedCol.isSortActive() && !selectedCol.isSortAscending()) {
                getMenuByClass(SortDescAdditionalMenu.class).setIconId(AbstractIcons.LongArrowDownRemove);
              }
              else {
                getMenuByClass(SortDescAdditionalMenu.class).setIconId(AbstractIcons.LongArrowDownPlus);
              }
              // group
              if (selectedCol.isGroupingActive()) {
                getMenuByClass(GroupAdditionalMenu.class).setIconId(AbstractIcons.GroupRemove);
              }
              else {
                getMenuByClass(GroupAdditionalMenu.class).setIconId(AbstractIcons.GroupPlus);
              }
            }

            protected void sortSelectedColumn(boolean multiSort, boolean ascending) {
              ITableRow row = getColumnsTableField().getTable().getSelectedRow();
              try {
                getColumnsTableField().getTable().setTableChanging(true);
                IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(row);
                if ((ascending && selectedCol.isSortActive() && selectedCol.isSortAscending())
                    || (!ascending && selectedCol.isSortActive() && !selectedCol.isSortAscending())) {
                  m_organizedTable.getColumnSet().removeSortColumn(selectedCol);
                }
                else {
                  m_organizedTable.getColumnSet().handleSortEvent(selectedCol, multiSort, ascending);
                }
                m_organizedTable.sort();

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
                  m_organizedTable.getColumnSet().removeGroupColumn(selectedCol);
                }
                else {
                  boolean ascending = true;
                  if (selectedCol.isSortActive()) {
                    ascending = selectedCol.isSortAscending();
                  }
                  m_organizedTable.getColumnSet().handleGroupingEvent(selectedCol, multiGroup, ascending);
                }
                m_organizedTable.sort();

                getColumnsTableField().reloadTableData();
                getColumnsTableField().getTable().selectRow(row.getRowIndex());
              }
              finally {
                getColumnsTableField().getTable().setTableChanging(false);
              }
              refreshMenus();
            }

            @Order(10)
            @ClassId("88b70ee3-05d8-458d-bd92-d7b4bfb22383")
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
            @ClassId("a5dcb98c-aca9-49e5-a80c-eb47d22041a2")
            public class TitleColumn extends AbstractStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("Title");
              }

              @Override
              protected int getConfiguredWidth() {
                return 120;
              }

            }

            @Order(40)
            @ClassId("23b153bc-1d74-46a2-b08b-87aecba6c1b0")
            public class GroupAndSortColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 45;
              }

              @Override
              protected int getConfiguredMinWidth() {
                return 45;
              }

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

              @Override
              protected boolean getConfiguredHtmlEnabled() {
                return true;
              }
            }

            @Order(50)
            @ClassId("09efa829-7f05-4e51-91e0-b4d032a5ab7c")
            public class FilterColumn extends AbstractStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("ResetTableColumnFilter");
              }

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

              @Override
              protected int getConfiguredMinWidth() {
                return 40;
              }

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

            }

            @Order(60)
            @ClassId("028a3b5b-5eda-4d7e-9490-cf12e2cf3a70")
            public class CustomColumnColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

              @Override
              protected int getConfiguredMinWidth() {
                return 40;
              }

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

            }

            @Order(70)
            @ClassId("c0bfe89c-2402-419a-bda1-68fc61b23ec7")
            public class WidthColumn extends AbstractIntegerColumn {

              @Override
              protected boolean getConfiguredEditable() {
                return true;
              }

              @Override
              protected int getConfiguredWidth() {
                return 60;
              }

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

              @Override
              protected void execDecorateCell(Cell cell, ITableRow row) {
                cell.setEditable(!isFixedWidth(row));
              }

              @Override
              protected IFormField execPrepareEdit(ITableRow row) {
                IFormField field = super.execPrepareEdit(row);
                if (field == null) {
                  return null;
                }

                field.setEnabledGranted(!isFixedWidth(row));
                return field;
              }

              protected boolean isFixedWidth(ITableRow row) {
                IColumn<?> column = getKeyColumn().getValue(row);
                return column != null && column.isFixedWidth();
              }

              @Override
              protected void execCompleteEdit(ITableRow row, IFormField editingField) {
                if (!editingField.isEnabled()) {
                  return;
                }

                super.execCompleteEdit(row, editingField);

                Integer enteredWidth = getValue(row);
                // In case we set the value to something different to what the user entered,
                // we need to update the displayed text to that value as well later.
                boolean updateValue = false;

                IColumn<?> column = getKeyColumn().getValue(row);
                int newWidth;
                if (enteredWidth == null || enteredWidth < column.getMinWidth()) {
                  newWidth = column.getMinWidth();
                  updateValue = true;
                }
                else {
                  newWidth = enteredWidth;
                }
                column.setWidth(newWidth);
                ClientUIPreferences.getInstance().setAllTableColumnPreferences(getOrganizedTable());

                if (updateValue) {
                  setValue(row, newWidth);
                }
              }

            }

            // prevents the scrollbar from overlapping the WidthColumn
            @Order(80)
            @ClassId("cf080377-238a-4d59-9120-e10708f17b9a")
            public class BehindScrollbarColumn extends AbstractStringColumn {

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

              @Override
              protected int getConfiguredWidth() {
                return 10;
              }

              @Override
              protected String getConfiguredCssClass() {
                return "organize-columns-behind-scrollbar-column";
              }

              @Override
              protected boolean getConfiguredVisible() {
                // touch devices don't have a regular scrollbar -> no need to show the column
                return !UserAgentUtility.isTouchDevice();
              }

            }

            @Order(10)
            @ClassId("3ffc14b8-85c5-4015-aaeb-5aa0dbb66a9f")
            public class AddColumnEmptySpaceMenu extends AbstractMenu {

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
                execAddColumnAction();
              }
            }

            @Order(10)
            @ClassId("36a172fa-c7ef-4682-9724-6cfdd950907a")
            public class AddColumnMenu extends AbstractMenu {

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
                execAddColumnAction();
              }

            }

            public void moveNewColumnsAfterSelection(List<String> existingColumns) {
              Table columnsTable = getColumnsTableField().getTable();
              ITableRow insertAfterThisRow = columnsTable.getSelectedRow();
              boolean insertOnTop = false;
              if (insertAfterThisRow == null && columnsTable.getRowCount() > 0) {
                insertOnTop = true;
              }
              getColumnsTableField().reloadTableData();
              int insertAfterRowIndex = 0;
              if (insertAfterThisRow != null) {
                insertAfterRowIndex = insertAfterThisRow.getRowIndex();
              }
              // find new rows
              for (ITableRow columnRow : columnsTable.getRows()) {
                if (!existingColumns.contains(columnsTable.getKeyColumn().getValue(columnRow).getColumnId())) {
                  // move new column
                  try {
                    getColumnsTableField().getTable().setTableChanging(true);
                    if (insertOnTop) {
                      moveUp(columnRow, 0);
                    }
                    else if (columnRow.getRowIndex() <= insertAfterRowIndex) {
                      moveDown(columnRow, insertAfterRowIndex + 1);
                    }
                    else {
                      moveUp(columnRow, insertAfterRowIndex + 1);
                    }
                    ++insertAfterRowIndex;
                    updateColumnVisibilityAndOrder();

                    // select new row
                    columnsTable.selectRow(columnRow);
                  }
                  finally {
                    columnsTable.setTableChanging(false);
                  }
                }
              }
            }

            @Order(20)
            @ClassId("da55a088-a05f-4c94-8933-c5a2a7d6ab15")
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
                execRemoveColumnAction();
              }
            }

            @Order(30)
            @ClassId("c81f96dc-5b4c-44ed-9737-63a85b22600f")
            public class MoveUpMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.AngleUp;
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

            @Order(40)
            @ClassId("8d30af92-6da2-420f-9c81-6d32928a68be")
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
                return AbstractIcons.AngleDown;
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

            @Order(50)
            @ClassId("93fe71a1-d0d4-4f01-9dab-5cf1b68e7cc9")
            public class MenuSeparator3 extends AbstractMenuSeparator {
            }

            @Order(60)
            @ClassId("679441de-450d-44c7-97dd-2950ac704266")
            public class SortAscMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
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

            @Order(70)
            @ClassId("6056fd4d-6014-4455-acaa-63438b7da7bc")
            public class SortDescMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
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

            @Order(80)
            @ClassId("730281db-2249-4d82-8e1b-8e34c3037291")
            public class SortAscAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowUpPlus;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(true, true);
              }
            }

            @Order(90)
            @ClassId("3cced8ae-dab0-496d-b2ba-31839accb261")
            public class SortDescAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.LongArrowDownPlus;
              }

              @Override
              protected void execAction() {
                sortSelectedColumn(true, false);
              }
            }

            @Order(100)
            @ClassId("6acb99fb-d66e-4699-abff-2d0bce12bb41")
            public class MenuSeparator1 extends AbstractMenuSeparator {
            }

            @Order(110)
            @ClassId("86487770-f3c2-4c0f-ac91-82b479924453")
            public class GroupMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
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

            @Order(120)
            @ClassId("fcf481fb-dc01-45ff-bf20-eab560363c44")
            public class GroupAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_organizedTable.isSortEnabled();
              }

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.GroupPlus;
              }

              @Override
              protected void execAction() {
                groupSelectedColumn(true);
              }
            }

            @Order(130)
            @ClassId("fe8106ee-1ed3-443d-bde1-02d2e440c99d")
            public class MenuSeparator2 extends AbstractMenuSeparator {
            }

            @Order(140)
            @ClassId("1636f632-aca4-4e96-ae9e-060c9d0c8317")
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
                Table columnsTable = getColumnsTableField().getTable();
                if (OrganizeColumnsForm.this.isCustomizable() && columnsTable.getSelectedRow() != null) {
                  IColumn<?> selectedCol = columnsTable.getKeyColumn().getValue(columnsTable.getSelectedRow());
                  if (isColumnModifiable(selectedCol)) {
                    m_organizedTable.getTableCustomizer().modifyColumn(selectedCol);
                    getColumnsTableField().reloadTableData();
                  }
                }
              }
            }

            @Order(150)
            @ClassId("378519e3-c7e0-40a3-b533-b9dabab44f36")
            public class RemoveFilterMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.FilterRemove;
              }

              @Override
              protected void execAction() {
                for (ITableRow selectedRow : getSelectedRows()) {
                  IColumn<?> selectedCol = getKeyColumn().getValue(selectedRow);
                  if (selectedCol.isColumnFilterActive()) {
                    m_organizedTable.getUserFilterManager().removeFilterByKey(selectedCol.getColumnId());
                  }
                }
                reloadTableData();
              }
            }
          }
        }

        @Order(80)
        @ClassId("d25a64d4-25f8-40aa-bf51-2705a5aa6bc2")
        public class CopyWidthsOfColumnsMenu extends AbstractMenu {

          @Override
          protected String getConfiguredText() {
            return TEXTS.get("CopyWidthsOfColumnsMenu");
          }

          @Override
          protected byte getConfiguredHorizontalAlignment() {
            return HORIZONTAL_ALIGNMENT_RIGHT;
          }

          @Override
          protected void execInitAction() {
            // This menu is only visible in development mode
            setVisibleGranted(Platform.get().inDevelopmentMode());
          }

          @Override
          protected void execAction() {
            StringBuilder sb = new StringBuilder();
            for (IColumn<?> column : m_organizedTable.getColumnSet().getVisibleColumns()) {
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
        }
      }
    }
  }

  public void updateColumnVisibilityAndOrder() {
    List<IColumn<?>> visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues(getColumnsTableField().getTable().getCheckedRows());
    m_organizedTable.getColumnSet().setVisibleColumns(visibleColumns);
    ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_organizedTable);
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
    enableDisableMenus();
  }

  public void moveDown(ITableRow row) {
    moveDown(row, row.getRowIndex() + 1);
  }

  public void moveDown(ITableRow row, int targetIndex) {
    if (row != null && targetIndex < getColumnsTableField().getTable().getRowCount()) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), targetIndex);
    }
    updateColumnVisibilityAndOrder();
    enableDisableMenus();
  }

  public void enableDisableMenus() {
    boolean moveUpEnabled = false,
        moveDownEnabled = false,
        addEnabled = false,
        modifyEnabled = false,
        removeEnabled = false,
        removeFilterEnabled = false;

    Table columnsTable = getColumnsTableField().getTable();
    List<ITableRow> selectedRows = columnsTable.getSelectedRows();
    addEnabled = isColumnAddable();

    for (ITableRow row : selectedRows) {
      IColumn<?> selectedColumn = columnsTable.getKeyColumn().getValue(row);
      if (isColumnMovableUp(selectedColumn)) {
        moveUpEnabled = true;
      }
      if (isColumnMovableDown(selectedColumn)) {
        moveDownEnabled = true;
      }
      if (isColumnRemovable(selectedColumn)) {
        removeEnabled = true;
      }
      if (isColumnModifiable(selectedColumn)) {
        modifyEnabled = true;
      }
      if (selectedColumn.isColumnFilterActive()) {
        removeFilterEnabled = true;
      }
    }
    setEnabledAndVisible(columnsTable, MoveUpMenu.class, moveUpEnabled);
    setEnabledAndVisible(columnsTable, MoveDownMenu.class, moveDownEnabled);
    setEnabledAndVisible(columnsTable, AddColumnMenu.class, addEnabled);
    setEnabledAndVisible(columnsTable, AddColumnEmptySpaceMenu.class, addEnabled && columnsTable.getSelectedRows().isEmpty());
    setEnabledAndVisible(columnsTable, ModifyCustomColumnMenu.class, modifyEnabled);
    setEnabledAndVisible(columnsTable, RemoveMenu.class, removeEnabled);
    setEnabledAndVisible(columnsTable, RemoveFilterMenu.class, removeFilterEnabled);
  }

  private void setEnabledAndVisible(Table columnsTable, Class<? extends IMenu> menuType, boolean enabled) {
    IMenu menu = columnsTable.getMenuByClass(menuType);
    menu.setEnabled(enabled);
    menu.setVisible(enabled);
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
    }
  }

  @Override
  public boolean isFormLoading() {
    return super.isFormLoading() || m_loading;
  }

  public void storeCurrentStateAsConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    prefs.addTableColumnsConfig(m_organizedTable, configName);
    prefs.setAllTableColumnPreferences(m_organizedTable, configName);
    if (isCustomizable()) {
      prefs.setTableCustomizerData(m_organizedTable.getTableCustomizer(), configName);
    }
  }

  public void deleteConfig(String config) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (prefs != null) {
      prefs.removeTableColumnsConfig(m_organizedTable, config);
    }
  }

  public void applyAll(String configName) {
    applyViewForConfig(configName);
    m_organizedTable.getColumnSet().applySortingAndGrouping(configName);
  }

  public void applyViewForConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (isCustomizable()) {
      byte[] tableCustomizerData = prefs.getTableCustomizerData(m_organizedTable.getTableCustomizer(), configName);
      if (tableCustomizerData != null) {
        m_organizedTable.getTableCustomizer().removeAllColumns();
        m_organizedTable.getTableCustomizer().setSerializedData(tableCustomizerData);
      }
      if (m_organizedTable.getReloadHandler() != null) {
        m_organizedTable.resetColumnConfiguration();
        m_organizedTable.getReloadHandler().reload(IReloadReason.ORGANIZE_COLUMNS);
      }
    }
    for (IColumn<?> col : m_organizedTable.getColumnSet().getColumns()) {
      col.setVisible(prefs.getTableColumnVisible(col, col.isInitialVisible(), configName));
      col.setWidth(prefs.getTableColumnWidth(col, col.getInitialWidth(), configName));
      col.setVisibleColumnIndexHint(prefs.getTableColumnViewIndex(col, col.getInitialSortIndex(), configName));
      if (col instanceof INumberColumn) {
        ((INumberColumn) col).setBackgroundEffect(prefs.getTableColumnBackgroundEffect(col, ((INumberColumn) col).getInitialBackgroundEffect(), configName));
      }
    }
  }

  public void resetAll() {
    m_organizedTable.reset(false);
    getColumnsTableField().reloadTableData();
  }

  public void resetView() {
    try {
      m_organizedTable.setTableChanging(true);
      //
      m_organizedTable.resetColumnVisibilities();
      m_organizedTable.resetColumnWidths();
      m_organizedTable.resetColumnOrder();
      ITableCustomizer cst = m_organizedTable.getTableCustomizer();
      if (cst != null) {
        cst.removeAllColumns();
      }
    }
    finally {
      m_organizedTable.setTableChanging(false);
    }
    getColumnsTableField().reloadTableData();
  }

  protected boolean isColumnMovableUp(IColumn<?> column) {
    if (column.isFixedPosition()) {
      return false;
    }
    List<IColumn<?>> visibleColumns = column.getTable().getColumnSet().getVisibleColumns();
    int index = visibleColumns.indexOf(column);
    if (index - 1 < 0) {
      return false;
    }
    return !visibleColumns.get(index - 1).isFixedPosition();
  }

  protected boolean isColumnMovableDown(IColumn<?> column) {
    if (column.isFixedPosition()) {
      return false;
    }
    List<IColumn<?>> visibleColumns = column.getTable().getColumnSet().getVisibleColumns();
    int index = visibleColumns.indexOf(column);
    if (index + 1 >= visibleColumns.size()) {
      return false;
    }
    return !visibleColumns.get(index + 1).isFixedPosition();
  }

  /**
   * Returns the enabled/visible state of the AddCustomColumn menu. Override this method if a subclass of this form
   * requires a different state for that menu.
   */
  protected boolean isColumnAddable() {
    return isCustomizable();
  }

  /**
   * Returns the enabled/visible state of the RemoveColumn menu. Override this method if a subclass of this form
   * requires a different state for that menu.
   */
  protected boolean isColumnRemovable(IColumn<?> column) {
    return isCustomizable() && m_organizedTable.getTableCustomizer().isCustomizable(column);
  }

  /**
   * Returns the enabled/visible state of the ModifyColumn menu. Override this method if a subclass of this form
   * requires a different state for that menu.
   */
  protected boolean isColumnModifiable(IColumn<?> column) {
    return column.isModifiable();
  }

  /**
   * Calls addColumn() method of table-customizer, if table has a customizer. Override this method if a different
   * behavior is required.
   */
  protected void execAddColumnAction() {
    if (isCustomizable()) {
      List<String> existingColumns = getVisibleColumnIds();
      m_organizedTable.getTableCustomizer().addColumn(null);
      getColumnsTableField().getTable().moveNewColumnsAfterSelection(existingColumns);
    }
  }

  /**
   * Calls removeColumn() method of table-customizer, if table has a customizer and selected row is a custom column.
   * Override this method if a different behavior is required.
   */
  protected void execRemoveColumnAction() {
    if (isCustomizable()) {
      Table columnsTable = getColumnsTableField().getTable();
      for (ITableRow selectedRow : columnsTable.getSelectedRows()) {
        IColumn<?> selectedColumn = columnsTable.getKeyColumn().getValue(selectedRow);
        if (isColumnRemovable(selectedColumn)) {
          m_organizedTable.getTableCustomizer().removeColumn(selectedColumn);
        }
      }
      getColumnsTableField().reloadTableData();
    }
  }

  protected boolean acceptColumnForColumnsTable(IColumn<?> column) {
    return column.isDisplayable() && (column.isVisible() || column.isVisibleGranted());
  }

  protected List<ITableRow> createColumnsTableRows(Table columnsTable) {
    List<ITableRow> rowList = new ArrayList<>();
    for (IColumn<?> col : m_organizedTable.getColumnSet().getAllColumnsInUserOrder()) {
      if (acceptColumnForColumnsTable(col)) {
        IHeaderCell headerCell = col.getHeaderCell();
        TableRow row = new TableRow(columnsTable.getColumnSet());

        // Key
        columnsTable.getKeyColumn().setValue(row, col);

        // Column Title
        String columnTitle = headerCell.getText();
        if (StringUtility.isNullOrEmpty(columnTitle)) {
          columnTitle = headerCell.getTooltipText();
          row.setFont(FontSpec.parse("ITALIC"));
        }
        else if (headerCell.isHtmlEnabled()) {
          columnTitle = BEANS.get(HtmlHelper.class).toPlainText(columnTitle);
        }
        columnsTable.getTitleColumn().setValue(row, columnTitle);

        // grouping and sorting
        List<CharSequence> cellContents = new ArrayList<>();
        if (col.isSortActive()) {
          cellContents.add(HTML.span(col.isSortAscending() ? UNICODE_ARROW_UP : UNICODE_ARROW_DOWN).cssClass("sort-symbol"));
          cellContents.add(HTML.span(String.valueOf(col.getSortIndex() + 1)).cssClass("sort-number"));
        }
        if (col.isGroupingActive()) {
          cellContents.add(HTML.span(TEXTS.get("GroupingAbbreviation")).cssClass("group-symbol"));
        }
        if (cellContents.size() > 0) {
          columnsTable.getGroupAndSortColumn().setValue(row, HTML.fragment(cellContents).toHtml());
        }

        // CustomColumn
        if (isCustomizable() && m_organizedTable.getTableCustomizer().isCustomizable(col)) {
          columnsTable.getCustomColumnColumn().setValue(row, TEXTS.get("CustomColumAbbreviation"));
        }

        // filter
        if (col.isColumnFilterActive()) {
          columnsTable.getFilterColumn().setValue(row, TEXTS.get("FilterAbbreviation"));
        }

        // width
        columnsTable.getWidthColumn().setValue(row, col.getWidth());

        rowList.add(row);
      }
    }
    return rowList;
  }

  protected List<String> getVisibleColumnIds() {
    List<IColumn<?>> visibleColumns = m_organizedTable.getColumnSet().getVisibleColumns();
    List<String> columnIds = new ArrayList<>(visibleColumns.size());
    for (IColumn<?> column : visibleColumns) {
      columnIds.add(column.getColumnId());
    }
    return columnIds;
  }

  private boolean isCustomizable() {
    return m_organizedTable.isCustomizable();
  }

  private class P_OrganizeColumnTablePropertyListener implements PropertyChangeListener {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      updateGroupingMenuVisibility();
    }
  }
}
