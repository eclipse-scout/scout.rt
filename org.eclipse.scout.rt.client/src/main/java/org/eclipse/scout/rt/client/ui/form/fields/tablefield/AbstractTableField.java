/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tablefield;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.scout.rt.client.dto.FormData;
import org.eclipse.scout.rt.client.dto.FormData.DefaultSubtypeSdkCommand;
import org.eclipse.scout.rt.client.dto.FormData.SdkCommand;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.ITableFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldReloadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveDeletedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveInsertedRowChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.tablefield.TableFieldChains.TableFieldSaveUpdatedRowChain;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableListener;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValidateContentDescriptor;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.XmlUtility;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.tablefield.AbstractTableFieldBeanData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

@ClassId("76887bde-6815-4f7d-9cbd-60409b49488d")
@FormData(value = AbstractTableFieldBeanData.class, sdkCommand = SdkCommand.USE, defaultSubtypeSdkCommand = DefaultSubtypeSdkCommand.CREATE)
public abstract class AbstractTableField<T extends ITable> extends AbstractFormField implements ITableField<T> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTableField.class);

  private T m_table;
  private boolean m_tableExternallyManaged;
  private TableListener m_managedTableListener;

  public AbstractTableField() {
    this(true);
  }

  public AbstractTableField(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * Called when the table data is reloaded, i.e. when {@link #reloadTableData()} is called.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(190)
  protected void execReloadTableData() {
  }

  /**
   * Called for batch processing when the table is saved. See {@link #doSave()} for the call order.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(200)
  protected void execSave(List<? extends ITableRow> insertedRows, List<? extends ITableRow> updatedRows, List<? extends ITableRow> deletedRows) {
  }

  /**
   * Called to handle deleted rows when the table is saved. See {@link #doSave()} for the call order.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(210)
  protected void execSaveDeletedRow(ITableRow row) {
  }

  /**
   * Called to handle inserted rows when the table is saved. See {@link #doSave()} for the call order.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(220)
  protected void execSaveInsertedRow(ITableRow row) {
  }

  /**
   * Called to handle updated rows when the table is saved. See {@link #doSave()} for the call order.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(230)
  protected void execSaveUpdatedRow(ITableRow row) {
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) {
    reloadTableData();
  }

  protected Class<? extends ITable> getConfiguredTable() {
    Class<?>[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITable>> f = ConfigurationUtility.filterClasses(dca, ITable.class);
    if (f.size() == 1) {
      return CollectionUtility.firstElement(f);
    }
    else {
      for (Class<? extends ITable> c : f) {
        if (c.getDeclaringClass() != AbstractTableField.class) {
          return c;
        }
      }
      return null;
    }
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a table field is 3.
   */
  @Override
  protected int getConfiguredGridH() {
    return 3;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setValidateContentDescriptor(new ValidateTableFieldDescriptor(this));
    setTableInternal(createTable());
  }

  @SuppressWarnings("unchecked")
  protected T createTable() {
    List<ITable> contributedFields = m_contributionHolder.getContributionsByClass(ITable.class);
    ITable result = CollectionUtility.firstElement(contributedFields);
    if (result != null) {
      return (T) result;
    }

    Class<? extends ITable> configuredTable = getConfiguredTable();
    if (configuredTable != null) {
      return (T) ConfigurationUtility.newInnerInstance(this, configuredTable);
    }
    return null;
  }

  /*
   * Runtime
   */

  @Override
  protected void disposeChildren(List<? extends IWidget> widgetsToDispose) {
    if (m_tableExternallyManaged) {
      widgetsToDispose.remove(getTable());
    }
    super.disposeChildren(widgetsToDispose);
  }

  @Override
  protected void initChildren(List<? extends IWidget> widgets) {
    if (m_tableExternallyManaged) {
      widgets.remove(getTable());
    }
    super.initChildren(widgets);
  }

  @Override
  public final T getTable() {
    return m_table;
  }

  @Override
  public void setTable(T newTable, boolean externallyManaged) {
    m_tableExternallyManaged = externallyManaged;
    setTableInternal(newTable);
  }

  protected void setTableInternal(T table) {
    if (m_table == table) {
      return;
    }
    if (m_table != null) {
      m_table.setParentInternal(null);
      if (!m_tableExternallyManaged && m_managedTableListener != null) {
        m_table.removeTableListener(m_managedTableListener);
        m_managedTableListener = null;
      }
      if (isInitConfigDone()) {
        for (int i = m_valueChangeTriggerEnabled; i <= 0; ++i) {
          m_table.setValueChangeTriggerEnabled(true);
        }
      }
    }
    m_table = table;
    if (m_table != null) {
      m_table.setParentInternal(this);
      if (!m_tableExternallyManaged) {
        m_managedTableListener = e -> {
          checkSaveNeeded();
          checkEmpty();
        };
        m_table.addTableListener(
            m_managedTableListener,
            TableEvent.TYPE_ALL_ROWS_DELETED,
            TableEvent.TYPE_ROWS_DELETED,
            TableEvent.TYPE_ROWS_INSERTED,
            TableEvent.TYPE_ROWS_UPDATED,
            TableEvent.TYPE_COLUMN_STRUCTURE_CHANGED);
      }
      if (isInitConfigDone()) {
        for (int i = m_valueChangeTriggerEnabled; i <= 0; ++i) {
          m_table.setValueChangeTriggerEnabled(false);
        }
      }
    }
    boolean changed = propertySupport.setProperty(PROP_TABLE, m_table);
    if (changed) {
      if (getForm() != null) {
        getForm().structureChanged(this);
      }
      updateKeyStrokes();
    }
  }

  @Override
  public void exportFormFieldData(AbstractFormFieldData target) {
    if (m_table != null && target instanceof AbstractTableFieldBeanData) {
      AbstractTableFieldBeanData tableBeanData = (AbstractTableFieldBeanData) target;
      m_table.exportToTableBeanData(tableBeanData);
      target.setValueSet(true);
    }
  }

  @Override
  public void importFormFieldData(AbstractFormFieldData source, boolean valueChangeTriggersEnabled) {
    Assertions.assertNotNull(source);
    if (source.isValueSet() && m_table != null) {
      try {
        if (!valueChangeTriggersEnabled) {
          setValueChangeTriggerEnabled(false);
        }
        if (source instanceof AbstractTableFieldBeanData) {
          AbstractTableFieldBeanData tableBeanData = (AbstractTableFieldBeanData) source;
          m_table.importFromTableBeanData(tableBeanData);
        }
        if (m_table.isCheckable() && m_table.getCheckableColumn() != null) {
          for (ITableRow row : m_table.getRows()) {
            row.setChecked(BooleanUtility.nvl(m_table.getCheckableColumn().getValue(row)));
          }
        }
      }
      finally {
        if (!valueChangeTriggersEnabled) {
          setValueChangeTriggerEnabled(true);
        }
      }
    }
  }

  @Override
  public boolean loadFromXml(Element x) {
    boolean success = super.loadFromXml(x);
    if (m_table != null) {
      int[] selectedRowIndices = null;
      try {
        selectedRowIndices = (int[]) XmlUtility.getObjectAttribute(x, "selectedRowIndices");
      }
      catch (Exception e) {
        LOG.warn("Could not read attribute 'selectedRowIndices'", e);
        success = false;
      }
      Object[][] dataMatrix = null;
      try {
        dataMatrix = (Object[][]) XmlUtility.getObjectAttribute(x, "rows");
      }
      catch (Exception e) {
        LOG.warn("Could not read attribute 'rows'", e);
        success = false;
      }
      m_table.discardAllRows();
      if (dataMatrix != null && dataMatrix.length > 0) {
        m_table.addRowsByMatrix(dataMatrix);
      }
      if (selectedRowIndices != null && selectedRowIndices.length > 0) {
        m_table.selectRows(m_table.getRows(selectedRowIndices));
      }
    }
    return success;
  }

  @Override
  public void storeToXml(Element x) {
    super.storeToXml(x);
    if (m_table != null) {
      List<ITableRow> selectedRows = m_table.getSelectedRows();
      int[] selectedRowIndices = new int[selectedRows.size()];
      int i = 0;
      for (ITableRow selrow : selectedRows) {
        selectedRowIndices[i] = selrow.getRowIndex();
        i++;
      }
      try {
        XmlUtility.setObjectAttribute(x, "selectedRowIndices", selectedRowIndices);
      }
      catch (Exception e) {
        LOG.warn("writing attribute 'selectedRowIndices'", e);
      }
      Object[][] dataMatrix = m_table.getTableData();
      for (int r = 0; r < dataMatrix.length; r++) {
        for (int c = 0; c < dataMatrix[r].length; c++) {
          Object o = dataMatrix[r][c];
          if (o != null && !(o instanceof Serializable)) {
            LOG.warn("ignoring not serializable value at row={}, col={}: {}[{}]", r, c, o, o.getClass());
            dataMatrix[r][c] = null;
          }
        }
      }
      try {
        XmlUtility.setObjectAttribute(x, "rows", dataMatrix);
      }
      catch (Exception e) {
        LOG.warn("writing attribute 'rows'", e);
      }
    }
  }

  @Override
  protected boolean execIsSaveNeeded() {
    boolean saveNeeded = super.execIsSaveNeeded();
    if (saveNeeded) {
      return true;
    }

    if (m_table == null || m_tableExternallyManaged) {
      return false;
    }
    return m_table.getDeletedRowCount() > 0
        || m_table.getInsertedRowCount() > 0
        || m_table.getUpdatedRowCount() > 0;
  }

  @Override
  protected void execMarkSaved() {
    super.execMarkSaved();
    if (m_table != null && !m_tableExternallyManaged) {
      try {
        m_table.setTableChanging(true);
        for (int i = 0; i < m_table.getRowCount(); i++) {
          ITableRow row = m_table.getRow(i);
          if (!row.isStatusNonchanged()) {
            row.setStatusNonchanged();
          }
        }
        m_table.discardAllDeletedRows();
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
  }

  @Override
  protected boolean execIsEmpty() {
    if (!super.execIsEmpty()) {
      return false;
    }
    if (m_table != null) {
      return m_table.getRowCount() == 0;
    }
    else {
      return true;
    }
  }

  @Override
  public IValidateContentDescriptor validateContent() {
    IValidateContentDescriptor desc = super.validateContent();
    // super check
    if (desc != null) {
      return desc;
    }
    ITable table = getTable();
    if (table == null) {
      return null;
    }
    // ensureCellEditComplete
    if (table instanceof AbstractTable) {
      table.completeCellEdit();
    }
    // check cells
    IValidateContentDescriptor validateContentDescriptor = getValidateContentDescriptor();
    boolean isTableFieldDescriptor = validateContentDescriptor instanceof ValidateTableFieldDescriptor;
    if (isTableFieldDescriptor) {
      // reset descriptor so that no old rows or columns are referenced
      ValidateTableFieldDescriptor tableDesc = (ValidateTableFieldDescriptor) validateContentDescriptor;
      tableDesc.setRow(null);
      tableDesc.setColumn(null);
    }
    Set<String> columnNames = new TreeSet<>();
    for (ITableRow row : table.getRows()) {
      for (IColumn col : table.getColumns()) {
        if (!col.isContentValid(row)) {
          if (columnNames.isEmpty() && isTableFieldDescriptor) {
            // set the first invalid column to the descriptor
            ValidateTableFieldDescriptor tableDesc = (ValidateTableFieldDescriptor) validateContentDescriptor;
            tableDesc.setRow(row);
            tableDesc.setColumn(col);
          }
          columnNames.add(getColumnName(col));
        }
      }
    }
    if (columnNames.isEmpty()) {
      return null; // all content valid
    }

    table.ensureInvalidColumnsVisible();
    validateContentDescriptor.setDisplayText(TEXTS.get("TableName") + " " + getLabel() + ": " + CollectionUtility.format(columnNames));
    return validateContentDescriptor;
  }

  /**
   * Column name for error message
   */
  private String getColumnName(IColumn col) {
    String columnName = col.getHeaderCell().getText();
    if (columnName == null) {
      LOG.warn("Validation Error on Column without header text, using className for error message.");
      columnName = col.getClass().getSimpleName();
    }
    return columnName;
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), Collections.singletonList(getTable()));
  }

  @Override
  public IStatus getTableStatus() {
    T table = getTable();
    if (table != null) {
      return table.getTableStatus();
    }
    return null;
  }

  @Override
  public void setTableStatus(IStatus tableStatus) {
    T table = getTable();
    if (table != null) {
      table.setTableStatus(tableStatus);
    }
  }

  @Override
  public boolean isTableStatusVisible() {
    T table = getTable();
    if (table != null) {
      return table.isTableStatusVisible();
    }
    return false;
  }

  @Override
  public void setTableStatusVisible(boolean tableStatusVisible) {
    T table = getTable();
    if (table != null) {
      table.setTableStatusVisible(tableStatusVisible);
    }
  }

  /**
   * Saves the table. The call order is as follows:
   * <ol>
   * <li>{@link #interceptSave(List, List, List)}</li>
   * <li>{@link #interceptSaveDeletedRow(ITableRow)}</li>
   * <li>{@link #interceptSaveInsertedRow(ITableRow)}</li>
   * <li>{@link #interceptSaveUpdatedRow(ITableRow)}</li>
   * </ol>
   */
  @Override
  public void doSave() {
    if (m_table != null && !m_tableExternallyManaged) {
      try {
        m_table.setTableChanging(true);
        //
        // 1. batch
        interceptSave(m_table.getInsertedRows(), m_table.getUpdatedRows(), m_table.getDeletedRows());
        // 2. per row
        // deleted rows
        for (ITableRow deletedRow : m_table.getDeletedRows()) {
          interceptSaveDeletedRow(deletedRow);
        }
        // inserted rows
        for (ITableRow insertedRow : m_table.getInsertedRows()) {
          interceptSaveInsertedRow(insertedRow);
          insertedRow.setStatusNonchanged();
          m_table.updateRow(insertedRow);
        }
        // updated rows
        for (ITableRow updatedRow : m_table.getUpdatedRows()) {
          interceptSaveUpdatedRow(updatedRow);
          updatedRow.setStatusNonchanged();
          m_table.updateRow(updatedRow);
        }
      }
      finally {
        m_table.setTableChanging(false);
      }
    }
    markSaved();
  }

  /**
   * Reloads the table data.
   * <p>
   * The default implementation calls {@link #interceptReloadTableData()}.
   */
  @Override
  public void reloadTableData() {
    interceptReloadTableData();
  }

  /**
   * @return {@code true} if all cells of given row contains value {@code null}, otherwise {@code false}.
   */
  public boolean isEmptyRow(ITableRow row) {
    return isEmptyRow(row, null);
  }

  /**
   * @return {@code true} if all cells of given row contains value {@code null}, otherwise {@code false}. The cells
   *         given by {@code columnIndexesToIgnore} are excluded in this check.
   */
  public boolean isEmptyRow(ITableRow row, Set<Integer> columnIndexesToIgnore) {
    for (int i = 0; i < row.getCellCount(); i++) {
      if (CollectionUtility.contains(columnIndexesToIgnore, i)) {
        continue;
      }
      if (row.getCellValue(i) != null) {
        return false;
      }
    }
    return true;
  }

  protected static class LocalTableFieldExtension<T extends ITable, OWNER extends AbstractTableField<T>> extends LocalFormFieldExtension<OWNER> implements ITableFieldExtension<T, OWNER> {

    public LocalTableFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execReloadTableData(TableFieldReloadTableDataChain<? extends ITable> chain) {
      getOwner().execReloadTableData();
    }

    @Override
    public void execSaveInsertedRow(TableFieldSaveInsertedRowChain<? extends ITable> chain, ITableRow row) {
      getOwner().execSaveInsertedRow(row);
    }

    @Override
    public void execSaveUpdatedRow(TableFieldSaveUpdatedRowChain<? extends ITable> chain, ITableRow row) {
      getOwner().execSaveUpdatedRow(row);
    }

    @Override
    public void execSaveDeletedRow(TableFieldSaveDeletedRowChain<? extends ITable> chain, ITableRow row) {
      getOwner().execSaveDeletedRow(row);
    }

    @Override
    public void execSave(TableFieldSaveChain<? extends ITable> chain, List<? extends ITableRow> insertedRows, List<? extends ITableRow> updatedRows, List<? extends ITableRow> deletedRows) {
      getOwner().execSave(insertedRows, updatedRows, deletedRows);
    }
  }

  @Override
  protected ITableFieldExtension<T, ? extends AbstractTableField<T>> createLocalExtension() {
    return new LocalTableFieldExtension<>(this);
  }

  protected final void interceptReloadTableData() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TableFieldReloadTableDataChain<T> chain = new TableFieldReloadTableDataChain<>(extensions);
    chain.execReloadTableData();
  }

  protected final void interceptSaveInsertedRow(ITableRow row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TableFieldSaveInsertedRowChain<T> chain = new TableFieldSaveInsertedRowChain<>(extensions);
    chain.execSaveInsertedRow(row);
  }

  protected final void interceptSaveUpdatedRow(ITableRow row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TableFieldSaveUpdatedRowChain<T> chain = new TableFieldSaveUpdatedRowChain<>(extensions);
    chain.execSaveUpdatedRow(row);
  }

  protected final void interceptSaveDeletedRow(ITableRow row) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TableFieldSaveDeletedRowChain<T> chain = new TableFieldSaveDeletedRowChain<>(extensions);
    chain.execSaveDeletedRow(row);
  }

  protected final void interceptSave(List<? extends ITableRow> insertedRows, List<? extends ITableRow> updatedRows, List<? extends ITableRow> deletedRows) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    TableFieldSaveChain<T> chain = new TableFieldSaveChain<>(extensions);
    chain.execSave(insertedRows, updatedRows, deletedRows);
  }

  @Override
  public void setValueChangeTriggerEnabled(boolean b) {
    super.setValueChangeTriggerEnabled(b);
    if (isInitConfigDone() && getTable() != null) {
      getTable().setValueChangeTriggerEnabled(b);
    }
  }
}
