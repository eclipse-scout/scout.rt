package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.dnd.JavaTransferObject;
import org.eclipse.scout.commons.dnd.TransferObject;
import org.eclipse.scout.commons.exception.VetoException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.common.bookmark.internal.BookmarkUtility;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.IDNDSupport;
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
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.shared.TEXTS;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.security.CreateCustomColumnPermission;
import org.eclipse.scout.rt.shared.security.UpdateCustomColumnPermission;
import org.eclipse.scout.rt.shared.services.common.bookmark.TableColumnState;

public class OrganizeColumnsForm extends AbstractForm {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(OrganizeColumnsForm.class);

  protected enum ConfigType {
    DEFAULT, CUSTOM
  }

  private final ITable m_table;

  protected boolean m_loading;

  public OrganizeColumnsForm(ITable table) {
    super(false);
    m_table = table;
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

  @Order(10.0)
  public class MainBox extends AbstractGroupBox {

    @Order(10.0)
    public class GroupBox extends AbstractGroupBox {

      @Override
      protected Class<? extends IGroupBoxBodyGrid> getConfiguredBodyGrid() {
        return HorizontalGroupBoxBodyGrid.class;
      }

      @Override
      protected int getConfiguredGridColumnCount() {
        return 6;
      }

      @Order(5.0)
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
          return TEXTS.get("Profiles");
        }

        @Override
        protected boolean getConfiguredStatusVisible() {
          return false;
        }

        @Order(10.0)
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
              Set<String> configs = prefs.getAllTableColumnsConfigs(m_table);
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
              m_table.setTableChanging(true);
              //
              m_table.resetColumns();
              TableUserFilterManager m = m_table.getUserFilterManager();
              if (m != null) {
                m.reset();
              }
              ITableCustomizer cst = m_table.getTableCustomizer();
              if (cst != null) {
                cst.removeAllColumns();
              }
            }
            finally {
              m_table.setTableChanging(false);
            }
            getColumnsTableField().reloadTableData();
          }

          protected void resetView() {
            try {
              m_table.setTableChanging(true);
              //
              m_table.resetColumnVisibilities();
              m_table.resetColumnWidths();
              m_table.resetColumnOrder();
              ITableCustomizer cst = m_table.getTableCustomizer();
              if (cst != null) {
                cst.removeAllColumns();
              }
            }
            finally {
              m_table.setTableChanging(false);
            }
            getColumnsTableField().reloadTableData();
          }

          @Order(10.0)
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

            @Order(10.0)
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
                      P_TableState tableStateBackup = createTableStateSnpashot();
                      applyAll(oldValue);
                      deleteConfig(oldValue);
                      storeCurrentStateAsConfig(newValue);
                      restoreTableState(tableStateBackup);
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

            @Order(20.0)
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

