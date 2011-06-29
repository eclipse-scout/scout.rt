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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.lang.reflect.Array;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.beans.AbstractPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columnfilter.ITableColumnFilterManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractColumn<T> extends AbstractPropertyObserver implements IColumn<T> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractColumn.class);

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private ITable m_table;
  private final HeaderCell m_headerCell;
  private boolean m_primaryKey;
  private boolean m_editable;
  private boolean m_summary;
  /**
   * A column is presented to the user when it is displayable AND visible this
   * column is visible to the user only used when displayable=true
   */
  private boolean m_visibleProperty;
  private boolean m_visibleGranted;
  private int m_initialWidth;
  private boolean m_initialVisible;
  private int m_initialSortIndex;
  private boolean m_initialSortAscending;
  private boolean m_initialAlwaysIncludeSortAtBegin;
  private boolean m_initialAlwaysIncludeSortAtEnd;

  public AbstractColumn() {
    m_headerCell = new HeaderCell();
    initConfig();
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredVisible() {
    return true;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  @ConfigPropertyValue("null")
  protected String getConfiguredHeaderText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  @ConfigPropertyValue("null")
  protected String getConfiguredHeaderTooltipText() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(40)
  @ConfigPropertyValue("null")
  protected String getConfiguredHeaderForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(50)
  @ConfigPropertyValue("null")
  protected String getConfiguredHeaderBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  @ConfigPropertyValue("null")
  protected String getConfiguredHeaderFont() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  @ConfigPropertyValue("60")
  protected int getConfiguredWidth() {
    return 60;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredDisplayable() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredPrimaryKey() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(95)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredEditable() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredSummary() {
    return false;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(110)
  @ConfigPropertyValue("null")
  protected String getConfiguredForegroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.COLOR)
  @Order(120)
  @ConfigPropertyValue("null")
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(130)
  @ConfigPropertyValue("null")
  protected String getConfiguredFont() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(140)
  @ConfigPropertyValue("-1")
  protected int getConfiguredSortIndex() {
    return -1;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredSortAscending() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
    return false;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(170)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAlwaysIncludeSortAtEnd() {
    return false;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(180)
  @ConfigPropertyValue("-1")
  protected int getConfiguredHorizontalAlignment() {
    return -1;
  }

  /**
   * true: Whenever table content changes, automatically calculate optimized column width so that all column content is
   * displayed without
   * cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   * <p>
   * This feature is not supported in SWT and RWT since SWT does not offer such an api method.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(190)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredAutoOptimizeWidth() {
    return false;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(200)
  @ConfigPropertyValue("null")
  protected String getConfiguredDoc() {
    return null;
  }

  @ConfigOperation
  @Order(10)
  protected void execInitColumn() throws ProcessingException {
  }

  @ConfigOperation
  @Order(15)
  protected void execDisposeColumn() throws ProcessingException {
  }

  /**
   * Parse is the process of transforming an arbitrary object to the correct type or throwing an exception.
   * <p>
   * see also {@link #execValidateValue(ITableRow, Object)}
   * 
   * @param rawValue
   * @return value in correct type, derived from rawValue
   * @throws ProcessingException
   *           parse AND validate value
   */
  @ConfigOperation
  @Order(20)
  protected T/* validValue */execParseValue(ITableRow row, Object rawValue) throws ProcessingException {
    return parseValueInternal(row, rawValue);
  }

  /**
   * Validate is the process of checking range, domain, bounds, correctness etc. of an already correctly typed value or
   * throwing an exception.
   * <p>
   * see also {@link #execParseValue(ITableRow, Object)}
   * 
   * @param rawValue
   * @return validated value
   * @throws ProcessingException
   */
  @ConfigOperation
  @Order(30)
  protected T/* validValue */execValidateValue(ITableRow row, T rawValue) throws ProcessingException {
    return validateValueInternal(row, rawValue);
  }

  @ConfigOperation
  @Order(40)
  protected void execDecorateCell(Cell cell, ITableRow row) throws ProcessingException {
  }

  @ConfigOperation
  @Order(50)
  protected void execDecorateHeaderCell(HeaderCell cell) throws ProcessingException {
  }

  /**
   * @return true if the cell (row, column) is editable
   *         <p>
   *         use this method only for dynamic checks of editable otherwise use {@link #getConfiguredEditable()}
   *         <p>
   *         make sure to first make the super call that checks for default editable on table, row and column.
   */
  @ConfigOperation
  @Order(60)
  protected boolean execIsEditable(ITableRow row) throws ProcessingException {
    return getTable() != null && getTable().isEnabled() && this.isVisible() && row.getCell(this).isEnabled() && this.isEditable() && row != null && row.isEnabled();
  }

  /**
   * Prepare editing of a cell in the table.
   * <p>
   * Cell editing is canceled (normally by typing escape) or saved (normally by clicking another cell, typing enter).
   * <p>
   * When saved, the method {@link #completeEdit(ITableRow, IFormField)} /
   * {@link #execCompleteEdit(ITableRow, IFormField)} is called on this column.
   * 
   * @param row
   *          on which editing occurs
   * @return a field for editing, use super.{@link #execPrepareEdit(ITableRow)} for the default implementation.
   */
  @SuppressWarnings("unchecked")
  @ConfigOperation
  @Order(61)
  protected IFormField execPrepareEdit(ITableRow row) throws ProcessingException {
    IFormField f = prepareEditInternal(row);
    if (f != null) {
      f.setLabelVisible(false);
      GridData gd = f.getGridData();
      gd.horizontalAlignment = getHorizontalAlignment();
      f.setGridDataInternal(gd);
      if (f instanceof IValueField<?>) {
        ((IValueField<T>) f).setValue(getValue(row));
      }
      f.markSaved();
    }
    return f;
  }

  /**
   * Complete editing of a cell
   * <p>
   * By default this calls {@link #setValue(int, Object)} and delegates to {@link #execParseValue(ITableRow, Object)}
   * and {@link #execValidateValue(ITableRow, Object)}.
   */
  @ConfigOperation
  @Order(62)
  protected void execCompleteEdit(ITableRow row, IFormField editingField) throws ProcessingException {
    if (editingField instanceof IValueField) {
      IValueField v = (IValueField) editingField;
      if (v.isSaveNeeded()) {
        setValue(row, parseValue(row, ((IValueField) editingField).getValue()));
      }
    }
  }

  protected void initConfig() {
    setAutoOptimizeWidth(getConfiguredAutoOptimizeWidth());
    m_visibleGranted = true;
    m_headerCell.setText(getConfiguredHeaderText());
    if (getConfiguredHeaderTooltipText() != null) {
      m_headerCell.setTooltipText(getConfiguredHeaderTooltipText());
    }
    if (getConfiguredHeaderForegroundColor() != null) {
      m_headerCell.setForegroundColor((getConfiguredHeaderForegroundColor()));
    }
    if (getConfiguredHeaderBackgroundColor() != null) {
      m_headerCell.setBackgroundColor((getConfiguredHeaderBackgroundColor()));
    }
    if (getConfiguredHeaderFont() != null) {
      m_headerCell.setFont(FontSpec.parse(getConfiguredHeaderFont()));
    }
    m_headerCell.setHorizontalAlignment(getConfiguredHorizontalAlignment());
    setHorizontalAlignment(getConfiguredHorizontalAlignment());

    setDisplayable(getConfiguredDisplayable());
    setVisible(getConfiguredVisible());

    setInitialWidth(getConfiguredWidth());
    setInitialVisible(getConfiguredVisible());
    setInitialSortIndex(getConfiguredSortIndex());
    setInitialSortAscending(getConfiguredSortAscending());
    setInitialAlwaysIncludeSortAtBegin(getConfiguredAlwaysIncludeSortAtBegin());
    setInitialAlwaysIncludeSortAtEnd(getConfiguredAlwaysIncludeSortAtEnd());
    //
    setWidth(getConfiguredWidth());
    m_primaryKey = getConfiguredPrimaryKey();
    m_summary = getConfiguredSummary();
    setEditable(getConfiguredEditable());
    setVisibleColumnIndexHint(-1);
    if (getConfiguredForegroundColor() != null) {
      setForegroundColor((getConfiguredForegroundColor()));
    }
    if (getConfiguredBackgroundColor() != null) {
      setBackgroundColor((getConfiguredBackgroundColor()));
    }
    if (getConfiguredFont() != null) {
      setFont(FontSpec.parse(getConfiguredFont()));
    }
  }

  /*
   * Runtime
   */

  public void initColumn() throws ProcessingException {
    ClientUIPreferences env = ClientUIPreferences.getInstance();
    setVisible(env.getTableColumnVisible(this, m_visibleProperty));
    setWidth(env.getTableColumnWidth(this, getWidth()));
    setVisibleColumnIndexHint(env.getTableColumnViewIndex(this, getVisibleColumnIndexHint()));
    //
    execInitColumn();
  }

  public void disposeColumn() throws ProcessingException {
    execDisposeColumn();
  }

  public boolean isInitialVisible() {
    return m_initialVisible;
  }

  public void setInitialVisible(boolean b) {
    m_initialVisible = b;
  }

  public int getInitialSortIndex() {
    return m_initialSortIndex;
  }

  public void setInitialSortIndex(int i) {
    m_initialSortIndex = i;
  }

  public boolean isInitialSortAscending() {
    return m_initialSortAscending;
  }

  public void setInitialSortAscending(boolean b) {
    m_initialSortAscending = b;
  }

  public boolean isInitialAlwaysIncludeSortAtBegin() {
    return m_initialAlwaysIncludeSortAtBegin;
  }

  public void setInitialAlwaysIncludeSortAtBegin(boolean b) {
    m_initialAlwaysIncludeSortAtBegin = b;
  }

  public boolean isInitialAlwaysIncludeSortAtEnd() {
    return m_initialAlwaysIncludeSortAtEnd;
  }

  public void setInitialAlwaysIncludeSortAtEnd(boolean b) {
    m_initialAlwaysIncludeSortAtEnd = b;
  }

  /**
   * controls the displayable property of the column
   */
  public void setVisiblePermission(Permission p) {
    boolean b;
    if (p != null) {
      b = SERVICES.getService(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  public ITable getTable() {
    return m_table;
  }

  /**
   * do not use this internal method
   */
  public void setTableInternal(ITable table) {
    m_table = table;
  }

  public int getColumnIndex() {
    return m_headerCell.getColumnIndex();
  }

  public String getColumnId() {
    String s = getClass().getSimpleName();
    if (s.endsWith("Column")) {
      s = s.replaceAll("Column$", "");
    }
    //do not remove other suffixes
    return s;
  }

  @SuppressWarnings("unchecked")
  public T getValue(ITableRow r) {
    return (r != null) ? (T) r.getCellValue(getColumnIndex()) : null;
  }

  public T getValue(int rowIndex) {
    return getValue(getTable().getRow(rowIndex));
  }

  public void setValue(int rowIndex, T rawValue) throws ProcessingException {
    setValue(getTable().getRow(rowIndex), rawValue);
  }

  public void setValue(ITableRow r, T rawValue) throws ProcessingException {
    T newValue = validateValue(r, rawValue);
    r.setCellValue(getColumnIndex(), newValue);
  }

  public void fill(T rawValue) throws ProcessingException {
    ITableRow[] rows = getTable().getRows();
    for (ITableRow row : rows) {
      setValue(row, rawValue);
    }
  }

  @SuppressWarnings("unchecked")
  public Class<T> getDataType() {
    return TypeCastUtility.getGenericsParameterClass(getClass(), IColumn.class);
  }

  @SuppressWarnings("unchecked")
  public T[] getValues() {
    T[] values = (T[]) Array.newInstance(getDataType(), m_table.getRowCount());
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      values[i] = getValue(m_table.getRow(i));
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  public T[] getValues(ITableRow[] rows) {
    T[] values = (T[]) Array.newInstance(getDataType(), rows.length);
    for (int i = 0; i < rows.length; i++) {
      values[i] = getValue(rows[i]);
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  public T[] getSelectedValues() {
    ITableRow[] rows = m_table.getSelectedRows();
    T[] values = (T[]) Array.newInstance(getDataType(), rows.length);
    for (int i = 0; i < rows.length; i++) {
      values[i] = getValue(rows[i]);
    }
    return values;
  }

  public T getSelectedValue() {
    ITableRow row = m_table.getSelectedRow();
    if (row != null) return getValue(row);
    else return null;
  }

  public String getDisplayText(ITableRow r) {
    return r.getCell(getColumnIndex()).getText();
  }

  public String[] getDisplayTexts() {
    String[] values = new String[m_table.getRowCount()];
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      values[i] = getDisplayText(m_table.getRow(i));
    }
    return values;
  }

  public String getSelectedDisplayText() {
    ITableRow row = m_table.getSelectedRow();
    if (row != null) return getDisplayText(row);
    else return null;
  }

  public String[] getSelectedDisplayTexts() {
    ITableRow[] rows = m_table.getSelectedRows();
    String[] values = new String[rows.length];
    for (int i = 0; i < rows.length; i++) {
      values[i] = getDisplayText(rows[i]);
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  public T[] getInsertedValues() {
    ITableRow[] rows = m_table.getInsertedRows();
    T[] values = (T[]) Array.newInstance(getDataType(), rows.length);
    for (int i = 0; i < rows.length; i++) {
      values[i] = getValue(rows[i]);
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  public T[] getUpdatedValues() {
    ITableRow[] rows = m_table.getUpdatedRows();
    T[] values = (T[]) Array.newInstance(getDataType(), rows.length);
    for (int i = 0; i < rows.length; i++) {
      values[i] = getValue(rows[i]);
    }
    return values;
  }

  @SuppressWarnings("unchecked")
  public T[] getDeletedValues() {
    ITableRow[] rows = m_table.getDeletedRows();
    T[] values = (T[]) Array.newInstance(getDataType(), rows.length);
    for (int i = 0; i < rows.length; i++) {
      values[i] = getValue(rows[i]);
    }
    return values;
  }

  public ITableRow[] findRows(T[] values) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        ITableRow row = findRow(values[i]);
        if (row != null) {
          rowList.add(row);
        }
      }
    }
    return rowList.toArray(new ITableRow[0]);
  }

  public ITableRow[] findRows(T value) {
    ArrayList<ITableRow> rowList = new ArrayList<ITableRow>();
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        rowList.add(row);
      }
    }
    return rowList.toArray(new ITableRow[0]);
  }

  public ITableRow findRow(T value) {
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        return row;
      }
    }
    return null;
  }

  public boolean contains(T value) {
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        return true;
      }
    }
    return false;
  }

  public boolean containsDuplicateValues() {
    return new HashSet<T>(Arrays.asList(getValues())).size() < getValues().length;
  }

  public boolean isEmpty() {
    if (m_table != null) {
      for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
        Object value = getValue(m_table.getRow(i));
        if (value != null) {
          return false;
        }
      }
    }
    return true;
  }

  public void setColumnIndexInternal(int index) {
    m_headerCell.setColumnIndexInternal(index);
  }

  public boolean isSortActive() {
    return getHeaderCell().isSortActive();
  }

  public boolean isSortExplicit() {
    return getHeaderCell().isSortExplicit();
  }

  public boolean isSortAscending() {
    return getHeaderCell().isSortAscending();
  }

  public boolean isSortPermanent() {
    return getHeaderCell().isSortPermanent();
  }

  public int getSortIndex() {
    ITable table = getTable();
    if (table != null) {
      ColumnSet cs = table.getColumnSet();
      if (cs != null) {
        return cs.getSortColumnIndex(this);
      }
    }
    return -1;
  }

  public boolean isColumnFilterActive() {
    ITable table = getTable();
    if (table != null) {
      ITableColumnFilterManager m = table.getColumnFilterManager();
      if (m != null) {
        return m.getFilter(this) != null;
      }
    }
    return false;
  }

  /**
   * sorting of rows based on this column<br>
   * default: compare objects by Comparable interface or use value
   */
  @SuppressWarnings("unchecked")
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    int c;
    T o1 = getValue(r1);
    T o2 = getValue(r2);
    if (o1 == null && o2 == null) {
      c = 0;
    }
    else if (o1 == null) {
      c = -1;
    }
    else if (o2 == null) {
      c = 1;
    }
    else if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
      c = ((Comparable) o1).compareTo(o2);
    }
    else {
      c = StringUtility.compareIgnoreCase(o1.toString(), o2.toString());
    }
    return c;
  }

  public final T/* validValue */parseValue(ITableRow row, Object rawValue) throws ProcessingException {
    T parsedValue = execParseValue(row, rawValue);
    return validateValue(row, parsedValue);
  }

  /**
   * do not use or override this internal method<br>
   * subclasses perform specific value validations here and set the
   * default textual representation of the value
   */
  protected T/* validValue */parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    return TypeCastUtility.castValue(rawValue, getDataType());
  }

  public T/* validValue */validateValue(ITableRow row, T rawValue) throws ProcessingException {
    return execValidateValue(row, rawValue);
  }

  /**
   * do not use or override this internal method<br>
   * subclasses perform specific value validations here and set the
   * default textual representation of the value
   */
  protected T/* validValue */validateValueInternal(ITableRow row, T rawValue) throws ProcessingException {
    return rawValue;
  }

  public final IFormField prepareEdit(ITableRow row) throws ProcessingException {
    ITable table = getTable();
    if (table == null || !table.isCellEditable(row, this)) {
      return null;
    }
    IFormField f = execPrepareEdit(row);
    if (f != null) {
      f.setLabelVisible(false);
      GridData gd = f.getGridDataHints();
      gd.weightY = 1;
      f.setGridDataHints(gd);
    }
    return f;
  }

  /**
   * do not use or override this internal method
   */
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractValueField<T> f = new AbstractValueField<T>() {
      @Override
      public Class<T> getHolderType() {
        return AbstractColumn.this.getDataType();
      }
    };
    return f;
  }

  /**
   * Complete editing of a cell
   * <p>
   * By default this calls {@link #setValue(ITableRow, Object)} and delegates to
   * {@link #execParseValue(ITableRow, Object)} and {@link #execValidateValue(ITableRow, Object)}.
   */
  public final void completeEdit(ITableRow row, IFormField editingField) throws ProcessingException {
    ITable table = getTable();
    if (table == null || !table.isCellEditable(row, this)) {
      return;
    }
    execCompleteEdit(row, editingField);
  }

  public void decorateCell(ITableRow row) {
    Cell cell = row.getCellForUpdate(getColumnIndex());
    decorateCellInternal(cell, row);
    try {
      execDecorateCell(cell, row);
    }
    catch (ProcessingException e) {
      LOG.warn(null, e);
    }
    catch (Throwable t) {
      LOG.warn(null, t);
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    if (getForegroundColor() != null) {
      cell.setForegroundColor(getForegroundColor());
    }
    if (getBackgroundColor() != null) {
      cell.setBackgroundColor(getBackgroundColor());
    }
    if (getFont() != null) {
      cell.setFont(getFont());
    }
    cell.setHorizontalAlignment(getConfiguredHorizontalAlignment());
    cell.setEditableInternal(isCellEditable(row));
  }

  public void decorateHeaderCell() {
    HeaderCell cell = m_headerCell;
    decorateHeaderCellInternal(cell);
    try {
      execDecorateHeaderCell(cell);
    }
    catch (ProcessingException e) {
      LOG.warn(null, e);
    }
    catch (Throwable t) {
      LOG.warn(null, t);
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void decorateHeaderCellInternal(HeaderCell cell) {
  }

  public IHeaderCell getHeaderCell() {
    return m_headerCell;
  }

  public int getVisibleColumnIndexHint() {
    return propertySupport.getPropertyInt(PROP_VIEW_COLUMN_INDEX_HINT);
  }

  public void setVisibleColumnIndexHint(int index) {
    int oldIndex = getVisibleColumnIndexHint();
    if (oldIndex != index) {
      propertySupport.setPropertyInt(PROP_VIEW_COLUMN_INDEX_HINT, index);
    }
  }

  public int getInitialWidth() {
    return m_initialWidth;
  }

  public void setInitialWidth(int w) {
    m_initialWidth = w;
  }

  public int getWidth() {
    return propertySupport.getPropertyInt(PROP_WIDTH);
  }

  public void setWidth(int w) {
    propertySupport.setPropertyInt(PROP_WIDTH, w);
  }

  public void setWidthInternal(int w) {
    propertySupport.setPropertyNoFire(PROP_WIDTH, w);
  }

  public void setHorizontalAlignment(int hAglin) {
    propertySupport.setPropertyInt(PROP_HORIZONTAL_ALIGNMENT, hAglin);
  }

  public int getHorizontalAlignment() {
    return propertySupport.getPropertyInt(PROP_HORIZONTAL_ALIGNMENT);
  }

  public boolean isDisplayable() {
    return propertySupport.getPropertyBool(PROP_DISPLAYABLE);
  }

  public void setDisplayable(boolean b) {
    propertySupport.setPropertyBool(PROP_DISPLAYABLE, b);
    calculateVisible();
  }

  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  private void calculateVisible() {
    propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleGranted && isDisplayable() && m_visibleProperty);
  }

  public boolean isVisibleInternal() {
    return m_visibleProperty;
  }

  public boolean isPrimaryKey() {
    return m_primaryKey;
  }

  public boolean isSummary() {
    return m_summary;
  }

  public boolean isEditable() {
    return m_editable;
  }

  public void setEditable(boolean editable) {
    m_editable = editable;
  }

  public boolean isCellEditable(ITableRow row) {
    try {
      return execIsEditable(row);
    }
    catch (Throwable t) {
      LOG.error("checking row " + row, t);
      return false;
    }
  }

  public String getForegroundColor() {
    return (String) propertySupport.getProperty(PROP_FOREGROUND_COLOR);
  }

  public void setForegroundColor(String c) {
    propertySupport.setProperty(PROP_FOREGROUND_COLOR, c);
  }

  public String getBackgroundColor() {
    return (String) propertySupport.getProperty(PROP_BACKGROUND_COLOR);
  }

  public void setBackgroundColor(String c) {
    propertySupport.setProperty(PROP_BACKGROUND_COLOR, c);
  }

  public FontSpec getFont() {
    return (FontSpec) propertySupport.getProperty(PROP_FONT);
  }

  public void setFont(FontSpec f) {
    propertySupport.setProperty(PROP_FONT, f);
  }

  /**
   * true: Whenever table content changes, automatically calculate optimized column width so that all column content is
   * displayed without
   * cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   */
  public boolean isAutoOptimizeWidth() {
    return propertySupport.getPropertyBool(PROP_AUTO_OPTIMIZE_WIDTH);
  }

  public void setAutoOptimizeWidth(boolean optimize) {
    propertySupport.setPropertyBool(PROP_AUTO_OPTIMIZE_WIDTH, optimize);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getHeaderCell().getText() + " width=" + getWidth() + (isPrimaryKey() ? " primaryKey" : "") + (isSummary() ? " summary" : "") + " viewIndexHint=" + getVisibleColumnIndexHint() + "]";
  }

}
