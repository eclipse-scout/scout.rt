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
package org.eclipse.scout.rt.client.ui.form.fields.listbox;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ConfigurationUtility;
import org.eclipse.scout.commons.TriState;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.services.lookup.FormFieldProvisioningContext;
import org.eclipse.scout.rt.client.services.lookup.ILookupCallProvisioningService;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTableRowBuilder;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowFilter;
import org.eclipse.scout.rt.client.ui.basic.table.TableAdapter;
import org.eclipse.scout.rt.client.ui.basic.table.TableEvent;
import org.eclipse.scout.rt.client.ui.basic.table.TableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.form.IFormFieldVisitor;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.form.ValidationRule;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractFormFieldData;
import org.eclipse.scout.rt.shared.data.form.fields.AbstractValueFieldData;
import org.eclipse.scout.rt.shared.services.common.code.ICodeType;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.lookup.CodeLookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupCall;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.service.SERVICES;

@ClassId("3dc8747d-19eb-4c0a-b5fc-c3dc2ad0783d")
public abstract class AbstractListBox<T> extends AbstractValueField<Set<T>> implements IListBox<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractListBox.class);

  private ITable m_table;
  private IListBoxUIFacade m_uiFacade;
  private ILookupCall<T> m_lookupCall;
  private Class<? extends ICodeType<?, T>> m_codeTypeClass;
  private boolean m_valueTableSyncActive;
  private ITableRowFilter m_checkedRowsFilter;
  private ITableRowFilter m_activeRowsFilter;
  // children
  private List<IFormField> m_fields;

  public AbstractListBox() {
    this(true);
  }

  public AbstractListBox(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.LOOKUP_CALL)
  @Order(240)
  @ValidationRule(ValidationRule.LOOKUP_CALL)
  protected Class<? extends ILookupCall<T>> getConfiguredLookupCall() {
    return null;
  }

  @ConfigProperty(ConfigProperty.CODE_TYPE)
  @Order(250)
  @ValidationRule(ValidationRule.CODE_TYPE)
  protected Class<? extends ICodeType<?, T>> getConfiguredCodeType() {
    return null;
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
   * @return true: a filter is added to the listbox table that only accepts rows
   *         that are active or checked.<br>
   *         Default is true<br>
   *         Affects {@link ITable#getFilteredRows()}
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(270)
  protected boolean getConfiguredFilterActiveRows() {
    return false;
  }

  /**
   * @return true: a filter is added to the listbox table that only accepts
   *         checked rows<br>
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

  private List<Class<? extends IFormField>> getConfiguredFields() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    return ConfigurationUtility.sortFilteredClassesByOrderAnnotation(Arrays.asList(dca), IFormField.class);
  }

  /**
   * called before any lookup is performed
   */
  @ConfigOperation
  @Order(250)
  protected void execPrepareLookup(ILookupCall<T> call) throws ProcessingException {
  }

  /**
   * @param call
   *          that produced this result
   * @param result
   *          live list containing the result rows. Add, remove, set, replace
   *          and clear of entries in this list is supported
   */
  @ConfigOperation
  @Order(260)
  protected void execFilterLookupResult(ILookupCall<T> call, List<ILookupRow<T>> result) throws ProcessingException {
  }

  @ConfigOperation
  @Order(230)
  protected List<? extends ILookupRow<T>> execLoadTableData() throws ProcessingException {
    List<? extends ILookupRow<T>> data;
    // (1) get data by service
    if (getLookupCall() != null) {
      ILookupCall<T> call = SERVICES.getService(ILookupCallProvisioningService.class).newClonedInstance(getLookupCall(), new FormFieldProvisioningContext(AbstractListBox.this));
      prepareLookupCall(call);
      data = call.getDataByAll();
      data = filterLookupResult(call, data);
    }
    // (b) get data direct
    else {
      data = Collections.emptyList();
      data = filterLookupResult(null, data);
    }
    return data;
  }

  /**
   * Intercepter is called after data was fetched from LookupCall and is adding
   * a table row for every LookupRow using IListBoxTable.createTableRow(row) and
   * ITable.addRows()
   * <p>
   * For most cases the override of just {@link #execLoadTableData()} is sufficient
   * 
   * <pre>
   * List<ILookupRow<T>> data=execLoadTableData();
   * List<ITableRow> rows=new ArrayList();
   * if(data!=null){
   *   for(int i=0; i{@code<}data.length; i++){
   *     rows.add(createTableRow(data[i]));
   *   }
   * }
   * getTable().replaceRows(rows);
   * </pre>
   */
  @ConfigOperation
  @Order(240)
  protected void execPopulateTable() throws ProcessingException {
    List<? extends ILookupRow<T>> data = null;
    //sle Ticket 92'893: Listbox Master required. only run loadTable when master value is set
    if (!isMasterRequired() || getMasterValue() != null) {
      data = execLoadTableData();
    }
    List<ITableRow> rows = new ArrayList<ITableRow>();
    if (data != null) {
      for (ILookupRow<T> lr : data) {
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
  protected void execChangedMasterValue(Object newMasterValue) throws ProcessingException {
    setValue(null);
    loadListBoxData();
  }

  @Override
  protected void initConfig() {
    m_uiFacade = createUIFacade();
    m_fields = Collections.emptyList();
    super.initConfig();
    setFilterActiveRows(getConfiguredFilterActiveRows());
    setFilterActiveRowsValue(TriState.TRUE);
    setFilterCheckedRows(getConfiguredFilterCheckedRows());
    setFilterCheckedRowsValue(getConfiguredFilterCheckedRows());
    try {
      m_table = ConfigurationUtility.newInnerInstance(this, getConfiguredTable());
      if (m_table instanceof AbstractTable) {
        ((AbstractTable) m_table).setContainerInternal(this);
      }
      updateActiveRowsFilter();
      updateCheckedRowsFilter();
      m_table.addTableListener(new TableAdapter() {
        @Override
        public void tableChanged(TableEvent e) {
          switch (e.getType()) {
            case TableEvent.TYPE_ROWS_SELECTED: {
              if (!getTable().isCheckable()) {
                syncTableToValue();
              }
              break;
            }
            case TableEvent.TYPE_ROWS_UPDATED: {
              if (getTable().isCheckable()) {
                syncTableToValue();
              }
              break;
            }
          }
        }
      });
      // default icon
      if (m_table.getDefaultIconId() == null && this.getConfiguredIconId() != null) {
        m_table.setDefaultIconId(this.getConfiguredIconId());
      }
      m_table.setEnabled(isEnabled());
    }
    catch (Exception e) {
      LOG.warn(null, e);
    }
    // lookup call
    if (getConfiguredLookupCall() != null) {
      try {
        ILookupCall<T> call = getConfiguredLookupCall().newInstance();
        setLookupCall(call);
      }
      catch (Exception e) {
        LOG.warn(null, e);
      }
    }
    // code type
    if (getConfiguredCodeType() != null) {
      setCodeTypeClass(getConfiguredCodeType());
    }
    // local property listener
    addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (m_table != null) {
          String name = e.getPropertyName();
          if (PROP_ENABLED.equals(name)) {
            m_table.setEnabled(isEnabled());
          }
          else if (PROP_FILTER_CHECKED_ROWS_VALUE.equals(name)) {
            updateCheckedRowsFilter();
          }
          else if (PROP_FILTER_ACTIVE_ROWS_VALUE.equals(name)) {
            updateActiveRowsFilter();
          }
        }
      }
    });
    // add fields
    List<Class<? extends IFormField>> fieldClasses = getConfiguredFields();
    List<IFormField> fieldList = new ArrayList<IFormField>();
    for (Class<? extends IFormField> fieldClazz : fieldClasses) {
      IFormField f;
      try {
        f = ConfigurationUtility.newInnerInstance(this, fieldClazz);
        fieldList.add(f);
      }// end try
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("field: " + fieldClazz.getName(), t));
      }
    }
    for (IFormField f : fieldList) {
      f.setParentFieldInternal(this);
    }
    m_fields = Collections.unmodifiableList(fieldList);
  }

  @SuppressWarnings("unchecked")
  public ListBoxFilterBox getListBoxFilterBox() {
    return getFieldByClass(ListBoxFilterBox.class);
  }

  @Override
  protected void initFieldInternal() throws ProcessingException {
    getTable().initTable();
    if (getConfiguredAutoLoad()) {
      try {
        setValueChangeTriggerEnabled(false);
        //
        loadListBoxData();
      }
      finally {
        setValueChangeTriggerEnabled(true);
      }
    }
    super.initFieldInternal();
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    getTable().disposeTable();
  }

  public AbstractTableRowBuilder<T> getTableRowBuilder() {
    return new P_TableRowBuilder();
  }

  protected IListBoxUIFacade createUIFacade() {
    return new P_ListBoxUIFacade();
  }

  @Override
  public IListBoxUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  public final ITable getTable() {
    return m_table;
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
  public void loadListBoxData() throws ProcessingException {
    if (getTable() != null) {
      try {
        m_valueTableSyncActive = true;
        getTable().setTableChanging(true);
        //
        execPopulateTable();
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
  public final void prepareLookupCall(ILookupCall<T> call) throws ProcessingException {
    prepareLookupCallInternal(call);
    execPrepareLookup(call);
  }

  private List<ILookupRow<T>> filterLookupResult(ILookupCall<T> call, List<? extends ILookupRow<T>> data) throws ProcessingException {
    // create a copy for the custom filter method
    List<ILookupRow<T>> result = new ArrayList<ILookupRow<T>>(data);
    execFilterLookupResult(call, result);
    Iterator<ILookupRow<T>> resultIt = result.iterator();
    while (resultIt.hasNext()) {
      if (resultIt.next() == null) {
        resultIt.remove();
      }
    }
    return result;
  }

  /**
   * do not use this internal method directly
   */
  private void prepareLookupCallInternal(ILookupCall<T> call) {
    call.setActive(TriState.UNDEFINED);
    //when there is a master value defined in the original call, don't set it to null when no master value is available
    if (getMasterValue() != null || getLookupCall() == null || getLookupCall().getMaster() == null) {
      call.setMaster(getMasterValue());
    }
  }

  @Override
  public final ILookupCall<T> getLookupCall() {
    return m_lookupCall;
  }

  @Override
  public void setLookupCall(ILookupCall<T> call) {
    m_lookupCall = call;
  }

  @Override
  public Class<? extends ICodeType<?, T>> getCodeTypeClass() {
    return m_codeTypeClass;
  }

  @Override
  public void setCodeTypeClass(Class<? extends ICodeType<?, T>> codeTypeClass) {
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
  protected String formatValueInternal(Set<T> validValue) {
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
  protected final Set<T> validateValueInternal(Set<T> rawValue0) throws ProcessingException {
    Set<T> rawValue = CollectionUtility.hashSetWithoutNullElements(rawValue0);
    return doValidateValueInternal(rawValue);
  }

  /**
   * override this method to perform detailed validation in subclasses
   */
  protected Set<T> doValidateValueInternal(Set<T> rawValue) throws ProcessingException {
    if (CollectionUtility.isEmpty(rawValue)) {
      // fast return empty set
      return rawValue;
    }
    ITable table = getTable();
    if (table != null) {
      if ((table.isCheckable() && !table.isMultiCheck()) || (!table.isCheckable() && !table.isMultiSelect())) {
        //only single value
        if (rawValue.size() > 1) {
          LOG.warn(getClass().getName() + " only accepts a single value. Got " + CollectionUtility.format(rawValue) + ". Using only first value.");
          return CollectionUtility.hashSet(CollectionUtility.firstElement(rawValue));
        }
      }
    }
    return rawValue;
  }

  @Override
  public Set<T> getValue() {
    return CollectionUtility.unmodifiableSetCopy(super.getValue());
  }

  @Override
  public Set<T> getInitValue() {
    return CollectionUtility.unmodifiableSetCopy(super.getInitValue());
  }

  @Override
  public T getSingleValue() {
    return CollectionUtility.firstElement(super.getValue());
  }

  @Override
  public void setSingleValue(T value) {
    Set<T> valueSet = new HashSet<T>();
    if (value != null) {
      valueSet.add(value);
    }
    setValue(valueSet);
  }

  @Override
  public int getCheckedKeyCount() {
    Set<T> value = super.getValue();
    if (value != null) {
      return value.size();
    }
    else {
      return 0;
    }
  }

  @Override
  public T getCheckedKey() {
    return CollectionUtility.firstElement(getCheckedKeys());
  }

  @Override
  public Set<T> getCheckedKeys() {
    return getValue();
  }

  @Override
  public ILookupRow<T> getCheckedLookupRow() {
    return CollectionUtility.firstElement(getCheckedLookupRows());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ILookupRow<T>> getCheckedLookupRows() {
    Set<ILookupRow<T>> result = new HashSet<ILookupRow<T>>();
    for (ITableRow row : getTable().getCheckedRows()) {
      ICell cell = row.getCell(1);
      result.add(new LookupRow<T>((T) row.getCellValue(0), cell.getText(), cell.getIconId(), cell.getTooltipText(), cell.getBackgroundColor(), cell.getForegroundColor(), cell.getFont(), cell.isEnabled()));
    }
    return Collections.unmodifiableSet(result);
  }

  @Override
  public void checkKey(T key) {
    if (key == null) {
      checkKeys(null);
    }
    else {
      checkKeys(CollectionUtility.hashSet(key));
    }
  }

  @Override
  public void checkKeys(Collection<? extends T> keys) {
    setValue(CollectionUtility.<T> hashSetWithoutNullElements(keys));
  }

  @Override
  public void uncheckAllKeys() {
    checkKeys(null);
  }

  @Override
  public Set<T> getUncheckedKeys() {
    Set<T> result = new HashSet<T>();
    Set<T> initValue = getInitValue();
    if (initValue != null) {
      result.addAll(initValue);
    }
    Set<T> checkedKeys = getCheckedKeys();
    if (checkedKeys != null) {
      result.removeAll(checkedKeys);
    }
    return CollectionUtility.unmodifiableSet(result);
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
  public void exportFormFieldData(AbstractFormFieldData target) throws ProcessingException {
    @SuppressWarnings("unchecked")
    AbstractValueFieldData<Set<T>> v = (AbstractValueFieldData<Set<T>>) target;
    Set<T> value = getValue();
    if (CollectionUtility.isEmpty(value)) {
      v.setValue(null);
    }
    else {
      v.setValue(CollectionUtility.hashSet(this.getValue()));
    }
  }

  @SuppressWarnings("unchecked")
  private IColumn<T> getKeyColumnInternal() {
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
      Set<T> checkedKeys = getCheckedKeys();
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
      checkKeys(getKeyColumnInternal().getValues(checkedRows));
      if (!getTable().isCheckable()) {
        //checks follow selection
        for (ITableRow row : m_table.getRows()) {
          row.setChecked(row.isSelected());
        }
      }
    }
    finally {
      getTable().setTableChanging(false);
      m_valueTableSyncActive = false;
    }
    // check if row filter needs to change
    if (!m_table.getUIFacade().isUIProcessing()) {
      updateActiveRowsFilter();
    }
    updateCheckedRowsFilter();
  }

  /*
   * Implementation of ICompositeField
   */

  @Override
  @SuppressWarnings("unchecked")
  public <F extends IFormField> F getFieldByClass(final Class<F> c) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getClass() == c) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return (F) found.getValue();
  }

  @Override
  public IFormField getFieldById(final String id) {
    final Holder<IFormField> found = new Holder<IFormField>(IFormField.class);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (field.getFieldId().equals(id)) {
          found.setValue(field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
  }

  @Override
  public <X extends IFormField> X getFieldById(final String id, final Class<X> type) {
    final Holder<X> found = new Holder<X>(type);
    IFormFieldVisitor v = new IFormFieldVisitor() {
      @Override
      @SuppressWarnings("unchecked")
      public boolean visitField(IFormField field, int level, int fieldIndex) {
        if (type.isAssignableFrom(field.getClass()) && field.getFieldId().equals(id)) {
          found.setValue((X) field);
        }
        return found.getValue() == null;
      }
    };
    visitFields(v, 0);
    return found.getValue();
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
  public List<IFormField> getFields() {
    return CollectionUtility.unmodifiableListCopy(m_fields);
  }

  @Override
  public boolean visitFields(IFormFieldVisitor visitor, int startLevel) {
    // myself
    if (!visitor.visitField(this, startLevel, 0)) {
      return false;
    }
    // children
    int index = 0;
    for (IFormField field : m_fields) {
      if (field instanceof ICompositeField) {
        if (!((ICompositeField) field).visitFields(visitor, startLevel + 1)) {
          return false;
        }
      }
      else {
        if (!visitor.visitField(field, startLevel, index)) {
          return false;
        }
      }
      index++;
    }
    return true;
  }

  @Override
  public final int getGridColumnCount() {
    return 1;
  }

  @Override
  public final int getGridRowCount() {
    return 1;
  }

  @Override
  public void rebuildFieldGrid() {
    GridData gd = getListBoxFilterBox().getGridDataHints();
    gd.x = 0;
    gd.y = 0;
    getListBoxFilterBox().setGridDataInternal(gd);
  }

  @Order(1)
  public class ListBoxFilterBox extends AbstractListBoxFilterBox {
    @Override
    protected IListBox getListBox() {
      return AbstractListBox.this;
    }
  }

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
    public class KeyColumn extends AbstractColumn<T> {
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
      public Class<T> getDataType() {
        return TypeCastUtility.getGenericsParameterClass(AbstractListBox.this.getClass(), IListBox.class);
      }
    }

    @Order(2)
    public class TextColumn extends AbstractStringColumn {

    }

    @Order(3)
    public class ActiveColumn extends AbstractBooleanColumn {
      @Override
      protected boolean getConfiguredDisplayable() {
        return false;
      }
    }
  }

  /*
   * UI Notifications
   */
  protected class P_ListBoxUIFacade implements IListBoxUIFacade {
  }

  private class P_TableRowBuilder extends AbstractTableRowBuilder<T> {

    @Override
    protected ITableRow createEmptyTableRow() {
      return new TableRow(getTable().getColumnSet());
    }

    @Override
    public ITableRow createTableRow(ILookupRow<T> dataRow) throws ProcessingException {
      TableRow tableRow = (TableRow) super.createTableRow(dataRow);
      // fill values to tableRow
      getKeyColumnInternal().setValue(tableRow, dataRow.getKey());
      getTextColumnInternal().setValue(tableRow, dataRow.getText());
      getActiveColumnInternal().setValue(tableRow, dataRow.isActive());

      //enable/disabled row
      Cell cell = tableRow.getCellForUpdate(1);
      cell.setEnabled(dataRow.isEnabled());

      // hint for inactive codes
      if (!dataRow.isActive()) {
        if (cell.getFont() == null) {
          cell.setFont(FontSpec.parse("italic"));
        }
        getTextColumnInternal().setValue(tableRow, dataRow.getText() + " (" + ScoutTexts.get("InactiveState") + ")");
      }
      return tableRow;
    }
  }
}
