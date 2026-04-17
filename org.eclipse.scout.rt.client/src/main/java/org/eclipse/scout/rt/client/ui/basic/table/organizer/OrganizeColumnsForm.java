/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.table.organizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.action.AbstractAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.AbstractKeyStroke;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
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
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.AddColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.ModifyCustomColumnMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.MoveDownMenu;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.OrganizeColumnsForm.MainBox.GroupBox.ColumnsGroupBox.ColumnsTableField.Table.MoveUpMenu;
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
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.html.HTML;
import org.eclipse.scout.rt.platform.html.HtmlHelper;
import org.eclipse.scout.rt.platform.html.IHtmlContent;
import org.eclipse.scout.rt.platform.html.IHtmlElement;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.CssClasses;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.ui.UserAgentUtility;

@ClassId("5bd26d3c-604d-4991-a246-7fff74e32faa")
public class OrganizeColumnsForm extends AbstractForm implements IOrganizeColumnsForm {

  private static final String UNICODE_ARROW_UP = "↑"; // U+2191
  private static final String UNICODE_ARROW_DOWN = "↓"; // U+2193

  public enum ConfigType {
    DEFAULT, CUSTOM
  }

  private final ITable m_organizedTable;

  protected boolean m_loading;

  public OrganizeColumnsForm(ITable table) {
    super(false);
    m_organizedTable = table;
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

  @Override
  protected Boolean getConfiguredHeaderVisible() {
    return true;
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
  @ClassId("d9f2e54a-cb41-4453-8ce9-ba41b8e247bd")
  public class MainBox extends AbstractGroupBox {

    @Override
    protected int getConfiguredWidthInPixel() {
      return 750;
    }

    @Override
    protected int getConfiguredHeightInPixel() {
      return 350;
    }

    @Override
    protected boolean execIsEmpty() {
      // Changes on this form must not be propagated to the parent which eventually is a table.
      // Otherwise, the table would be marked as non-empty as soon as the tables on this form are changed.
      return true;
    }

    @Order(10)
    @ClassId("abaf2e0c-1c14-4b99-81dc-8b83453f5766")
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
        return HorizontalGroupBoxBodyGrid.class;
      }

      @Override
      protected boolean getConfiguredBorderVisible() {
        return false;
      }

      @Order(5)
      @ClassId("698da86a-d878-439e-9a1c-da7b63d4f2e3")
      public class ProfilesBox extends AbstractGroupBox {

        @Override
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

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
            protected boolean getConfiguredHeaderEnabled() {
              return false;
            }

            @Override
            protected String getConfiguredCssClass() {
              return "table-organizer-profiles-table";
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

              if (rows.size() > 0) {
                getColumnsTableField().getTable().deselectAllRows();
              }
            }

            @Order(10)
            @ClassId("f607ab39-e616-4b27-b7c4-f6e437b6b1a3")
            public class ConfigNameColumn extends AbstractAlphanumericSortingStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("SavedSettings");
              }

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
                field.selectAll();
                return field;
              }

              @Override
              protected void execCompleteEdit(ITableRow row, IFormField editingField) {
                String oldValue = getConfigNameColumn().getValue(row);
                super.execCompleteEdit(row, editingField);
                String newValue = ((IStringField) editingField).getValue();
                if (!StringUtility.isNullOrEmpty(newValue)) {
                  switch (getConfigTypeColumn().getValue(row)) {
                    case CUSTOM -> {
                      ClientUIPreferences prefs = ClientUIPreferences.getInstance();
                      prefs.renameTableColumnsConfig(m_organizedTable, oldValue, newValue);
                    }
                    default -> throw new IllegalStateException("Rows of configType " + getConfigTypeColumn().getValue(row).name() + " should never be editable.");
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
                  applyConfig(configName);
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
        protected boolean getConfiguredLabelVisible() {
          return false;
        }

        @Override
        protected int getConfiguredGridW() {
          return 1;
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
              columnsTable.addRows(rowList);
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
            protected String getConfiguredCssClass() {
              return CssClasses.NO_MENUBAR_SEPARATORS;
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
            protected boolean getConfiguredHeaderEnabled() {
              return false;
            }

            public KeyColumn getKeyColumn() {
              return getColumnSet().getColumnByClass(KeyColumn.class);
            }

            public StatusColumn getStatusColumn() {
              return getColumnSet().getColumnByClass(StatusColumn.class);
            }

            public WidthColumn getWidthColumn() {
              return getColumnSet().getColumnByClass(WidthColumn.class);
            }

            public TitleColumn getTitleColumn() {
              return getColumnSet().getColumnByClass(TitleColumn.class);
            }

            @Override
            protected void execRowsSelected(List<? extends ITableRow> rows) {
              enableDisableMenus();
              if (rows.size() > 0) {
                getProfilesTableField().getTable().deselectAllRows();
              }
            }

            @Override
            protected void execInitTable() {
              getWidthColumn().setVisible(!m_organizedTable.isAutoResizeColumns());
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
                return TEXTS.get("Column");
              }

              @Override
              protected int getConfiguredWidth() {
                return 120;
              }
            }

            @Order(40)
            @ClassId("23b153bc-1d74-46a2-b08b-87aecba6c1b0")
            public class StatusColumn extends AbstractStringColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("Status");
              }

              @Override
              protected int getConfiguredWidth() {
                return 70;
              }

              @Override
              protected boolean getConfiguredFixedWidth() {
                return true;
              }

              @Override
              protected boolean getConfiguredHtmlEnabled() {
                return true;
              }

              @Override
              protected String getConfiguredCssClass() {
                return "table-organizer-column-status-cell";
              }

              @Override
              protected void execDecorateCell(Cell cell, ITableRow row) {
                cell.setTooltipText(computeColumnStatusTooltip(getKeyColumn().getValue(row)));
              }
            }

            @Order(70)
            @ClassId("c0bfe89c-2402-419a-bda1-68fc61b23ec7")
            public class WidthColumn extends AbstractIntegerColumn {

              @Override
              protected String getConfiguredHeaderText() {
                return TEXTS.get("Width");
              }

              @Override
              protected boolean getConfiguredAutoOptimizeWidth() {
                return true;
              }

              @Override
              protected int getConfiguredAutoOptimizeMaxWidth() {
                // Size of width in Italian.
                // This column should not take too much space, so if another language has a larger word it will show ellipsis.
                return 85;
              }
            }

            @Order(10)
            @ClassId("36a172fa-c7ef-4682-9724-6cfdd950907a")
            public class AddColumnMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Plus;
              }

              @Override
              protected String getConfiguredTooltipText() {
                return TEXTS.get("AddColumn");
              }

              @Override
              protected void execAction() {
                execAddColumnAction();
              }
            }

            @Order(20)
            @ClassId("da55a088-a05f-4c94-8933-c5a2a7d6ab15")
            public class RemoveMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
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
              protected String getConfiguredTooltipText() {
                return TEXTS.get("RemoveColumn");
              }

              @Override
              protected void execAction() {
                execRemoveColumnAction();
              }
            }

