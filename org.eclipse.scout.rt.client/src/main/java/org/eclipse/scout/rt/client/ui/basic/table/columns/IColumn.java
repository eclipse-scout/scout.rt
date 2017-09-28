/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.IStyleable;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.TableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizer;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.classid.ITypeWithClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.IPropertyObserver;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.dimension.IVisibleDimension;

public interface IColumn<VALUE> extends IPropertyObserver, ITypeWithClassId, IOrdered, IStyleable, IHtmlCapable, IVisibleDimension {
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
   * type int
   */
  String PROP_MIN_WIDTH = "minWidth";
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

  String PROP_ORDER = "order";

  /**
   * type boolean
   */
  String PROP_UI_SORT_POSSIBLE = "uiSortPossible";

  int MIN_WIDTH = 60;
  int NARROW_MIN_WIDTH = 32;

  void initColumn();

  void disposeColumn();

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

  VALUE getValue(int rowIndex);

  VALUE getValue(ITableRow r);

  List<VALUE> getValues(Collection<? extends ITableRow> rows);

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
   */
  void setValue(ITableRow r, VALUE value);

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
   */
  void setValue(int rowIndex, VALUE value);

  /**
   * fill all values in this column with the new value
   */
  void fill(VALUE rawValue);

  List<VALUE> getValues();

  List<VALUE> getValues(boolean includeDeleted);

  List<VALUE> getSelectedValues();

  List<VALUE> getInsertedValues();

  List<VALUE> getUpdatedValues();

  List<VALUE> getDeletedValues();

  List<VALUE> getNotDeletedValues();

  /**
   * @return display text for this row's cell on this column
   */
  String getDisplayText(ITableRow r);

  /**
   * @return display texts for all row's cells on this column
   */
  List<String> getDisplayTexts();

  /**
   * @return display text for selected row's cell on this column
   */
  String getSelectedDisplayText();

  /**
   * @return display texts for all selected row's cells on this column
   */
  List<String> getSelectedDisplayTexts();

  Class<VALUE> getDataType();

  /**
   * first selected value
   */
  VALUE getSelectedValue();

  List<ITableRow> findRows(Collection<? extends VALUE> values);

  List<ITableRow> findRows(VALUE value);

  ITableRow findRow(VALUE value);

  /**
   * @return true if column contains value at least one time
   */
  boolean contains(VALUE value);

  /**
   * @return true if column is not unique, that means at least one value occurs more than one time
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

  boolean isInitialGrouped();

  void setInitialGrouped(boolean b);

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

  int getMinWidth();

  void setMinWidth(int w);

  /**
   * @return <code>true</code>, if the column width is fixed, meaning that it is not changed by resizing/auto-resizing
   *         and cannot be resized by the user.
   */
  boolean isFixedWidth();

  void setFixedWidth(boolean fixedWidth);

  int getVisibleColumnIndexHint();

  void setVisibleColumnIndexHint(int index);

  /**
   * A column is presented to the user in the table when it is displayable AND visible<br>
   * this column is basically displayable to the user<br>
   * this property is used in combination with isVisible
   */
  boolean isDisplayable();

  void setDisplayable(boolean b);

  /**
   * @return Returns whether the column is visible to the user using the three properties visible, dispalyable and
   *         visiblePermission.
   */
  boolean isVisible();

  void setVisible(boolean b);

  /**
   * the value in this column is part of the row primary key
   */
  boolean isPrimaryKey();

  /**
   * the value in this column is part of the row summary text (for example in a explorer tree node)
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
   * <0 for left alignment 0 for center alignment and > 0 for right alignment. This alignment is used for header cell
   * and all column cells. Cell specific alignments can be applied by overriding the decorateCell methods.
   */
  int getHorizontalAlignment();

  /**
   * Is a simplified ui sorting possible? If enabled, ui (e.g. web browser, presentation layer itself) may sort the
   * column without computing the order on a client server (e.g. application server). The ui uses a very simplified sort
   * rule set, e.g. alphabetical or numerical sorting is possible, more advanced sort rules are not implemented. Which
   * rules to use are determined by the client. To use advanced sort rules return <code>false</code>.
   *
   * @see ITable#isUiSortPossible()
   */
  boolean isUiSortPossible();

  void setUiSortPossible(boolean uiSortPossible);

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

  boolean isGroupingActive();

  /**
   * Used for mapping an {@link AbstractTableRowData} to an {@link ITableRow} by {@link TableRowDataMapper}
   * <p>
   * Do not use this method for normal value setting! Use {@link IColumn#setValue(ITableRow, Object)} instead.
   * <p>
   * The default implementation writes the given value directly into the corresponding cell.
   */
  void importValue(ITableRow row, Object value);

  /**
   * Parse row and set value to ensure correct type and valid value
   */
  void parseValueAndSet(ITableRow row, Object rawValue);

  /**
   * @param rawValue
   * @return value in correct type, derived from rawValue
   * @throws ProcessingException
   *           parse AND validate value
   */
  VALUE/* validValue */ parseValue(ITableRow row, Object rawValue);

  /**
   * validate cell value on a row
   */
  VALUE/* validValue */ validateValue(ITableRow row, VALUE rawValue);

  /**
   * Prepare editing of a cell in the table.
   * <p>
   * Cell editing is canceled (normally by typing escape) or saved (normally by clicking another cell, typing enter).
   * <p>
   * When saved, the method {@link #completeEdit(ITableRow, IFormField)} is called on this column.
   *
   * @param row
   *          on which editing occurs
   * @return a field for editing, use super.{@link #prepareEdit(ITableRow)} for the default implementation.
   */
  IFormField prepareEdit(ITableRow row);

  /**
   * Complete editing of a cell
   */
  void completeEdit(ITableRow row, IFormField editingField);

  void decorateCell(ITableRow row);

  void decorateCells(List<ITableRow> row);

  void updateDisplayTexts(List<ITableRow> row);

  void updateDisplayText(ITableRow row, Cell cell);

  void decorateHeaderCell();

  /**
   * true: Whenever table content changes, automatically calculate optimized column width so that all column content is
   * displayed without cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   */
  boolean isAutoOptimizeWidth();

  /**
   * see {@link #isAutoOptimizeWidth()}
   */
  void setAutoOptimizeWidth(boolean optimize);

  boolean isMandatory();

  void setMandatory(boolean mandatory);

  /**
   * @return true if column content is valid, no error status is set on column and mandatory property is met.
   */
  boolean isContentValid(ITableRow row);

  /**
   * Ensure that a displayable columns is visible, if there is an error
   */
  void ensureVisibileIfInvalid(ITableRow row);

  /**
   * Initialize cell properties with column properties
   */
  void initCell(ITableRow row);

  /**
   * Delegates to {@link ITableOrganizer#isColumnRemovable(IColumn)}.
   */
  boolean isRemovable();

  /**
   * Delegates to {@link ITableOrganizer#isColumnModifiable(IColumn)}.
   */
  boolean isModifiable();

}
