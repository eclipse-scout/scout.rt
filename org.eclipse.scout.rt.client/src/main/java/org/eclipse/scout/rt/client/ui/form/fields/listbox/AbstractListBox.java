/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import static org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldUtility.connectFields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.IListBoxExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxFilterLookupResultChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxLoadTableDataChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPopulateTableChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.listbox.ListBoxChains.ListBoxPrepareLookupChain;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTableRowBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.CheckableStyle;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.CompositeFieldUtility;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeFieldGrid;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.OrderedComparator;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("3dc8747d-19eb-4c0a-b5fc-c3dc2ad0783d")
public abstract class AbstractListBox<KEY> extends AbstractValueField<Set<KEY>> implements IListBox<KEY> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractListBox.class);

  private ITable m_table;
  private ILookupCall<KEY> m_lookupCall;
  private Class<? extends ICodeType<?, KEY>> m_codeTypeClass;
  private boolean m_valueTableSyncActive;
  private ITableRowFilter m_checkedRowsFilter;
  private ITableRowFilter m_activeRowsFilter;
  // children
  private List<IFormField> m_fields;
  private Map<Class<? extends IFormField>, IFormField> m_movedFormFieldsByClass;

  public AbstractListBox() {
    this(true);
  }

  public AbstractListBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  /**
   * Configure a lookup call to fill listbox with values.
   *
   * @return Lookup call of listbox
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(240)
  protected Class<? extends ILookupCall<KEY>> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  protected Class<? extends ICodeType<?, KEY>> getConfiguredCodeType() {
    return null;
  }

  @Override
  @Order(210)
  @ConfigProperty(ConfigProperty.BOOLEAN)
  protected boolean getConfiguredAutoAddDefaultMenus() {
    return false;
  }

  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(230)
  protected String getConfiguredIconId() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(260)
  protected boolean getConfiguredAutoLoad() {
    return true;
  }

  /**
   * @return true: a filter is added to the listbox table that only accepts rows that are active or checked.<br>
   *         Default is true<br>
   *         Affects {@link ITable#getFilteredRows()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredFilterActiveRows() {
    return false;
  }

  /**
   * @return true: a filter is added to the listbox table that only accepts checked rows<br>
   *         Default is false<br>
   *         Affects {@link ITable#getFilteredRows()}<br>
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(280)
  protected boolean getConfiguredFilterCheckedRows() {
    return false;
  }

  @Override
  protected double getConfiguredGridWeightY() {
    return 1.0;
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default for a list box is 2.
   */
  @Override
  protected int getConfiguredGridH() {
    return 2;
  }

  @Override
  protected String getConfiguredClearable() {
    return CLEARABLE_NEVER;
  }

  private List<Class<? extends IFormField>> getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IFormField>> fields = ConfigurationUtility.filterClasses(dca, IFormField.class);
    return ConfigurationUtility.removeReplacedClasses(fields);
  }

  /**
   * On any value change or call to {@link #checkEmpty()} this method is called to calculate if the field represents an
   * empty state (semantics)
   * <p>
   */
  @Override
  protected boolean execIsEmpty() {
    if (!areChildrenEmpty()) {
      return false;
    }
    return getValue().isEmpty();
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(250)
  protected void execPrepareLookup(ILookupCall<KEY> call) {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace and clear of entries in this list is
   *          supported
   */
  @ConfigOperation
  @Order(260)
  protected void execFilterLookupResult(ILookupCall<KEY> call, List<ILookupRow<KEY>> result) {
  }

  @ConfigOperation
  @Order(230)
  protected List<? extends ILookupRow<KEY>> execLoadTableData() {
    List<? extends ILookupRow<KEY>> data;
    // (1) get data by service
    if (getLookupCall() != null) {
      ILookupCall<KEY> call = BEANS.get(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractListBox.this));
      prepareLookupCall(call);
      data = call.getDataByAll();
      data = filterLookupResult(call, data);
    }
    // (b) get data direct
    else {
      data = filterLookupResult(null, null);
    }
    return data;
  }

  /**
   * Interceptor is called after data was fetched from LookupCall and is adding a table row for every LookupRow using
   * IListBoxTable.createTableRow(row) and ITable.addRows()
   * <p>
   * For most cases the override of just {@link #interceptLoadTableData()} is sufficient
   *
   * <pre>
   * List<ILookupRow<T>> data = execLoadTableData();
   * List<ITableRow> rows = new ArrayList();
   * if (data != null) {
   *   for (int i = 0; i &lt; data.length; i++) {
   *     rows.add(createTableRow(data[i]));
   *   }
   * }
   * getTable().replaceRows(rows);
   * </pre>
   */
  @ConfigOperation
  @Order(240)
  protected void execPopulateTable() {
    List<? extends ILookupRow<KEY>> data = null;
    //sle Ticket 92'893: Listbox Master required. only run loadTable when master value is set
    if (!isMasterRequired() || getMasterValue() != null) {
      data = interceptLoadTableData();
    }
    List<ITableRow> rows = new ArrayList<>();
    if (data != null) {
      for (ILookupRow<KEY> lr : data) {
        rows.add(getTableRowBuilder().createTableRow(lr));
      }
    }
    getTable().replaceRows(rows);
  }

  private Class<? extends ITable> getConfiguredTable() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<ITable>> tableClasses = ConfigurationUtility.filterClasses(dca, ITable.class);
    if (tableClasses.size() == 1) {
      return CollectionUtility.firstElement(tableClasses);
    }
    else {
      for (Class<ITable> tableClazz : tableClasses) {
        if (tableClazz.getDeclaringClass() != AbstractListBox.class) {
          return tableClazz;
        }
      }
      return null;
    }
  }

  @Override
  protected void execChangedMasterValue(Object newMasterValue) {
    setValue(null);
    loadListBoxData();
  }

  @Override
  protected void initConfig() {
    m_fields = CollectionUtility.emptyArrayList();
    m_movedFormFieldsByClass = new HashMap<>();
    super.initConfig();
    setFilterActiveRows(getConfiguredFilterActiveRows());
    setFilterActiveRowsValue(TriState.TRUE);
    setFilterCheckedRows(getConfiguredFilterCheckedRows());
    setFilterCheckedRowsValue(getConfiguredFilterCheckedRows());
    List<ITable> contributedTables = m_contributionHolder.getContributionsByClass(ITable.class);
    m_table = CollectionUtility.firstElement(contributedTables);
    if (m_table == null) {
      Class<? extends ITable> configuredTable = getConfiguredTable();
      if (configuredTable != null) {
        m_table = ConfigurationUtility.newInnerInstance(this, configuredTable);
      }
    }

    if (m_table != null) {
      if (m_table instanceof AbstractTable) {
        m_table.setParentInternal(this);
      }
      updateActiveRowsFilter();
      updateCheckedRowsFilter();
      m_table.addTableListener(
          e -> {
            switch (e.getType()) {
              case TableEvent.TYPE_ROWS_SELECTED: {
                if (!getTable().isCheckable()) {
                  syncTableToValue();
                }
                break;
              }
              case TableEvent.TYPE_ROWS_CHECKED: {
                if (getTable().isCheckable()) {
                  syncTableToValue();
                }
                break;
              }
            }
          },
          TableEvent.TYPE_ROWS_SELECTED,
          TableEvent.TYPE_ROWS_CHECKED);
      // default icon
      if (m_table.getDefaultIconId() == null && this.getConfiguredIconId() != null) {
        m_table.setDefaultIconId(this.getConfiguredIconId());
      }
    }
    else {
      LOG.warn("there is no inner class of type ITable in {}", getClass().getName());
    }

    // lookup call
    Class<? extends ILookupCall<KEY>> lookupCallClass = getConfiguredLookupCall();
    if (lookupCallClass != null) {
      ILookupCall<KEY> call = BEANS.get(lookupCallClass);
      setLookupCall(call);
    }
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // local property listener
    addPropertyChangeListener(e -> {
      if (m_table == null) {
        return;
      }

      String name = e.getPropertyName();
      if (PROP_FILTER_CHECKED_ROWS_VALUE.equals(name)) {
        updateCheckedRowsFilter();
      }
      else if (PROP_FILTER_ACTIVE_ROWS_VALUE.equals(name)) {
        updateActiveRowsFilter();
      }
    });
    // add fields
    List<Class<? extends IFormField>> fieldClasses = getConfiguredFields();
    List<IFormField> contributedFields = m_contributionHolder.getContributionsByClass(IFormField.class);
    List<IFormField> fieldList = new ArrayList<>(fieldClasses.size() + contributedFields.size());
    for (Class<? extends IFormField> fieldClazz : fieldClasses) {
      IFormField f = ConfigurationUtility.newInnerInstance(this, fieldClazz);
      fieldList.add(f);
    }
    fieldList.addAll(contributedFields);
    fieldList.sort(new OrderedComparator());

    for (IFormField f : fieldList) {
      connectFields(f, this);
    }
    m_fields = fieldList;
  }

  @Override
  public void addField(IFormField f) {
    CompositeFieldUtility.addField(f, this, m_fields);
  }

  @Override
  public void removeField(IFormField f) {
    CompositeFieldUtility.removeField(f, this, m_fields);
  }

  @Override
  public void moveFieldTo(IFormField f, ICompositeField newContainer) {
    CompositeFieldUtility.moveFieldTo(f, this, newContainer);
    m_movedFormFieldsByClass.put(f.getClass(), f);
  }

  @Override
  public Map<Class<? extends IFormField>, IFormField> getMovedFields() {
    return Collections.unmodifiableMap(m_movedFormFieldsByClass);
  }

  @SuppressWarnings("unchecked")
  public ListBoxFilterBox getListBoxFilterBox() {
    return getFieldByClass(ListBoxFilterBox.class);
  }

  @Override
  protected void initFieldInternal() {
    if (getConfiguredAutoLoad()) {
      try {
        setValueChangeTriggerEnabled(false);
        loadListBoxData();
      }
      finally {
        setValueChangeTriggerEnabled(true);
      }
    }
    super.initFieldInternal();
  }

  public AbstractTableRowBuilder<KEY> getTableRowBuilder() {
    return new P_TableRowBuilder();
  }

  @Override
  public final ITable getTable() {
    return m_table;
  }

  @Override
  public void setMandatory(boolean b, boolean recursive) {
    // do not propagate to children
    // for backwards-compatibility because list-box does not extend AbstractCompositeField which contained the auto-propagation in older releases
    super.setMandatory(b, false);
  }

  @Override
  public boolean isFilterCheckedRows() {
    return propertySupport.getPropertyBool(PROP_FILTER_CHECKED_ROWS);
  }

  @Override
  public void setFilterCheckedRows(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_CHECKED_ROWS, b);
  }

  @Override
  public boolean getFilterCheckedRowsValue() {
    return propertySupport.getPropertyBool(PROP_FILTER_CHECKED_ROWS_VALUE);
  }

  @Override
  public void setFilterCheckedRowsValue(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_CHECKED_ROWS_VALUE, b);
  }

  @Override
  public boolean isFilterActiveRows() {
    return propertySupport.getPropertyBool(PROP_FILTER_ACTIVE_ROWS);
  }

  @Override
  public void setFilterActiveRows(boolean b) {
    propertySupport.setPropertyBool(PROP_FILTER_ACTIVE_ROWS, b);
  }

  @Override
  public TriState getFilterActiveRowsValue() {
    return (TriState) propertySupport.getProperty(PROP_FILTER_ACTIVE_ROWS_VALUE);
  }

  @Override
  public void setFilterActiveRowsValue(TriState t) {
    if (t == null) {
      t = TriState.TRUE;
    }
    propertySupport.setProperty(PROP_FILTER_ACTIVE_ROWS_VALUE, t);
  }

  private void updateActiveRowsFilter() {
    try {
      getTable().setTableChanging(true);
      //
      if (m_activeRowsFilter != null) {
        getTable().removeRowFilter(m_activeRowsFilter);
        m_activeRowsFilter = null;
      }
      m_activeRowsFilter = new ActiveOrCheckedRowsFilter(getActiveColumnInternal(), getFilterActiveRowsValue());
      getTable().addRowFilter(m_activeRowsFilter);
    }
    finally {
      getTable().setTableChanging(false);
    }
  }

  private void updateCheckedRowsFilter() {
    try {
      getTable().setTableChanging(true);
      //
      if (m_checkedRowsFilter != null) {
        getTable().removeRowFilter(m_checkedRowsFilter);
        m_checkedRowsFilter = null;
      }
      if (getFilterCheckedRowsValue()) {
        m_checkedRowsFilter = new CheckedRowsFilter();
        getTable().addRowFilter(m_checkedRowsFilter);
      }
    }
    finally {
      getTable().setTableChanging(false);
    }
  }

  @Override
  public void loadListBoxData() {
    if (getTable() != null) {
      try {
        m_valueTableSyncActive = true;
        getTable().setTableChanging(true);
        //
        interceptPopulateTable();
      }
      finally {
        getTable().setTableChanging(false);
        m_valueTableSyncActive = false;
      }
      syncValueToTable();
    }
  }

  /**
   * do not use this internal method directly
   */
  @Override
  public final void prepareLookupCall(ILookupCall<KEY> call) {
    prepareLookupCallInternal(call);
    interceptPrepareLookup(call);
  }

  private List<ILookupRow<KEY>> filterLookupResult(ILookupCall<KEY> call, List<? extends ILookupRow<KEY>> data) {
    // create a copy for the custom filter method
    List<ILookupRow<KEY>> result = CollectionUtility.arrayList(data);
    interceptFilterLookupResult(call, result);
    Iterator<ILookupRow<KEY>> resultIt = result.iterator();
    while (resultIt.hasNext()) {
      ILookupRow<KEY> row = resultIt.next();
      if (row == null) {
        resultIt.remove();
      }
      else if (row.getKey() == null) {
        LOG.warn("The key of a lookup row may not be null. Row has been removed for list box '{}'.", getClass().getName());
        resultIt.remove();
      }
    }
    return result;
  }

  /**
   * do not use this internal method directly
   */
  private void prepareLookupCallInternal(ILookupCall<KEY> call) {
    call.setActive(TriState.UNDEFINED);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
  }

  @Override
  public final ILookupCall<KEY> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<KEY> call) {
    m_lookupCall = call;
  }

  @Override
  public Class<? extends ICodeType<?, KEY>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, KEY>> codeTypeClass) {
    m_codeTypeClass = codeTypeClass;
    // create lookup service call
    m_lookupCall = null;
    if (m_codeTypeClass != null) {
      m_lookupCall = CodeLookupCall.newInstanceByService(m_codeTypeClass);
    }
  }

  @Override
  protected void valueChangedInternal() {
    super.valueChangedInternal();
    syncValueToTable();
  }

  @Override
  protected String formatValueInternal(Set<KEY> validValue) {
    if (!CollectionUtility.hasElements(validValue)) {
      return "";
    }
    StringBuilder b = new StringBuilder();
    List<ITableRow> rows = getKeyColumnInternal().findRows(validValue);
    if (CollectionUtility.hasElements(rows)) {
      Iterator<ITableRow> rowIt = rows.iterator();
      b.append(getTextColumnInternal().getValue(rowIt.next()));
      while (rowIt.hasNext()) {
        b.append(", ");
        b.append(getTextColumnInternal().getValue(rowIt.next()));
      }
    }
    return b.toString();
  }

  @Override
  protected final Set<KEY> validateValueInternal(Set<KEY> rawValue0) {
    Set<KEY> rawValue = CollectionUtility.hashSet(rawValue0);
    return doValidateValueInternal(rawValue);
  }

  /**
   * override this method to perform detailed validation in subclasses
   */
  protected Set<KEY> doValidateValueInternal(Set<KEY> rawValue) {
    if (CollectionUtility.isEmpty(rawValue)) {
      // fast return empty set
      return rawValue;
    }
    ITable table = getTable();
    if (table != null
        && ((table.isCheckable() && !table.isMultiCheck()) || (!table.isCheckable() && !table.isMultiSelect()))
        && rawValue.size() > 1) {
      LOG.warn("{} only accepts a single value. Got {}. Using only first value.", getClass().getName(), rawValue);
      return CollectionUtility.hashSet(CollectionUtility.firstElement(rawValue));
    }
    return rawValue;
  }

  @Override
  public boolean isContentValid() {
    boolean valid = super.isContentValid();
    if (valid && isMandatory() && getValue().isEmpty()) {
      return false;
    }
    return valid;
  }

  /**
   * Value, empty {@link Set} in case of an empty value, never <code>null</code>.
   */
  @Override
  public Set<KEY> getValue() {
    return CollectionUtility.hashSet(super.getValue());
  }

  /**
   * Initial value, empty {@link Set} in case of an empty value, never <code>null</code>.
   */
  @Override
  public Set<KEY> getInitValue() {
    return CollectionUtility.hashSet(super.getInitValue());
  }

  @Override
  public KEY getSingleValue() {
    return CollectionUtility.firstElement(super.getValue());
  }

  @Override
  public void setSingleValue(KEY value) {
    Set<KEY> valueSet = new HashSet<>();
    if (value != null) {
      valueSet.add(value);
    }
    setValue(valueSet);
  }

  @Override
  public int getCheckedKeyCount() {
    return getValue().size();
  }

  @Override
  public KEY getCheckedKey() {
    return CollectionUtility.firstElement(getCheckedKeys());
  }

  @Override
  public Set<KEY> getCheckedKeys() {
    return getValue();
  }

  @Override
  public ILookupRow<KEY> getCheckedLookupRow() {
    return CollectionUtility.firstElement(getCheckedLookupRows());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ILookupRow<KEY>> getCheckedLookupRows() {
    Collection<ITableRow> checkedRows = getTable().getCheckedRows();
    Set<ILookupRow<KEY>> result = new HashSet<>(checkedRows.size());
    for (ITableRow row : checkedRows) {
      ICell cell = row.getCell(1);
      result.add(new LookupRow<>((KEY) row.getCellValue(0), cell.getText())
          .withIconId(cell.getIconId())
          .withTooltipText(cell.getTooltipText())
          .withBackgroundColor(cell.getBackgroundColor())
          .withForegroundColor(cell.getForegroundColor())
          .withFont(cell.getFont())
          .withEnabled(row.isEnabled()));
    }
    return result;
  }

  @Override
  public void checkKey(KEY key) {
    if (key == null) {
      checkKeys(null);
    }
    else {
      checkKeys(CollectionUtility.hashSet(key));
    }
  }

  @Override
  public void checkKeys(Collection<? extends KEY> keys) {
    setValue(CollectionUtility.hashSetWithoutNullElements(keys));
  }

  @Override
  public void uncheckAllKeys() {
    checkKeys(null);
  }

  @Override
  public Set<KEY> getUncheckedKeys() {
    Set<KEY> result = new HashSet<>();
    Set<KEY> initValue = getInitValue();
    if (initValue != null) {
      result.addAll(initValue);
    }
    Set<KEY> checkedKeys = getCheckedKeys();
    if (checkedKeys != null) {
      result.removeAll(checkedKeys);
    }
    return result;
  }

  @Override
  public void checkAllKeys() {
    checkKeys(getKeyColumnInternal().getValues());
  }

  @Override
  public void checkAllActiveKeys() {
    checkKeys(getKeyColumnInternal().getValues(getActiveColumnInternal().findRows(true)));
  }

  @Override
  public void uncheckAllInactiveKeys() {
    checkKeys(getKeyColumnInternal().getValues(getActiveColumnInternal().findRows(false)));
  }

  @Override
  public void exportFormFieldData(AbstractFormFieldData target) {
    @SuppressWarnings("unchecked")
    AbstractValueFieldData<Set<KEY>> v = (AbstractValueFieldData<Set<KEY>>) target;
    Set<KEY> value = getValue();
    if (CollectionUtility.isEmpty(value)) {
      v.setValue(null);
    }
    else {
      v.setValue(CollectionUtility.hashSet(this.getValue()));
    }
  }

  @SuppressWarnings("unchecked")
  private IColumn<KEY> getKeyColumnInternal() {
    return getTable().getColumnSet().getColumn(0);
  }

  @SuppressWarnings("unchecked")
  private IColumn<String> getTextColumnInternal() {
    return getTable().getColumnSet().getColumn(1);
  }

  @SuppressWarnings("unchecked")
  private IColumn<Boolean> getActiveColumnInternal() {
    return getTable().getColumnSet().getColumn(2);
  }

  private void syncValueToTable() {
    if (m_valueTableSyncActive) {
      return;
    }
    try {
      m_valueTableSyncActive = true;
      getTable().setTableChanging(true);
      //
      Set<KEY> checkedKeys = getCheckedKeys();
      List<ITableRow> checkedRows = getKeyColumnInternal().findRows(checkedKeys);
      for (ITableRow row : getTable().getRows()) {
        row.setChecked(false);
      }
      for (ITableRow row : checkedRows) {
        row.setChecked(true);
      }
      if (!getTable().isCheckable()) {
        getTable().selectRows(checkedRows, false);
      }
      getTable().applyRowFilters();
    }
    finally {
      getTable().setTableChanging(false);
      m_valueTableSyncActive = false;
    }
  }

  private void syncTableToValue() {
    if (m_valueTableSyncActive) {
      return;
    }
    boolean resync = false;
    try {
      m_valueTableSyncActive = true;
      m_table.setTableChanging(true);
      //
      Collection<ITableRow> checkedRows;
      if (getTable().isCheckable()) {
        checkedRows = getTable().getCheckedRows();
      }
      else {
        checkedRows = getTable().getSelectedRows();
      }
      List<KEY> checkedKeys = getKeyColumnInternal().getValues(checkedRows);
      checkKeys(checkedKeys);
      // Due to validate logic, the actual value
      // may differ now, making a resync of the value is necessary
      Set<KEY> validatedCheckedKeys = getCheckedKeys();
      if (!CollectionUtility.equalsCollection(checkedKeys, validatedCheckedKeys)) {
        resync = true;
      }
      if (!getTable().isCheckable()) {
        //checks follow selection
        for (ITableRow row : m_table.getRows()) {
          row.setChecked(row.isSelected());
        }
      }
      getTable().applyRowFilters();
    }
    finally {
      getTable().setTableChanging(false);
      m_valueTableSyncActive = false;
    }
    if (resync) {
      // The value of the treeBox is different
      // from the one represented in the tree.
      // Need to sync.
      syncValueToTable();
    }
  }

/*
 * Implementation of ICompositeField
 */

  @Override
  public <F extends IFormField> F getFieldByClass(Class<F> widgetClassToFind) {
    return getWidgetByClass(widgetClassToFind);
  }

  @Override
  public IFormField getFieldById(String id) {
    return CompositeFieldUtility.getFieldById(this, id);
  }

  @Override
  public <X extends IFormField> X getFieldById(String id, Class<X> type) {
    return CompositeFieldUtility.getFieldById(this, id, type);
  }

  @Override
  public int getFieldCount() {
    return m_fields.size();
  }

  @Override
  public int getFieldIndex(IFormField f) {
    return m_fields.indexOf(f);
  }

  @Override
  public void setFields(List<IFormField> fields) {
    m_fields = CollectionUtility.arrayList(fields);
  }

  @Override
  public List<IFormField> getFields() {
    return CollectionUtility.arrayList(m_fields);
  }

  @Override
  public List<? extends IWidget> getChildren() {
    return CollectionUtility.flatten(super.getChildren(), m_fields, Collections.singletonList(getTable()));
  }

  @Override
  public void rebuildFieldGrid() {
    GridData gd = getListBoxFilterBox().getGridDataHints();
    gd.x = 0;
    gd.y = 0;
    getListBoxFilterBox().setGridDataInternal(gd);
  }

  @Override
  public ICompositeFieldGrid<? extends ICompositeField> getFieldGrid() {
    return null;
  }

  @Order(1)
  @ClassId("a2e982d1-ea01-4d11-8655-d10c9935d8b9")
  public class ListBoxFilterBox extends AbstractListBoxFilterBox {
    @Override
    protected IListBox getListBox() {
      return AbstractListBox.this;
    }
  }

  @ClassId("44c5f5e6-0f81-40cb-9842-6b61bee8e8e8")
  public class DefaultListBoxTable extends AbstractTable {
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
    protected boolean getConfiguredCheckable() {
      return true;
    }

    @Override
    protected CheckableStyle getConfiguredCheckableStyle() {
      return CheckableStyle.CHECKBOX_TABLE_ROW;
    }

    @SuppressWarnings("unchecked")
    public KeyColumn getKeyColumn() {
      return getColumnSet().getColumnByClass(KeyColumn.class);
    }

    @SuppressWarnings("unchecked")
    public TextColumn getTextColumn() {
      return getColumnSet().getColumnByClass(TextColumn.class);
    }

    @SuppressWarnings("unchecked")
    public ActiveColumn getActiveColumn() {
      return getColumnSet().getColumnByClass(ActiveColumn.class);
    }

    @Order(1)
    @ClassId("7199a815-0d92-4c39-b28f-262b0295a814")
    public class KeyColumn extends AbstractColumn<KEY> {
      @Override
      protected boolean getConfiguredPrimaryKey() {
        return true;
      }

      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }

      @Override
      @SuppressWarnings("unchecked")
      public Class<KEY> getDataType() {
        return TypeCastUtility.getGenericsParameterClass(AbstractListBox.this.getClass(), IListBox.class);
      }
    }

    @Order(2)
    @ClassId("5cc5bf09-c711-4557-9b11-a26b45723743")
    public class TextColumn extends AbstractStringColumn {

    }

    @Order(3)
    @ClassId("1ee3754a-32a1-4e6b-bfcb-2cae34505ee6")
    public class ActiveColumn extends AbstractBooleanColumn {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }
  }

  protected class P_TableRowBuilder extends AbstractTableRowBuilder<KEY> {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getTable().getColumnSet());
    }

    @Override
    public ITableRow createTableRow(ILookupRow<KEY> dataRow) {
      TableRow tableRow = (TableRow) super.createTableRow(dataRow);
      // fill values to tableRow
      getKeyColumnInternal().setValue(tableRow, dataRow.getKey());
      getTextColumnInternal().setValue(tableRow, dataRow.getText());
      getActiveColumnInternal().setValue(tableRow, dataRow.isActive());

      // enable/disabled row
      tableRow.setEnabled(dataRow.isEnabled());

      Cell cell = tableRow.getCellForUpdate(1);
      // hint for inactive codes
      if (!dataRow.isActive()) {
        if (cell.getFont() == null) {
          cell.setFont(FontSpec.parse("italic"));
        }
        getTextColumnInternal().setValue(tableRow, dataRow.getText() + " (" + TEXTS.get("InactiveState") + ")");
      }
      return tableRow;
    }
  }

  protected final void interceptPopulateTable() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ListBoxPopulateTableChain<KEY> chain = new ListBoxPopulateTableChain<>(extensions);
    chain.execPopulateTable();
  }

  protected final List<? extends ILookupRow<KEY>> interceptLoadTableData() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ListBoxLoadTableDataChain<KEY> chain = new ListBoxLoadTableDataChain<>(extensions);
    return chain.execLoadTableData();
  }

  protected final void interceptFilterLookupResult(ILookupCall<KEY> call, List<ILookupRow<KEY>> result) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ListBoxFilterLookupResultChain<KEY> chain = new ListBoxFilterLookupResultChain<>(extensions);
    chain.execFilterLookupResult(call, result);
  }

  protected final void interceptPrepareLookup(ILookupCall<KEY> call) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ListBoxPrepareLookupChain<KEY> chain = new ListBoxPrepareLookupChain<>(extensions);
    chain.execPrepareLookup(call);
  }

  protected static class LocalListBoxExtension<KEY, OWNER extends AbstractListBox<KEY>> extends LocalValueFieldExtension<Set<KEY>, OWNER> implements IListBoxExtension<KEY, OWNER> {

    public LocalListBoxExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execPopulateTable(ListBoxPopulateTableChain<KEY> chain) {
      getOwner().execPopulateTable();
    }

    @Override
    public List<? extends ILookupRow<KEY>> execLoadTableData(ListBoxLoadTableDataChain<KEY> chain) {
      return getOwner().execLoadTableData();
    }

    @Override
    public void execFilterLookupResult(ListBoxFilterLookupResultChain<KEY> chain, ILookupCall<KEY> call, List<ILookupRow<KEY>> result) {
      getOwner().execFilterLookupResult(call, result);
    }

    @Override
    public void execPrepareLookup(ListBoxPrepareLookupChain<KEY> chain, ILookupCall<KEY> call) {
      getOwner().execPrepareLookup(call);
    }
  }

  @Override
  protected IListBoxExtension<KEY, ? extends AbstractListBox<KEY>> createLocalExtension() {
    return new LocalListBoxExtension<>(this);
  }
}