            @Order(25)
            @ClassId("1636f632-aca4-4e96-ae9e-060c9d0c8317")
            public class ModifyCustomColumnMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Pencil;
              }

              @Override
              protected String getConfiguredTooltipText() {
                return TEXTS.get("ModifyColumn");
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

            @Order(30)
            @ClassId("c81f96dc-5b4c-44ed-9737-63a85b22600f")
            public class MoveUpMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
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
              protected byte getConfiguredHorizontalAlignment() {
                return HORIZONTAL_ALIGNMENT_RIGHT;
              }

              @Override
              protected String getConfiguredTooltipText() {
                return TEXTS.get("MoveColumnForward");
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
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.EmptySpace);
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
              protected byte getConfiguredHorizontalAlignment() {
                return HORIZONTAL_ALIGNMENT_RIGHT;
              }

              @Override
              protected String getConfiguredTooltipText() {
                return TEXTS.get("MoveColumnBackward");
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
          }
        }
      }
    }
  }

  public void updateColumnVisibilityAndOrder() {
    List<IColumn<?>> visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues();
    m_organizedTable.getColumnSet().setVisibleColumns(visibleColumns);
    ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_organizedTable);
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
        removeEnabled = false;

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
    }
    setEnabledAndVisible(columnsTable, AddColumnMenu.class, addEnabled);
    setEnabledAndVisible(columnsTable, ModifyCustomColumnMenu.class, modifyEnabled);
    setEnabledAndVisible(columnsTable, RemoveMenu.class, removeEnabled);

    // The move-menus should always be visible, otherwise their position changes when row is moved to top or bottom
    columnsTable.getMenuByClass(MoveUpMenu.class).setEnabled(moveUpEnabled);
    columnsTable.getMenuByClass(MoveDownMenu.class).setEnabled(moveDownEnabled);
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

  public void resetAll() {
    m_organizedTable.reset(false);
    ClientUIPreferences.getInstance().removeTableColumnsConfig(m_organizedTable, null); // clear global profile
    if (isCustomizable() && m_organizedTable.getReloadHandler() != null) {
      m_organizedTable.getReloadHandler().reload(IReloadReason.ORGANIZE_COLUMNS);
    }
    getColumnsTableField().reloadTableData();
  }

  public void applyConfig(String configName) {
    applyConfigImpl(configName);
    ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_organizedTable, null); // save new state as global profile
    if (isCustomizable() && m_organizedTable.getReloadHandler() != null) {
      m_organizedTable.getReloadHandler().reload(IReloadReason.ORGANIZE_COLUMNS);
    }
    getColumnsTableField().reloadTableData();
    // After reloading, the columnsTableField now contains the expected columns in the correct order
    // -> ensure the order of columns is correctly applied to the table
    updateColumnVisibilityAndOrder();
  }

  protected void applyConfigImpl(String configName) {
    try {
      m_organizedTable.setTableChanging(true);

      ClientUIPreferences prefs = ClientUIPreferences.getInstance();
      if (isCustomizable()) {
        byte[] tableCustomizerData = prefs.getTableCustomizerData(m_organizedTable.getTableCustomizer(), configName);
        if (tableCustomizerData != null) {
          m_organizedTable.getTableCustomizer().removeAllColumns();
          m_organizedTable.getTableCustomizer().setSerializedData(tableCustomizerData);
        }
        if (m_organizedTable.getReloadHandler() != null) {
          m_organizedTable.resetColumnConfiguration();
        }
      }
      for (IColumn<?> col : m_organizedTable.getColumnSet().getColumns()) {
        col.setVisible(prefs.getTableColumnVisible(col, col.isInitialVisible(), configName));
        col.setWidth(prefs.getTableColumnWidth(col, col.getInitialWidth(), configName));
        col.setVisibleColumnIndexHint(prefs.getTableColumnViewIndex(col, col.getInitialSortIndex(), configName));
        if (col instanceof INumberColumn numberColumn) {
          numberColumn.setBackgroundEffect(prefs.getTableColumnBackgroundEffect(col, numberColumn.getInitialBackgroundEffect(), configName));
        }
      }

      m_organizedTable.getColumnSet().applySortingAndGrouping(configName);
    }
    finally {
      m_organizedTable.setTableChanging(false);
    }
  }

  protected boolean isColumnMovableUp(IColumn<?> column) {
    if (column.isFixedPosition() || !column.isVisible()) {
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
    if (column.isFixedPosition() || !column.isVisible()) {
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
    return getOrganizedTable().getTableOrganizer().isColumnAddable();
  }

  /**
   * Returns the enabled/visible state of the RemoveColumn menu. Override this method if a subclass of this form
   * requires a different state for that menu.
   */
  protected boolean isColumnRemovable(IColumn<?> column) {
    return column.isRemovable();
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
    Table columnsTable = getColumnsTableField().getTable();
    List<String> existingColumnIds = new ArrayList<>();
    columnsTable.getKeyColumn().getValues().forEach(col -> existingColumnIds.add(col.getColumnId()));

    IColumn<?> insertAfterColumn = null;
    List<IColumn<?>> selectedColumns = columnsTable.getKeyColumn().getSelectedValues();
    if (selectedColumns.size() > 0) {
      insertAfterColumn = selectedColumns.get(selectedColumns.size() - 1);
    }
    getOrganizedTable().getTableOrganizer().addColumn(insertAfterColumn);
    getColumnsTableField().reloadTableData();

    // Select added rows
    List<ITableRow> newRows = columnsTable.getRows().stream().filter(row -> !existingColumnIds.contains(columnsTable.getKeyColumn().getValue(row).getColumnId())).collect(Collectors.toList());
    columnsTable.selectRows(newRows);
  }

  /**
   * Calls removeColumn() method of table-customizer, if table has a customizer and selected row is a custom column.
   * Override this method if a different behavior is required.
   */
  protected void execRemoveColumnAction() {
    Table columnsTable = getColumnsTableField().getTable();
    for (ITableRow selectedRow : columnsTable.getSelectedRows()) {
      IColumn<?> selectedColumn = columnsTable.getKeyColumn().getValue(selectedRow);
      getOrganizedTable().getTableOrganizer().removeColumn(selectedColumn);
    }
    getColumnsTableField().reloadTableData();
  }

  protected boolean acceptColumnForColumnsTable(IColumn<?> column) {
    return column.isVisible();
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

        // Status
        columnsTable.getStatusColumn().setValue(row, computeColumnStatus(col));

        // Width
        columnsTable.getWidthColumn().setValue(row, col.getWidth());

        rowList.add(row);
      }
    }
    return rowList;
  }

  protected String computeColumnStatus(IColumn<?> column) {
    String groupSymbol = "G";
    String filterSymbol = "F";
    String sortSymbol = column.isSortAscending() ? UNICODE_ARROW_UP : UNICODE_ARROW_DOWN;
    String sortIndex = String.valueOf(column.getSortIndex() + 1);
    int sortCount = getOrganizedTable().getColumnSet().getSortColumnCount();
    IHtmlElement htmlGrouped = HTML.div(groupSymbol).toggleCssClass("hidden", !column.isGroupingActive());
    IHtmlElement htmlFiltered = HTML.div(filterSymbol).toggleCssClass("hidden", !column.isColumnFilterActive());
    if (!column.isGroupingActive() && !column.isColumnFilterActive()) {
      htmlGrouped.removeCssClass("hidden").addCssClass("invisible");
    }
    IHtmlContent container = HTML.fragment(
        HTML.div(
            HTML.span(htmlGrouped, htmlFiltered).addCssClass("group-filter"),
            HTML.span(sortSymbol).addCssClass("sort-direction").toggleCssClass("invisible", !column.isSortActive()),
            HTML.span(sortIndex).addCssClass("sort-index").toggleCssClass("invisible", !column.isSortActive() || sortCount <= 1)
        ).addCssClass("status")
    );
    return container.toHtml();
  }

  protected String computeColumnStatusTooltip(IColumn<?> column) {
    List<String> result = new ArrayList<>();
    if (column.isGroupingActive()) {
      result.add(TEXTS.get("Grouped"));
    }
    if (column.isColumnFilterActive()) {
      result.add(TEXTS.get("Filtered"));
    }
    if (column.isSortActive()) {
      result.add(TEXTS.get("Sorted"));
    }
    return StringUtility.join("\n", result);
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
}