            @Order(10.0)
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
//                ensureNewConfigRowExists();
                String newConfigName = newConfigName();
                storeCurrentStateAsConfig(newConfigName);
                ITableRow newRow = new TableRow(getTable().getColumnSet());
                getTable().getConfigTypeColumn().setValue(newRow, ConfigType.CUSTOM);
                getTable().getConfigNameColumn().setValue(newRow, newConfigName);
                try {
                  getTable().setTableChanging(true);
                  newRow = getTable().addRow(newRow);
                  newRow.getCellForUpdate(getTable().getConfigNameColumn()).setEditable(false);
                }
                finally {
                  getTable().setTableChanging(false);
                }
              }

            }

            @Order(20.0)
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
              }

            }

            @Order(30.0)
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
                // FIXME ASA open and focus cell for edit, once it is supported.
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
              String baseName = TEXTS.get("Profile") + " ";
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

            @Order(40.0)
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

            @Order(50.0)
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
              }
            }
          }

        }
      }

      @Order(10.0)
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

        @Order(10.0)
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
            for (IColumn<?> col : m_table.getColumnSet().getAllColumnsInUserOrder()) {
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
                    // FIXME ASA when decided that we keep "G", instead of symbol: create translated text
                    sb.append("G");
                  }
                  getTable().getGroupAndSortColumn().setValue(row, sb.toString());

                  // CustomColumn
                  if (col instanceof ICustomColumn<?>) {
                    // FIXME ASA when decided that we keep "C", instead of symbol: create translated text
                    getTable().getCustomColumnColumn().setValue(row, "C");
                  }

                  // filter
                  if (col.isColumnFilterActive()) {
                    // FIXME ASA when decided that we keep "F", instead of symbol: create translated text
                    getTable().getFilterColumn().setValue(row, "F");
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
              getMenuByClass(AddCustomColumnMenu.class).setVisible(m_table.getTableCustomizer() != null);
              getMenuByClass(ModifyCustomColumnMenu.class).setVisiblePermission(new UpdateCustomColumnPermission());
              getMenuByClass(ModifyCustomColumnMenu.class).setVisible(m_table.getTableCustomizer() != null);
            }

            private void refreshMenus() {
              ITableRow selectedRow = getColumnsTableField().getTable().getSelectedRow();
              IColumn selectedCol = getColumnsTableField().getTable().getKeyColumn().getValue(selectedRow);
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
                  m_table.getColumnSet().removeSortColumn(selectedCol);
                }
                else {
                  m_table.getColumnSet().handleSortEvent(selectedCol, multiSort, ascending);
                }
                m_table.sort();

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
                  m_table.getColumnSet().removeGroupColumn(selectedCol);
                }
                else {
                  boolean ascending = true;
                  if (selectedCol.isSortActive()) {
                    ascending = selectedCol.isSortAscending();
                  }
                  m_table.getColumnSet().handleGroupingEvent(selectedCol, multiGroup, ascending);
                }
                m_table.sort();

                getColumnsTableField().reloadTableData();
                getColumnsTableField().getTable().selectRow(row.getRowIndex());
              }
              finally {
                getColumnsTableField().getTable().setTableChanging(false);
              }
              refreshMenus();
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

            @Order(30.0)
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

            @Order(40.0)
            public class GroupAndSortColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 40;
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
                return 40;
              }

            }

            @Order(60.0)
            public class CustomColumnColumn extends AbstractStringColumn {

              @Override
              protected int getConfiguredWidth() {
                return 40;
              }

            }

            @Order(70.0)
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
            @Order(80.0)
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

            @Order(10.0)
            public class SortAscMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(20.0)
            public class SortDescMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(30.0)
            public class SortAscAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(40.0)
            public class SortDescAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(41.0)
            public class MenuSeparator1 extends AbstractMenuSeparator {
            }

            @Order(44.0)
            public class GroupMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(46.0)
            public class GroupAdditionalMenu extends AbstractMenu {

              @Override
              protected boolean getConfiguredEnabled() {
                return m_table.isSortEnabled();
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

            @Order(49.0)
            public class MenuSeparator2 extends AbstractMenuSeparator {
            }

            @Order(50.0)
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

            @Order(60.0)
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

            @Order(69.0)
            public class MenuSeparator3 extends AbstractMenuSeparator {
            }

            @Order(70.0)
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
                            moveDown(newColumnRow, targetOrderRow.getRowIndex());
                          }
                          else {
                            moveUp(newColumnRow, targetOrderRow.getRowIndex());
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

            @Order(80.0)
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

            @Order(90.0)
            public class RemoveMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredIconId() {
                return AbstractIcons.Remove;
              }

              @Override
              protected String getConfiguredKeyStroke() {
                return AbstractKeyStroke.DELETE;
              }

              @Override
              protected void execAction() {
                if (m_table != null) {
                  for (ITableRow selectedRow : getSelectedRows()) {
                    IColumn<?> selectedCol = getKeyColumn().getValue(selectedRow);
                    if (selectedCol instanceof ICustomColumn<?>) {
                      if (m_table.getTableCustomizer() != null) {
                        m_table.getTableCustomizer().removeColumn((ICustomColumn<?>) selectedCol);
                      }
                    }
                    // FIXME ASA either remove completely or elaborate
//                    else {
//                      if (selectedCol.isColumnFilterActive()) {
//                        m_table.getUserFilterManager().removeFilterByKey(selectedCol);
//                      }
//                      setColumnVisible(selectedRow, false);
//                    }

                  }
                }
                reloadTableData();
              }
            }

            @Order(100.0)
            public class RemoveFilterMenu extends AbstractMenu {

              @Override
              protected Set<? extends IMenuType> getConfiguredMenuTypes() {
                return CollectionUtility.<IMenuType> hashSet(TableMenuType.SingleSelection, TableMenuType.MultiSelection);
              }

              @Override
              protected String getConfiguredText() {
                return "F x";
              }

              @Override
              protected void execAction() {
                if (m_table != null) {
                  for (ITableRow selectedRow : getSelectedRows()) {
                    IColumn<?> selectedCol = getKeyColumn().getValue(selectedRow);
                    if (selectedCol.isColumnFilterActive()) {
                      m_table.getUserFilterManager().removeFilterByKey(selectedCol);
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

    @Order(80.0)
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
          for (IColumn<?> column : m_table.getColumnSet().getVisibleColumns()) {
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
          throw BEANS.get(ProcessingExceptionTranslator.class).translateAndAddContextMessages(e, getLabel());
        }
      }
    }

  }

  private void updateColumnVisibilityAndOrder() {
    List<IColumn<?>> visibleColumns = getColumnsTableField().getTable().getKeyColumn().getValues(getColumnsTableField().getTable().getCheckedRows());
    m_table.getColumnSet().setVisibleColumns(visibleColumns);
    ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_table);
  }

  private void setColumnVisible(ITableRow row, Boolean visible) {
    getColumnsTableField().getTable().checkRow(row, visible);

    updateColumnVisibilityAndOrder();
  }

  private void moveUp(ITableRow row) {
    moveUp(row, row.getRowIndex() - 1);
  }

  private void moveUp(ITableRow row, int targetIndex) {
    if (row != null && targetIndex >= 0) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), targetIndex);
    }
    touch();
    updateColumnVisibilityAndOrder();
  }

  private void moveDown(ITableRow row) {
    moveDown(row, row.getRowIndex() + 1);
  }

  private void moveDown(ITableRow row, int targetIndex) {
    if (row != null && targetIndex < getColumnsTableField().getTable().getRowCount()) {
      getColumnsTableField().getTable().moveRow(row.getRowIndex(), targetIndex);
    }
    touch();
    updateColumnVisibilityAndOrder();
  }

  private void enableDisableMenus() {
    boolean isCustomColumnSelected = false;
    boolean isFilterActive = false;
    List<ITableRow> selectedRows = getColumnsTableField().getTable().getSelectedRows();
    for (ITableRow row : selectedRows) {
      if (getColumnsTableField().getTable().getKeyColumn().getValue(row).isColumnFilterActive()) {
        isFilterActive = true;
      }
      if (getColumnsTableField().getTable().getKeyColumn().getValue(row) instanceof ICustomColumn<?>) {
        isCustomColumnSelected = true;
      }
    }

    getColumnsTableField().getTable().getMenuByClass(ModifyCustomColumnMenu.class).setEnabled(isCustomColumnSelected);
    getColumnsTableField().getTable().getMenuByClass(ModifyCustomColumnMenu.class).setVisible(isCustomColumnSelected);
    getColumnsTableField().getTable().getMenuByClass(RemoveMenu.class).setEnabled(isCustomColumnSelected);
    getColumnsTableField().getTable().getMenuByClass(RemoveMenu.class).setVisible(isCustomColumnSelected);

    getColumnsTableField().getTable().getMenuByClass(RemoveFilterMenu.class).setVisible(isFilterActive);

  }

  @Override
  public void validateForm() {
    boolean oneColumnIsVisble = getColumnsTableField().getTable().getCheckedRows().size() > 0;

    if (!oneColumnIsVisble) {
      throw new VetoException(TEXTS.get("OrganizeTableColumnsMinimalColumnCountMessage"));
    }
  }

  /**
   * complete state (config and data)
   */
  protected static class P_TableState {
    protected byte[] m_tableCustomizerData;
    protected byte[] m_userFilterData;
    protected List<TableColumnState> m_columnStates;
    protected Object[][] m_data;

    public byte[] getTableCustomizerData() {
      return m_tableCustomizerData;
    }

    public void setTableCustomizerData(byte[] tableCustomizerData) {
      m_tableCustomizerData = tableCustomizerData;
    }

    public byte[] getUserFilterData() {
      return m_userFilterData;
    }

    public void setUserFilterData(byte[] userFilterData) {
      m_userFilterData = userFilterData;
    }

    public List<TableColumnState> getColumnStates() {
      return m_columnStates;
    }

    public void setColumnStates(List<TableColumnState> columnStates) {
      m_columnStates = columnStates;
    }

    public Object[][] getData() {
      return m_data;
    }

    public void setData(Object[][] data) {
      m_data = data;
    }

  }

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

  protected P_TableState createTableStateSnpashot() {
    P_TableState tableState = new P_TableState();
    if (m_table.getTableCustomizer() != null) {

      tableState.setTableCustomizerData(m_table.getTableCustomizer().getSerializedData());
    }
    tableState.setColumnStates(BookmarkUtility.backupTableColumns(m_table));
    tableState.setData(m_table.getTableData());
    if (m_table.getUserFilterManager() != null) {
      tableState.setUserFilterData(m_table.getUserFilterManager().getSerializedData());
    }
    return tableState;
  }

  protected void restoreTableState(P_TableState tableState) {
    try {
      m_table.setTableChanging(true);
      if (m_table.getTableCustomizer() != null) {
        m_table.getTableCustomizer().removeAllColumns();
        m_table.getTableCustomizer().setSerializedData(tableState.getTableCustomizerData());
        ClientUIPreferences.getInstance().setAllTableColumnPreferences(m_table);
      }
      m_table.resetColumnConfiguration();
      BookmarkUtility.restoreTableColumns(m_table, tableState.getColumnStates());
      if (m_table.getUserFilterManager() != null) {
        m_table.getUserFilterManager().setSerializedData(tableState.getUserFilterData());
      }
      m_table.addRowsByMatrix(tableState.getData());
    }
    finally {
      m_table.setTableChanging(false);
    }
  }

  protected void storeCurrentStateAsConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (prefs != null) {
      prefs.addTableColumnsConfig(m_table, configName);
      prefs.setAllTableColumnPreferences(m_table, configName);
      if (m_table.getTableCustomizer() != null) {
        prefs.setTableCustomizerData(m_table.getTableCustomizer(), configName);
      }
    }
  }

  protected void deleteConfig(String config) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (prefs != null) {
      prefs.removeTableColumnsConfig(m_table, config);
    }
  }

  protected void applyAll(String configName) {
    applyViewForConfig(configName);
    m_table.getColumnSet().applySortingAndGrouping(configName);
  }

  protected void applyViewForConfig(String configName) {
    ClientUIPreferences prefs = ClientUIPreferences.getInstance();
    if (m_table.getTableCustomizer() != null) {
      byte[] tableCustomizerData = prefs.getTableCustomizerData(m_table.getTableCustomizer(), configName);
      if (tableCustomizerData != null) {
        m_table.getTableCustomizer().removeAllColumns();
        m_table.getTableCustomizer().setSerializedData(tableCustomizerData);
      }
      m_table.resetColumnConfiguration();
      m_table.getReloadHandler().reload();
    }
    for (IColumn<?> col : m_table.getColumnSet().getColumns()) {
      col.setVisible(prefs.getTableColumnVisible(col, col.isInitialVisible(), configName));
      col.setWidth(prefs.getTableColumnWidth(col, col.getInitialWidth(), configName));
      col.setVisibleColumnIndexHint(prefs.getTableColumnViewIndex(col, col.getInitialSortIndex(), configName));
      if (col instanceof INumberColumn) {
        ((INumberColumn) col).setBackgroundEffect(prefs.getTableColumnBackgroundEffect(col, ((INumberColumn) col).getInitialBackgroundEffect(), configName));
      }
    }
  }
}
