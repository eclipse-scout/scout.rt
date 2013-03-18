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

import java.security.Permission;

import org.eclipse.scout.commons.beans.IPropertyObserver;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;

public interface IColumn<T> extends IPropertyObserver {
  /**
   * type boolean
   */
  String PROP_VISIBLE = "visible";// defined as: visibleGranted &&
  // displayable && visibleProperty
  /**
   * type boolean
   */
  String PROP_DISPLAYABLE = "displayable";
  /**
   * type int
   */
  String PROP_WIDTH = "width";
  /**
   * type boolean
   */
  String PROP_FIXED_WIDTH = "fixedWidth";
  /**
   * type int
   */
  String PROP_VIEW_COLUMN_INDEX_HINT = "viewColumnIndexHint";

  /**
   * type {@link String}
   */
  String PROP_BACKGROUND_COLOR = "backgroundColor";
  /**
   * type {@link String}
   */
  String PROP_FOREGROUND_COLOR = "foregroundColor";
  /**
   * type {@link ScoutFont}
   */
  String PROP_FONT = "font";
  /**
   * type int
   */
  String PROP_HORIZONTAL_ALIGNMENT = "horizontalAlignment";
  /**
   * type boolean
   */
  String PROP_AUTO_OPTIMIZE_WIDTH = "autoOptimizeWidth";

  String PROP_EDITABLE = "editable";

  String PROP_VIEW_ORDER = "viewOrder";

  void initColumn() throws ProcessingException;

  void disposeColumn() throws ProcessingException;

  void setVisiblePermission(Permission p);

  boolean isVisibleGranted();

  void setVisibleGranted(boolean b);

  int getColumnIndex();

  /**
   * the field ID is the simple class name of a column without the suffix "Column"
   */
  String getColumnId();

  ITable getTable();

  int compareTableRows(ITableRow r1, ITableRow r2);

  T getValue(int rowIndex);

  T getValue(ITableRow r);

  T[] getValues(ITableRow[] rows);

  /**
   * <p>
   * Updates the value of the given row.
   * </p>
   * <p>
   * If any cell editor is active, editing is canceled and it's value rejected.
   * </p>
   * 
   * @param r
   * @param value
   * @throws ProcessingException
   */
  void setValue(ITableRow r, T value) throws ProcessingException;

  /**
   * <p>
   * Updates the value of the given row.
   * </p>
   * <p>
   * If any cell editor is active, editing is canceled and it's value rejected.
   * </p>
   * 
   * @param r
   * @param value
   * @throws ProcessingException
   */
  void setValue(int rowIndex, T value) throws ProcessingException;

  /**
   * fill all values in this column with the new value
   */
  void fill(T rawValue) throws ProcessingException;

  T[] getValues();

  T[] getSelectedValues();

  T[] getInsertedValues();

  T[] getUpdatedValues();

  T[] getDeletedValues();

  /**
   * @return display text for this row's cell on this column
   */
  String getDisplayText(ITableRow r);

  /**
   * @return display texts for all row's cells on this column
   */
  String[] getDisplayTexts();

  /**
   * @return display text for selected row's cell on this column
   */
  String getSelectedDisplayText();

  /**
   * @return display texts for all selected row's cells on this column
   */
  String[] getSelectedDisplayTexts();

  Class<T> getDataType();

  /**
   * first selected value
   */
  T getSelectedValue();

  ITableRow[] findRows(T[] values);

  ITableRow[] findRows(T value);

  ITableRow findRow(T value);

  /**
   * @return true if column contains value at least one time
   */
  boolean contains(T value);

  /**
   * @return true if column is not unique, that means at least one value occurs
   *         more than one time
   */
  boolean containsDuplicateValues();

  /**
   * @return true if column contains no values or all values are null
   */
  boolean isEmpty();

  IHeaderCell getHeaderCell();

  int getInitialWidth();

  void setInitialWidth(int w);

  boolean isInitialVisible();

  void setInitialVisible(boolean b);

  /**
   * @return Returns the column's view order. It determines where this column is arranged in the view.
   */
  double getViewOrder();

  /**
   * Sets the column's view order used for ordering all columns of a table. It is initialized with the column's model
   * order (given by @Order annotation), i.e. the column order values are filled into the table.
   * 
   * @param order
   */
  void setViewOrder(double order);

  int getInitialSortIndex();

  void setInitialSortIndex(int i);

  boolean isInitialSortAscending();

  void setInitialSortAscending(boolean b);

  boolean isInitialAlwaysIncludeSortAtBegin();

  void setInitialAlwaysIncludeSortAtBegin(boolean b);

  boolean isInitialAlwaysIncludeSortAtEnd();

  void setInitialAlwaysIncludeSortAtEnd(boolean b);

  int getWidth();

  void setWidth(int w);

  /**
   * set the width of the column without firing events
   */
  void setWidthInternal(int w);

  /**
   * @return <code>true</code>, if the column width is fixed, meaning that it is not changed by resizing/auto-resizing
   *         and cannot be resized by the user.
   */
  boolean isFixedWidth();

  void setFixedWidth(boolean fixedWidth);

  int getVisibleColumnIndexHint();

  void setVisibleColumnIndexHint(int index);

  /**
   * A column is presented to the user in the table when it is displayable AND
   * visible<br>
   * this column is basically displayable to the user<br>
   * this property is used in combination with isVisible
   */
  boolean isDisplayable();

  void setDisplayable(boolean b);

  /**
   * @return Returns whether the column is visible to the user using the three properties
   *         visible, dispalyable and visiblePermission.
   */
  boolean isVisible();

  void setVisible(boolean b);

  /**
   * @return Returns the column's internal visible state that does no take
   *         permissions or the displayable property into account. This method is used by
   *         the framework only.
   * @see IColumn#isVisible()
   */
  boolean isVisibleInternal();

  /**
   * the value in this column is part of the row primary key
   */
  boolean isPrimaryKey();

  /**
   * the value in this column is part of the row summary text (for example in a
   * explorer tree node)
   */
  boolean isSummary();

  boolean isEditable();

  void setEditable(boolean editable);

  /**
   * @return true if this cell (row, column) is editable <b>and</b> the column is editable {@link #isEditable()}
   *         <p>
   *         It does NOT check if the column is visible, this is done in {@link ITable#isCellEditable(int, int)} et al.
   *         <p>
   *         Note that this is not a java bean method and thus not thread-safe
   */
  boolean isCellEditable(ITableRow row);

  String getForegroundColor();

  void setForegroundColor(String c);

  String getBackgroundColor();

  void setBackgroundColor(String c);

  FontSpec getFont();

  void setFont(FontSpec f);

  void setHorizontalAlignment(int hAglin);

  /**
   * <0 for left alignment 0 for center alignment and > 0 for right alignment.
   * This alignment is used for header cell and all column cells. Cell specific alignments can be
   * applied by overriding the decorateCell methods.
   */
  int getHorizontalAlignment();

  /**
   * <p>
   * When doing single column sort, all previous columns are kept as implicit history and are marked sortExplicit=false
   * <p>
   * When doing multi-column sort, all columns are kept as explicit history and are marked sortExplicit=true
   * <p>
   * Convenience for {@link IHeaderCell#isSortExplicit()}
   */
  boolean isSortExplicit();

  /**
   * Convenience for {@link IHeaderCell#isSortActive()}
   */
  boolean isSortActive();

  /**
   * Convenience to find out if a filter is active
   */
  boolean isColumnFilterActive();

  /**
   * Convenience for {@link IHeaderCell#isSortPermanent()}
   */
  boolean isSortPermanent();

  /**
   * Convenience for {@link IHeaderCell#isSortAscending()}
   */
  boolean isSortAscending();

  /**
   * Convenience for {@link ColumnSet#getSortColumns()} and finding the index
   */
  int getSortIndex();

  /**
   * @param rawValue
   * @return value in correct type, derived from rawValue
   * @throws ProcessingException
   *           parse AND validate value
   */
  T/* validValue */parseValue(ITableRow row, Object rawValue) throws ProcessingException;

  /**
   * validate cell value on a row
   */
  T/* validValue */validateValue(ITableRow row, T rawValue) throws ProcessingException;

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
  IFormField prepareEdit(ITableRow row) throws ProcessingException;

  /**
   * Complete editing of a cell
   * <p>
   * By default this calls {@link #setValue(ITableRow, Object)} and delegates to
   * {@link #execParseValue(ITableRow, Object)} and {@link #execValidateValue(ITableRow, Object)}.
   */
  void completeEdit(ITableRow row, IFormField editingField) throws ProcessingException;

  void decorateCell(ITableRow row);

  void decorateHeaderCell();

  /**
   * true: Whenever table content changes, automatically calculate optimized column width so that all column content is
   * displayed without
   * cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   * <p>
   * This feature is not supported in SWT and RWT since SWT does not offer such an api method.
   */
  boolean isAutoOptimizeWidth();

  /**
   * see {@link #isAutoOptimizeWidth()}
   */
  void setAutoOptimizeWidth(boolean optimize);

  boolean isMandatory();
  
  void setMandatory(boolean mandatory);

}
