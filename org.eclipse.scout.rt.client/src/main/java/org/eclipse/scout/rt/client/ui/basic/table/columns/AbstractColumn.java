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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnCompleteEditChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDecorateCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDecorateHeaderCellChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnDisposeColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnInitColumnChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnParseValueChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnPrepareEditChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.ColumnChains.ColumnValidateValueChain;
import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IColumnExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.ClientUIPreferences;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ColumnSet;
import org.eclipse.scout.rt.client.ui.basic.table.HeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.IHeaderCell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRowCustomValueContributor;
import org.eclipse.scout.rt.client.ui.basic.table.TableRowDataMapper;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.TableUserFilterManager;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPageWithTable;
import org.eclipse.scout.rt.client.ui.form.AbstractForm;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.ValidationFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.tablefield.AbstractTableField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IOrdered;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.Replace;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.reflect.AbstractPropertyObserver;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.CompareUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.data.form.AbstractFormData;
import org.eclipse.scout.rt.shared.extension.AbstractExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("ebe15e4d-017b-4ac0-9a5a-2c9e07c8ad6f")
public abstract class AbstractColumn<VALUE> extends AbstractPropertyObserver implements IColumn<VALUE>, IExtensibleObject {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractColumn.class);

  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private ITable m_table;
  private final HeaderCell m_headerCell;
  private boolean m_primaryKey;
  private boolean m_summary;
  private boolean m_initialized;
  /**
   * A column is presented to the user when it is displayable AND visible this column is visible to the user only used
   * when displayable=true
   */
  private boolean m_visibleProperty;
  private boolean m_visibleGranted;
  private int m_initialWidth;
  private boolean m_initialVisible;
  private boolean m_initialGrouped;
  private int m_initialSortIndex;
  private boolean m_initialSortAscending;
  private boolean m_initialAlwaysIncludeSortAtBegin;
  private boolean m_initialAlwaysIncludeSortAtEnd;

  private final ObjectExtensions<AbstractColumn<VALUE>, IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> m_objectExtensions;

  public AbstractColumn() {
    this(true);
  }

  public AbstractColumn(boolean callInitializer) {
    m_headerCell = new HeaderCell();
    m_objectExtensions = new ObjectExtensions<AbstractColumn<VALUE>, IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>>(this);
    if (callInitializer) {
      callInitializer();
    }
  }

  protected final void callInitializer() {
    if (!m_initialized) {
      interceptInitConfig();
      m_initialized = true;
    }
  }

  protected boolean isInitialized() {
    return m_initialized;
  }

  protected Map<String, Object> getPropertiesMap() {
    return propertySupport.getPropertiesMap();
  }

  /*
   * Configuration
   */

  /**
   * Configures the visibility of this column. If the column must be visible for the user, it must be displayable too
   * (see {@link #getConfiguredDisplayable()}).
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this column is visible, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(10)
  protected boolean getConfiguredVisible() {
    return true;
  }

  /**
   * Configures the header text of this column.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Header text of this column.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(20)
  protected String getConfiguredHeaderText() {
    return null;
  }

  /**
   * Configures the header tooltip of this column.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Tooltip of this column.
   */
  @ConfigProperty(ConfigProperty.TEXT)
  @Order(30)
  protected String getConfiguredHeaderTooltipText() {
    return null;
  }

  /**
   * Configures the header icon of this column.
   *
   * @return the ID (name) of the icon
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(40)
  protected String getConfiguredHeaderIconId() {
    return null;
  }

  /**
   * Configures the header css class(es) of this column.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(110)
  protected String getConfiguredHeaderCssClass() {
    return null;
  }

  /**
   * Configures the color of this column header text. The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Foreground color HEX value of this column header text.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(40)
  protected String getConfiguredHeaderForegroundColor() {
    return null;
  }

  /**
   * Configures the background color of this column header. The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Background color HEX value of this column header.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(50)
  protected String getConfiguredHeaderBackgroundColor() {
    return null;
  }

  /**
   * Configures the font of this column header text. See {@link FontSpec#parse(String)} for the appropriate format.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Font of this column header text.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(60)
  protected String getConfiguredHeaderFont() {
    return null;
  }

  /**
   * Configures the width of this column. The width of a column is represented by an {@code int}. If the table's auto
   * resize flag is set (see {@link AbstractTable#getConfiguredAutoResizeColumns()} ), the ratio of the column widths
   * determines the real column width. If the flag is not set, the column's width is represented by the configured
   * width.
   * <p>
   * Subclasses can override this method. Default is {@code 60}.
   *
   * @return Width of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(70)
  protected int getConfiguredWidth() {
    return 60;
  }

  /**
   * Configures whether the column width is fixed, meaning that it is not changed by resizing/auto-resizing and cannot
   * be resized by the user. If <code>true</code>, the configured width is fixed. Defaults to <code>false</code>.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(75)
  protected boolean getConfiguredFixedWidth() {
    return false;
  }

  /**
   * Configures whether the column is displayable or not. A non-displayable column is always invisible for the user. A
   * displayable column may be visible for a user, depending on {@link #getConfiguredVisible()}.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this column is displayable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(80)
  protected boolean getConfiguredDisplayable() {
    return true;
  }

  /**
   * Configures whether this column value belongs to the primary key of the surrounding table. The table's primary key
   * might consist of several columns. The primary key can be used to find the appropriate row by calling
   * {@link AbstractTable#findRowByKey(List)}.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column value belongs to the primary key of the surrounding table, {@code false}
   *         otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(90)
  protected boolean getConfiguredPrimaryKey() {
    return false;
  }

  /**
   * Configures whether this column is editable or not. A user might directly modify the value of an editable column. A
   * non-editable column is read-only.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column is editable, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(95)
  protected boolean getConfiguredEditable() {
    return false;
  }

  /**
   * Configures whether this column is a summary column. Summary columns are used in case of a table with children. The
   * label of the child node is based on the value of the summary columns. See {@link ITable#getSummaryCell(ITableRow)}
   * for more information.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column is a summary column, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(100)
  protected boolean getConfiguredSummary() {
    return false;
  }

  /**
   * Configures the css class(es) of this column.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return a string containing one or more classes separated by space, or null if no class should be set.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(105)
  protected String getConfiguredCssClass() {
    return null;
  }

  /**
   * Configures, if HTML rendering is enabled for this column.
   * <p>
   * Subclasses can override this method. Default is {@code false}. Make sure that any user input (or other insecure
   * input) is encoded (security), if this property is enabled.
   *
   * @return {@code true}, if HTML rendering is enabled for this column.{@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(105)
  protected boolean getConfiguredHtmlEnabled() {
    return false;
  }

  /**
   * Configures the color of this column text (except color of header text, see
   * {@link #getConfiguredHeaderForegroundColor()}). The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Foreground color HEX value of this column text.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(110)
  protected String getConfiguredForegroundColor() {
    return null;
  }

  /**
   * Configures the background color of this column (except background color of header, see
   * {@link #getConfiguredHeaderBackgroundColor()}. The color is represented by the HEX value (e.g. FFFFFF).
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Background color HEX value of this column.
   */
  @ConfigProperty(ConfigProperty.COLOR)
  @Order(120)
  protected String getConfiguredBackgroundColor() {
    return null;
  }

  /**
   * Configures the font of this column text (except header text, see {@link #getConfiguredHeaderFont()}). See
   * {@link FontSpec#parse(String)} for the appropriate format.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Font of this column text.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(130)
  protected String getConfiguredFont() {
    return null;
  }

  /**
   * Configures the group-by index of this column A group-by index {@code < 0} means that the column is not considered
   * for grouping. For a column to be considered for sorting, the sort index must be {@code >=0}.
   *
   * @return "Group-By" index of this column
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(135)
  protected boolean getConfiguredGrouped() {
    return false;
  }

  /**
   * Configures the sort index of this column. A sort index {@code < 0} means that the column is not considered for
   * sorting. For a column to be considered for sorting, the sort index must be {@code >= 0}. Several columns might have
   * set a sort index. Sorting starts with the column having the the lowest sort index ({@code >= 0}).
   * <p>
   * Subclasses can override this method. Default is {@code -1}.
   *
   * @return Sort index of this column.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(140)
  protected int getConfiguredSortIndex() {
    return -1;
  }

  /**
   * Configures the view order of this column. The view order determines the order in which the columns appear. The
   * order of columns with no view order configured ({@code < 0}) is initialized based on the {@link Order} annotation
   * of the column class.
   * <p>
   * Subclasses can override this method. The default is {@link IOrdered#DEFAULT_ORDER}.
   *
   * @return View order of this column.
   */
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(145)
  protected double getConfiguredViewOrder() {
    return IOrdered.DEFAULT_ORDER;
  }

  /**
   * Configures whether this column is sorted ascending or descending. For a column to be sorted at all, a sort index
   * must be set (see {@link #getConfiguredSortIndex()}).
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this column is sorted ascending, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  protected boolean getConfiguredSortAscending() {
    return true;
  }

  /**
   * Configures whether this column is always included for sort at begin, independent of a sort change by the user. If
   * set to {@code true}, the sort index (see {@link #getConfiguredSortIndex()}) must be set.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column is always included for sort at begin, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(160)
  protected boolean getConfiguredAlwaysIncludeSortAtBegin() {
    return false;
  }

  /**
   * Configures whether this column is always included for sort at end, independent of a sort change by the user. If set
   * to {@code true}, the sort index (see {@link #getConfiguredSortIndex()}) must be set.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column is always included for sort at end, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(170)
  protected boolean getConfiguredAlwaysIncludeSortAtEnd() {
    return false;
  }

  /**
   * Configures the horizontal alignment of text inside this column (including header text).
   * <p>
   * Subclasses can override this method. Default is {@code -1} (left alignment).
   *
   * @return {@code -1} for left, {@code 0} for center and {@code 1} for right alignment.
   */
  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(180)
  protected int getConfiguredHorizontalAlignment() {
    return -1;
  }

  /**
   * Configures whether the column width is auto optimized. If true: Whenever the table content changes, the optimized
   * column width is automatically calculated so that all column content is displayed without cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column width is auto optimized, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(190)
  protected boolean getConfiguredAutoOptimizeWidth() {
    return false;
  }

  /**
   * Configures whether this column value is mandatory / required. This only affects editable columns (see
   * {@link #getConfiguredEditable()} ).
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if this column value is mandatory, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(210)
  protected boolean getConfiguredMandatory() {
    return false;
  }

  /**
   * @see IColumn#isUiSortPossible()
   * @return {@code true} to allow simplified sorting e.g. by web browser, <code>false</code> otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  protected boolean getConfiguredUiSortPossible() {
    return false;
  }

  /**
   * Called after this column has been added to the column set of the surrounding table. This method may execute
   * additional initialization for this column (e.g. register listeners).
   * <p>
   * Do not load table data here, this should be done lazily in {@link AbstractPageWithTable#execLoadTableData()},
   * {@link AbstractTableField#reloadTableData()} or via {@link AbstractForm#importFormData(AbstractFormData)}.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(10)
  protected void execInitColumn() {
  }

  /**
   * Called when the surrounding table is disposed. This method may execute additional cleanup.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(15)
  protected void execDisposeColumn() {
  }

  /**
   * Parse is the process of transforming an arbitrary object to the correct type or throwing an exception.
   * <p>
   * see also {@link #interceptValidateValue(ITableRow, Object)}
   * <p>
   * Subclasses can override this method. The default calls {@link #parseValueInternal(ITableRow, Object)}.
   *
   * @param row
   *          Table row for which to parse the raw value.
   * @param rawValue
   *          Raw value to parse.
   * @return Value in correct type, derived from rawValue.
   */
  @ConfigOperation
  @Order(20)
  protected VALUE/* validValue */ execParseValue(ITableRow row, Object rawValue) {
    return parseValueInternal(row, rawValue);
  }

  /**
   * Validate is the process of checking range, domain, bounds, correctness etc. of an already correctly typed value or
   * throwing an exception.
   * <p>
   * see also {@link #interceptParseValue(ITableRow, Object)}
   * <p>
   * Subclasses can override this method. The default calls {@link #validateValueInternal(ITableRow, Object)}.
   *
   * @param row
   *          Table row for which to validate the raw value.
   * @param rawValue
   *          Already parsed raw value to validate.
   * @return Validated value
   */
  @ConfigOperation
  @Order(30)
  protected VALUE/* validValue */ execValidateValue(ITableRow row, VALUE rawValue) {
    return rawValue;
  }

  /**
   * Called when decorating the table cell. This method may add additional decorations to the table cell.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param cell
   *          Cell to decorate.
   * @param row
   *          Table row of cell.
   */
  @ConfigOperation
  @Order(40)
  protected void execDecorateCell(Cell cell, ITableRow row) {
  }

  /**
   * Called when decorating the table header cell. This method may add additional decorations to the table header cell.
   * <p>
   * Subclasses can override this method. The default does nothing.
   *
   * @param cell
   *          Header cell to decorate.
   */
  @ConfigOperation
  @Order(50)
  protected void execDecorateHeaderCell(HeaderCell cell) {
  }

  /**
   * Prepares the editing of a cell in the table.
   * <p>
   * Cell editing is canceled (normally by typing escape) or saved (normally by clicking another cell, typing enter).
   * <p>
   * When saved, the method {@link #completeEdit(ITableRow, IFormField)} /
   * {@link #interceptCompleteEdit(ITableRow, IFormField)} is called on this column.
   * <p>
   * Subclasses can override this method. The default returns an appropriate field based on the column data type.
   * <p>
   * The mapping from the cell value to the field value is achieved using {@link #cellToEditField(Object, String,
   * IMultiStatus, IFormField))}
   *
   * @param row
   *          on which editing occurs
   * @return a field for editing.
   */
  @ConfigOperation
  @Order(61)
  protected IFormField execPrepareEdit(ITableRow row) {
    IFormField f = prepareEditInternal(row);
    if (f != null) {
      f.setLabelVisible(false);
      cellValueToEditor(row, f);
      f.markSaved();
    }
    return f;
  }

  /**
   * Completes editing of a cell.
   * <p>
   * Subclasses can override this method. The default calls {@link #applyValueInternal(ITableRow, Object)} and delegates
   * to {@link #interceptParseValue(ITableRow, Object)} and {@link #interceptValidateValue(ITableRow, Object)}.
   * <p>
   * The mapping from the field value to the cell value is achieved using {@link #editFieldToCellValue(IFormField)}
   *
   * @param row
   *          on which editing occurred.
   * @param editingField
   *          Field which was used to edit cell value (as returned by {@link #interceptPrepareEdit(ITableRow)}).
   */
  @ConfigOperation
  @Order(62)
  protected void execCompleteEdit(ITableRow row, IFormField editingField) {
    editorValueToCell(row, editingField);
  }

  /**
   * Map the values of a cell to the editing form field. The default implementation assumes a value field.
   *
   * @throws ProcessingException
   *           if the field is not a value field
   */
  protected void editorValueToCell(ITableRow row, IFormField editorField) {
    if (!(editorField instanceof IValueField<?>)) {
      throw new ProcessingException("Expected a value field.");
    }
    else {
      @SuppressWarnings("unchecked")
      IValueField<VALUE> valueField = (IValueField<VALUE>) editorField;
      LOG.debug("complete edit: [value={}, text={}, status={}]", valueField.getValue(), valueField.getDisplayText(), valueField.getErrorStatus());

      String cellAction = "";
      Cell cell = row.getCellForUpdate(this);
      if (!contentEquals(cell, valueField)) {
        // remove existing validation and parsing error (but don't remove other possible error-statuses)
        cell.removeErrorStatus(ValidationFailedStatus.class);
        cell.removeErrorStatus(ParsingFailedStatus.class);

        if (valueField.getErrorStatus() == null) {
          parseValueAndSet(row, valueField.getValue(), true);
          cellAction = "parseAndSetValue";
        }
        else {
          cell.setText(valueField.getDisplayText());
          cell.addErrorStatuses(valueField.getErrorStatus().getChildren());
          cellAction = "setText/addErrorStatuses";
        }
      }

      LOG.debug("cell updated: [value={}, text={}, status={}, cellAction={}]", cell.getValue(), cell.getText(), cell.getErrorStatus(), cellAction);
    }
  }

  /**
   * Map the values of a cell to the editing form field. The default implementation assumes a value field.
   *
   * @param row
   *          the row that is currently edited
   * @param editorField
   *          the field to edit the value
   * @throws ProcessingException
   *           if the field is not a value field
   */
  @SuppressWarnings("unchecked")
  protected void cellValueToEditor(ITableRow row, IFormField editorField) {
    final ICell cell = row.getCell(this);
    IMultiStatus status = cell.getErrorStatus();

    if (status == null || status.isOK()) {
      cellValueToEditField((VALUE) cell.getValue(), editorField);
    }
    else {
      cellTextToEditField(cell.getText(), editorField);
    }
  }

  protected void cellValueToEditField(VALUE cellValue, IFormField editorField) {
    if (!(editorField instanceof IValueField<?>)) {
      throw new ProcessingException("Expected a value field.");
    }
    @SuppressWarnings("unchecked")
    IValueField<VALUE> field = (IValueField<VALUE>) editorField;
    field.setValue(cellValue);
  }

  protected void cellTextToEditField(String cellText, IFormField editorField) {
    if (!(editorField instanceof IValueField<?>)) {
      throw new ProcessingException("Expected a value field.");
    }
    @SuppressWarnings("unchecked")
    IValueField<VALUE> field = (IValueField<VALUE>) editorField;
    field.parseAndSetValue(cellText);
  }

  /**
   * Used in {@link #execCompleteEdit(ITableRow, IFormField)}
   */
  @SuppressWarnings("unchecked")
  protected VALUE editFieldToCellValue(IFormField editField) {
    if (!(editField instanceof IValueField<?>)) {
      throw new ProcessingException("Expected a value field.");
    }
    return (VALUE) ((IValueField) editField).getValue();
  }

  private boolean contentEquals(Cell cell, IValueField<VALUE> field) {
    return CompareUtility.equals(cell.getText(), field.getDisplayText()) && CompareUtility.equals(cell.getValue(), editFieldToCellValue(field)) && CompareUtility.equals(cell.getErrorStatus(), field.getErrorStatus());
  }

  /**
   * <p>
   * Updates the value of the cell with the given value.
   * </p>
   * <p>
   * Thereby, if sorting is enabled on table, it is temporarily suspended to prevent rows from scampering.
   * </p>
   *
   * @param row
   * @param newValue
   */
  protected void applyValueInternal(ITableRow row, VALUE newValue) {
    if (!getTable().isSortEnabled()) {
      setValue(row, newValue);
    }
    else {
      // suspend sorting to prevent rows from scampering
      try {
        getTable().setSortEnabled(false);
        setValue(row, newValue);
      }
      finally {
        getTable().setSortEnabled(true);
      }
    }
  }

  @Override
  public final List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>> createLocalExtension() {
    return new LocalColumnExtension<VALUE, AbstractColumn<VALUE>>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    setAutoOptimizeWidth(getConfiguredAutoOptimizeWidth());
    m_visibleGranted = true;
    m_headerCell.setText(getConfiguredHeaderText());
    if (getConfiguredHeaderTooltipText() != null) {
      m_headerCell.setTooltipText(getConfiguredHeaderTooltipText());
    }
    m_headerCell.setIconId(getConfiguredHeaderIconId());
    m_headerCell.setCssClass(getConfiguredHeaderCssClass());
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
    setInitialGrouped(getConfiguredGrouped());
    setOrder(calculateViewOrder());
    setWidth(getConfiguredWidth());
    setFixedWidth(getConfiguredFixedWidth());
    m_primaryKey = getConfiguredPrimaryKey();
    m_summary = getConfiguredSummary();
    setEditable(getConfiguredEditable());
    setMandatory(getConfiguredMandatory());
    setVisibleColumnIndexHint(-1);
    setCssClass((getConfiguredCssClass()));
    if (getConfiguredForegroundColor() != null) {
      setForegroundColor((getConfiguredForegroundColor()));
    }
    if (getConfiguredBackgroundColor() != null) {
      setBackgroundColor((getConfiguredBackgroundColor()));
    }
    if (getConfiguredFont() != null) {
      setFont(FontSpec.parse(getConfiguredFont()));
    }
    setHtmlEnabled(getConfiguredHtmlEnabled());
    setUiSortPossible(getConfiguredUiSortPossible());
  }

  /**
   * Calculates the column's view order, e.g. if the @Order annotation is set to 30.0, the method will return 30.0. If
   * no {@link Order} annotation is set, the method checks its super classes for an @Order annotation.
   *
   * @since 3.10.0-M4
   */
  protected double calculateViewOrder() {
    double viewOrder = getConfiguredViewOrder();
    Class<?> cls = getClass();
    if (viewOrder == IOrdered.DEFAULT_ORDER) {
      while (cls != null && IColumn.class.isAssignableFrom(cls)) {
        if (cls.isAnnotationPresent(Order.class)) {
          Order order = (Order) cls.getAnnotation(Order.class);
          return order.value();
        }
        cls = cls.getSuperclass();
      }
    }
    return viewOrder;
  }

  /*
   * Runtime
   */

  @Override
  public void initColumn() {
    // Apply prefs only if header is enabled (because the user is not able to change or reset anything if the header is disabled)
    if (getTable() != null && getTable().isHeaderEnabled()) {
      ClientUIPreferences env = ClientUIPreferences.getInstance();
      setVisible(env.getTableColumnVisible(this, m_visibleProperty));
      if (!isFixedWidth()) {
        setWidth(env.getTableColumnWidth(this, getWidth()));
      }
      setVisibleColumnIndexHint(env.getTableColumnViewIndex(this, getVisibleColumnIndexHint()));
    }
    //
    interceptInitColumn();
  }

  @Override
  public void disposeColumn() {
    interceptDisposeColumn();
  }

  /**
   * Initialize cell with column defaults.
   */
  @Override
  public void initCell(ITableRow row) {
    Cell cell = row.getCellForUpdate(this);
    if (getForegroundColor() != null) {
      cell.setForegroundColor(getForegroundColor());
    }
    if (getBackgroundColor() != null) {
      cell.setBackgroundColor(getBackgroundColor());
    }
    if (getFont() != null) {
      cell.setFont(getFont());
    }
    if (getCssClass() != null) {
      cell.setCssClass(getCssClass());
    }

    cell.setHorizontalAlignment(getHorizontalAlignment());
    cell.setEditable(isEditable());
    cell.setHtmlEnabled(isHtmlEnabled());
    cell.setMandatory(isMandatory());
  }

  protected void reinitCells() {
    if (getTable() == null) {
      return;
    }
    for (ITableRow row : getTable().getRows()) {
      initCell(row);
    }
  }

  @Override
  public void setMandatory(boolean mandatory) {
    boolean changed = propertySupport.setPropertyBool(IFormField.PROP_MANDATORY, mandatory);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public boolean isMandatory() {
    return propertySupport.getPropertyBool(IFormField.PROP_MANDATORY);
  }

  @Override
  public boolean isUiSortPossible() {
    return propertySupport.getPropertyBool(IColumn.PROP_UI_SORT_POSSIBLE);
  }

  @Override
  public void setUiSortPossible(boolean uiSortPossible) {
    propertySupport.setPropertyBool(IColumn.PROP_UI_SORT_POSSIBLE, uiSortPossible);
  }

  @Override
  public boolean isInitialVisible() {
    return m_initialVisible;
  }

  @Override
  public void setInitialVisible(boolean b) {
    m_initialVisible = b;
  }

  @Override
  public boolean isInitialGrouped() {
    return m_initialGrouped;
  }

  @Override
  public void setInitialGrouped(boolean b) {
    m_initialGrouped = b;
  }

  @Override
  public int getInitialSortIndex() {
    return m_initialSortIndex;
  }

  @Override
  public void setInitialSortIndex(int i) {
    m_initialSortIndex = i;
  }

  @Override
  public boolean isInitialSortAscending() {
    return m_initialSortAscending;
  }

  @Override
  public void setInitialSortAscending(boolean b) {
    m_initialSortAscending = b;
  }

  @Override
  public boolean isInitialAlwaysIncludeSortAtBegin() {
    return m_initialAlwaysIncludeSortAtBegin;
  }

  @Override
  public void setInitialAlwaysIncludeSortAtBegin(boolean b) {
    m_initialAlwaysIncludeSortAtBegin = b;
  }

  @Override
  public boolean isInitialAlwaysIncludeSortAtEnd() {
    return m_initialAlwaysIncludeSortAtEnd;
  }

  @Override
  public void setInitialAlwaysIncludeSortAtEnd(boolean b) {
    m_initialAlwaysIncludeSortAtEnd = b;
  }

  /**
   * controls the displayable property of the column
   */
  @Override
  public void setVisiblePermission(Permission p) {
    boolean b;
    if (p != null) {
      b = BEANS.get(IAccessControlService.class).checkPermission(p);
    }
    else {
      b = true;
    }
    setVisibleGranted(b);
  }

  @Override
  public boolean isVisibleGranted() {
    return m_visibleGranted;
  }

  @Override
  public void setVisibleGranted(boolean b) {
    m_visibleGranted = b;
    calculateVisible();
  }

  @Override
  public ITable getTable() {
    return m_table;
  }

  /**
   * do not use this internal method
   */
  public void setTableInternal(ITable table) {
    m_table = table;
  }

  @Override
  public int getColumnIndex() {
    return m_headerCell.getColumnIndex();
  }

  @Override
  public String getColumnId() {
    Class<?> c = getClass();
    while (c.isAnnotationPresent(Replace.class)) {
      c = c.getSuperclass();
    }
    String s = c.getSimpleName();
    if (s.endsWith("Column")) {
      s = s.replaceAll("Column$", "");
    }
    //do not remove other suffixes
    return s;
  }

  /**
   * Needs to consider the class id of the container to account for columns in templates.
   */
  @Override
  public String classId() {
    String simpleClassId = ConfigurationUtility.getAnnotatedClassIdWithFallback(getClass(), false);
    if (getTable() != null) {
      return simpleClassId + ID_CONCAT_SYMBOL + getTable().classId();
    }
    return simpleClassId;
  }

  @Override
  public VALUE getValue(ITableRow r) {
    return getValueInternal(r);
  }

  @SuppressWarnings("unchecked")
  protected VALUE getValueInternal(ITableRow r) {
    return (r != null) ? (VALUE) r.getCellValue(getColumnIndex()) : null;
  }

  @Override
  public VALUE getValue(int rowIndex) {
    return getValue(getTable().getRow(rowIndex));
  }

  @Override
  public void setValue(int rowIndex, VALUE rawValue) {
    setValue(getTable().getRow(rowIndex), rawValue);
  }

  @Override
  public void setValue(ITableRow r, VALUE value) {
    setValue(r, value, false);
  }

  protected void setValue(ITableRow r, VALUE value, boolean updateValidDisplayText) {
    try {
      Cell cell = r.getCellForUpdate(this);
      cell.removeErrorStatus(ValidationFailedStatus.class);
      VALUE newValue = validateValue(r, value);

      // set newValue into the cell only if there's no error.
      if (!cell.hasError()) {
        r.setCellValue(getColumnIndex(), newValue);
        if (this instanceof ITableRowCustomValueContributor) {
          ((ITableRowCustomValueContributor) this).enrichCustomValues(r, r.getCustomValues());
        }
      }

      ensureVisibileIfInvalid(r);
      if (updateValidDisplayText) {
        updateDisplayText(r, newValue);
      }
    }
    catch (ProcessingException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error setting column value ", e);
      }
      Cell cell = r.getCellForUpdate(this);
      //add error
      cell.addErrorStatus(new ValidationFailedStatus<VALUE>(e, value));
      updateDisplayText(r, value);
    }
  }

  @Override
  public void fill(VALUE rawValue) {
    for (ITableRow row : getTable().getRows()) {
      setValue(row, rawValue);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<VALUE> getDataType() {
    return TypeCastUtility.getGenericsParameterClass(getClass(), IColumn.class);
  }

  @Override
  public List<VALUE> getValues() {
    int rowCount = m_table.getRowCount();
    List<VALUE> values = new ArrayList<VALUE>(rowCount);
    for (int i = 0; i < rowCount; i++) {
      values.add(getValue(m_table.getRow(i)));
    }
    return values;
  }

  @Override
  public List<VALUE> getValues(boolean includeDeleted) {
    List<VALUE> values = new ArrayList<VALUE>();
    for (ITableRow row : m_table.getRows()) {
      if (includeDeleted || (row.getStatus() != ITableRow.STATUS_DELETED)) {
        values.add(getValue(row));
      }
    }
    return values;
  }

  @Override
  public List<VALUE> getValues(Collection<? extends ITableRow> rows) {
    List<VALUE> values = new ArrayList<VALUE>(rows.size());
    for (ITableRow row : rows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public List<VALUE> getSelectedValues() {
    List<ITableRow> selectedRows = m_table.getSelectedRows();
    List<VALUE> values = new ArrayList<VALUE>(selectedRows.size());
    for (ITableRow row : selectedRows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public VALUE getSelectedValue() {
    ITableRow row = m_table.getSelectedRow();
    if (row != null) {
      return getValue(row);
    }
    else {
      return null;
    }
  }

  @Override
  public String getDisplayText(ITableRow r) {
    return r.getCell(getColumnIndex()).getText();
  }

  @Override
  public List<String> getDisplayTexts() {
    List<String> values = new ArrayList<String>(m_table.getRowCount());
    for (int i = 0; i < m_table.getRowCount(); i++) {
      values.add(getDisplayText(m_table.getRow(i)));
    }
    return values;
  }

  @Override
  public String getSelectedDisplayText() {
    ITableRow row = m_table.getSelectedRow();
    if (row != null) {
      return getDisplayText(row);
    }
    else {
      return null;
    }
  }

  @Override
  public List<String> getSelectedDisplayTexts() {
    List<ITableRow> selectedRows = m_table.getSelectedRows();
    List<String> values = new ArrayList<String>(selectedRows.size());
    for (ITableRow row : selectedRows) {
      values.add(getDisplayText(row));
    }
    return values;
  }

  @Override
  public List<VALUE> getInsertedValues() {
    List<ITableRow> insertedRows = m_table.getInsertedRows();
    List<VALUE> values = new ArrayList<VALUE>(insertedRows.size());
    for (ITableRow row : insertedRows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public List<VALUE> getUpdatedValues() {
    List<ITableRow> updatedRows = m_table.getUpdatedRows();
    List<VALUE> values = new ArrayList<VALUE>(updatedRows.size());
    for (ITableRow row : updatedRows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public List<VALUE> getDeletedValues() {
    List<ITableRow> deletedRows = m_table.getDeletedRows();
    List<VALUE> values = new ArrayList<VALUE>(deletedRows.size());
    for (ITableRow row : deletedRows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public List<VALUE> getNotDeletedValues() {
    List<ITableRow> notDeletedRows = m_table.getNotDeletedRows();
    List<VALUE> values = new ArrayList<VALUE>(notDeletedRows.size());
    for (ITableRow row : notDeletedRows) {
      values.add(getValue(row));
    }
    return values;
  }

  @Override
  public List<ITableRow> findRows(Collection<? extends VALUE> keys) {
    if (keys != null) {
      List<ITableRow> values = new ArrayList<ITableRow>(keys.size());
      for (VALUE t : keys) {
        ITableRow row = findRow(t);
        if (row != null) {
          values.add(row);
        }
      }
      return values;
    }
    return CollectionUtility.emptyArrayList();
  }

  @Override
  public List<ITableRow> findRows(VALUE value) {
    List<ITableRow> values = new ArrayList<ITableRow>();
    for (int i = 0; i < m_table.getRowCount(); i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        values.add(row);
      }
    }
    return values;
  }

  @Override
  public ITableRow findRow(VALUE value) {
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        return row;
      }
    }
    return null;
  }

  @Override
  public boolean contains(VALUE value) {
    for (int i = 0, ni = m_table.getRowCount(); i < ni; i++) {
      ITableRow row = m_table.getRow(i);
      if (CompareUtility.equals(value, getValue(row))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean containsDuplicateValues() {
    return new HashSet<VALUE>(getValues()).size() < getValues().size();
  }

  @Override
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

  @Override
  public boolean isSortActive() {
    return getHeaderCell().isSortActive();
  }

  @Override
  public boolean isSortAscending() {
    return getHeaderCell().isSortAscending();
  }

  @Override
  public boolean isSortPermanent() {
    return getHeaderCell().isSortPermanent();
  }

  @Override
  public boolean isGroupingActive() {
    return getHeaderCell().isGroupingActive();
  }

  @Override
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

  @Override
  public boolean isColumnFilterActive() {
    ITable table = getTable();
    if (table != null) {
      TableUserFilterManager m = table.getUserFilterManager();
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
  @Override
  @SuppressWarnings("unchecked")
  public int compareTableRows(ITableRow r1, ITableRow r2) {
    int c;
    VALUE o1 = getValue(r1);
    VALUE o2 = getValue(r2);
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

  /**
   * Refresh all column values to trigger re-validate and re-format
   */
  public void refreshValues() {
    if (isInitialized()) {
      if (getTable() != null) {
        List<ITableRow> rows = getTable().getRows();
        for (ITableRow row : rows) {
          setValue(row, getValue(row));
        }
        updateDisplayTexts();
        decorateCells();
      }
    }
  }

  /**
   * Used for mapping an {@link AbstractTableRowData} to an {@link ITableRow} by {@link TableRowDataMapper}
   * <p>
   * Do not use this method for normal value setting! Use {@link IColumn#setValue(ITableRow, Object)} instead.
   * <p>
   * The default implementation writes the given value directly into the corresponding cell.
   */
  @Override
  public void importValue(ITableRow row, Object value) {
    row.getCellForUpdate(this).setValue(value);
  }

  /**
   * Parses values in table row and sets it to the table row
   */
  public final void parseValueAndSet(ITableRow row, Object rawValue, boolean updateDisplayText) {
    try {
      setValue(row, interceptParseValue(row, rawValue), updateDisplayText);
    }
    catch (ProcessingException e) {
      row.getCellForUpdate(this).addErrorStatus(new ParsingFailedStatus(e, StringUtility.emptyIfNull(rawValue)));
    }
  }

  /**
   * Parses values in table row and sets it to the table row
   */
  @Override
  public final void parseValueAndSet(ITableRow row, Object rawValue) {
    try {
      setValue(row, interceptParseValue(row, rawValue));
    }
    catch (ProcessingException e) {
      row.getCellForUpdate(this).addErrorStatus(new ParsingFailedStatus(e, StringUtility.emptyIfNull(rawValue)));
    }
  }

  @Override
  public final VALUE/* validValue */ parseValue(ITableRow row, Object rawValue) {
    VALUE parsedValue = interceptParseValue(row, rawValue);
    return validateValue(row, parsedValue);
  }

  /**
   * do not use or override this internal method<br>
   * subclasses perform specific value validations here and set the default textual representation of the value
   */
  protected VALUE/* validValue */ parseValueInternal(ITableRow row, Object rawValue) {
    return TypeCastUtility.castValue(rawValue, getDataType());
  }

  @Override
  public VALUE/* validValue */ validateValue(ITableRow row, VALUE rawValue) {
    VALUE vinternal = validateValueInternal(row, rawValue);
    VALUE validatedValue = interceptValidateValue(row, vinternal);
    return validatedValue;
  }

  /**
   * do not use or override this internal method<br>
   * subclasses perform specific value validations here and set the default textual representation of the value
   */
  protected VALUE/* validValue */ validateValueInternal(ITableRow row, VALUE rawValue) {
    return rawValue;
  }

  @Override
  public final IFormField prepareEdit(ITableRow row) {
    ITable table = getTable();
    if (table == null || !this.isCellEditable(row)) {
      return null;
    }
    IFormField f = interceptPrepareEdit(row);
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
  protected IFormField prepareEditInternal(ITableRow row) {
    IValueField<VALUE> f = getDefaultEditor();
    mapEditorFieldProperties(f);
    return f;
  }

  protected void mapEditorFieldProperties(IFormField f) {
    Assertions.assertNotNull(f);
    f.setBackgroundColor(getBackgroundColor());
    f.setForegroundColor(getForegroundColor());
    f.setFont(getFont());
    f.setMandatory(isMandatory());
    mapGridDataToField(f);
  }

  /**
   * apply horizontal alignment of column to respective editor field
   */
  protected void mapGridDataToField(IFormField f) {
    GridData gd = f.getGridDataHints();
    gd.horizontalAlignment = getHorizontalAlignment();
    f.setGridDataHints(gd);
  }

  /**
   * @return a default editor for this column (the same for all rows).
   */
  protected final IValueField<VALUE> getDefaultEditor() {
    return createDefaultEditor();
  }

  /**
   * @return a default editor independent of the current row. Should only be created once for performance reasons.
   */
  protected IValueField<VALUE> createDefaultEditor() {
    return new AbstractValueField<VALUE>() {
    };
  }

  /**
   * Complete editing of a cell
   * <p>
   * By default this calls {@link #setValue(ITableRow, Object)} and delegates to
   * {@link #interceptParseValue(ITableRow, Object)} and {@link #interceptValidateValue(ITableRow, Object)}.
   */
  @Override
  public final void completeEdit(ITableRow row, IFormField editingField) {
    ITable table = getTable();
    if (table == null || !table.isCellEditable(row, this)) {
      return;
    }
    interceptCompleteEdit(row, editingField);
  }

  /**
   * Decorate all cells.
   */
  @Internal
  protected void decorateCells() {
    if (getTable() != null) {
      decorateCells(getTable().getRows());
    }
  }

  @Internal
  @Override
  public void decorateCells(List<ITableRow> rows) {
    for (ITableRow row : rows) {
      decorateCell(row);
    }
  }

  @Override
  public void decorateCell(ITableRow row) {
    Cell cell = row.getCellForUpdate(getColumnIndex());
    decorateCellInternal(cell, row);
    try {
      interceptDecorateCell(cell, row);
    }
    catch (RuntimeException e) {
      LOG.warn("Exception decorating cell", e);
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void decorateCellInternal(Cell cell, ITableRow row) {
  }

  /**
   * Update all display texts
   */
  protected void updateDisplayTexts() {
    if (getTable() != null) {
      updateDisplayTexts(getTable().getRows());
    }
  }

  @Override
  public void updateDisplayTexts(List<ITableRow> rows) {
    for (ITableRow row : Assertions.assertNotNull(rows)) {
      updateDisplayText(row, row.getCellForUpdate(this));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void updateDisplayText(ITableRow row, Cell cell) {
    updateDisplayText(row, cell, (VALUE) cell.getValue());
  }

  public void updateDisplayText(ITableRow row, VALUE value) {
    Cell cell = row.getCellForUpdate(this);
    updateDisplayText(row, cell, value);
  }

  private void updateDisplayText(ITableRow row, Cell cell, VALUE value) {
    cell.setText(formatValueInternal(row, value));
  }

  /**
   * by default, there is not display text set on the column
   */
  protected String formatValueInternal(ITableRow row, VALUE value) {
    return null;
  }

  @Override
  public void decorateHeaderCell() {
    HeaderCell cell = m_headerCell;
    decorateHeaderCellInternal(cell);
    try {
      interceptDecorateHeaderCell(cell);

      if (getTable() != null && getTable().getColumnSet() != null) {
        getTable().getColumnSet().updateColumn(this);
      }
    }
    catch (Exception e) {
      LOG.warn("Error decorating header", e);
    }
  }

  /**
   * do not use or override this internal method
   */
  protected void decorateHeaderCellInternal(HeaderCell cell) {
  }

  @Override
  public IHeaderCell getHeaderCell() {
    return m_headerCell;
  }

  @Override
  public int getVisibleColumnIndexHint() {
    return propertySupport.getPropertyInt(PROP_VIEW_COLUMN_INDEX_HINT);
  }

  @Override
  public void setVisibleColumnIndexHint(int index) {
    int oldIndex = getVisibleColumnIndexHint();
    if (oldIndex != index) {
      propertySupport.setPropertyInt(PROP_VIEW_COLUMN_INDEX_HINT, index);
    }
  }

  @Override
  public int getInitialWidth() {
    return m_initialWidth;
  }

  @Override
  public void setInitialWidth(int w) {
    m_initialWidth = w;
  }

  @Override
  public double getOrder() {
    return propertySupport.getPropertyDouble(PROP_VIEW_ORDER);
  }

  @Override
  public void setOrder(double order) {
    propertySupport.setPropertyDouble(PROP_VIEW_ORDER, order);
  }

  @Override
  public int getWidth() {
    return propertySupport.getPropertyInt(PROP_WIDTH);
  }

  @Override
  public void setWidth(int w) {
    propertySupport.setPropertyInt(PROP_WIDTH, w);
  }

  @Override
  public void setWidthInternal(int w) {
    propertySupport.setPropertyNoFire(PROP_WIDTH, w);
  }

  @Override
  public boolean isFixedWidth() {
    return propertySupport.getPropertyBool(PROP_FIXED_WIDTH);
  }

  @Override
  public void setFixedWidth(boolean fixedWidth) {
    propertySupport.setPropertyBool(PROP_FIXED_WIDTH, fixedWidth);
  }

  @Override
  public void setHorizontalAlignment(int hAglin) {
    boolean changed = propertySupport.setPropertyInt(PROP_HORIZONTAL_ALIGNMENT, hAglin);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public int getHorizontalAlignment() {
    return propertySupport.getPropertyInt(PROP_HORIZONTAL_ALIGNMENT);
  }

  @Override
  public boolean isDisplayable() {
    return propertySupport.getPropertyBool(PROP_DISPLAYABLE);
  }

  @Override
  public void setDisplayable(boolean b) {
    propertySupport.setPropertyBool(PROP_DISPLAYABLE, b);
    calculateVisible();
  }

  @Override
  public boolean isVisible() {
    return propertySupport.getPropertyBool(PROP_VISIBLE);
  }

  @Override
  public void setVisible(boolean b) {
    m_visibleProperty = b;
    calculateVisible();
  }

  private void calculateVisible() {
    propertySupport.setPropertyBool(PROP_VISIBLE, m_visibleGranted && isDisplayable() && m_visibleProperty);
  }

  @Override
  public boolean isVisibleInternal() {
    return m_visibleProperty;
  }

  @Override
  public boolean isPrimaryKey() {
    return m_primaryKey;
  }

  @Override
  public boolean isSummary() {
    return m_summary;
  }

  @Override
  public boolean isEditable() {
    return propertySupport.getPropertyBool(PROP_EDITABLE);
  }

  @Override
  public void setEditable(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_EDITABLE, b);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public boolean isCellEditable(ITableRow row) {
    return row.getCell(this).isEditable();
  }

  @Override
  public String getCssClass() {
    return (String) propertySupport.getProperty(PROP_CSS_CLASS);
  }

  @Override
  public void setCssClass(String cssClass) {
    boolean changed = propertySupport.setProperty(PROP_CSS_CLASS, cssClass);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public String getForegroundColor() {
    return (String) propertySupport.getProperty(PROP_FOREGROUND_COLOR);
  }

  @Override
  public void setForegroundColor(String c) {
    boolean changed = propertySupport.setProperty(PROP_FOREGROUND_COLOR, c);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public String getBackgroundColor() {
    return (String) propertySupport.getProperty(PROP_BACKGROUND_COLOR);
  }

  @Override
  public void setBackgroundColor(String c) {
    boolean changed = propertySupport.setProperty(PROP_BACKGROUND_COLOR, c);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public FontSpec getFont() {
    return (FontSpec) propertySupport.getProperty(PROP_FONT);
  }

  @Override
  public void setFont(FontSpec f) {
    boolean changed = propertySupport.setProperty(PROP_FONT, f);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  /**
   * true: Whenever table content changes, automatically calculate optimized column width so that all column content is
   * displayed without cropping.
   * <p>
   * This may display a horizontal scroll bar on the table.
   */
  @Override
  public boolean isAutoOptimizeWidth() {
    return propertySupport.getPropertyBool(PROP_AUTO_OPTIMIZE_WIDTH);
  }

  @Override
  public void setAutoOptimizeWidth(boolean optimize) {
    propertySupport.setPropertyBool(PROP_AUTO_OPTIMIZE_WIDTH, optimize);
  }

  @Override
  public void setHtmlEnabled(boolean enabled) {
    boolean changed = propertySupport.setProperty(PROP_HTML_ENABLED, enabled);
    if (changed && isInitialized()) {
      reinitCells();
    }
  }

  @Override
  public boolean isHtmlEnabled() {
    return (boolean) propertySupport.getProperty(PROP_HTML_ENABLED);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getHeaderCell().getText() + " width=" + getWidth() + (isPrimaryKey() ? " primaryKey" : "") + (isSummary() ? " summary" : "") + " viewIndexHint=" + getVisibleColumnIndexHint() + "]";
  }

  /**
   * Ensure that displayable columns are visible, if there is an error
   */
  @Override
  public void ensureVisibileIfInvalid(ITableRow row) {
    ICell cell = row.getCell(this);
    if (!cell.isContentValid() && isDisplayable() && !isVisible()) {
      setVisible(true);
    }
  }

  /**
   * @return true if column content is valid, no error status is set on column and mandatory property is met.
   */
  @Override
  public boolean isContentValid(ITableRow row) {
    return Assertions.assertNotNull(row).getCell(this).isContentValid();
  }

  @Override
  public boolean isRemovable() {
    return getTable().getTableOrganizer().isColumnRemovable(this);
  }

  @Override
  public boolean isModifiable() {
    return getTable().getTableOrganizer().isColumnModifiable(this);
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalColumnExtension<VALUE, OWNER extends AbstractColumn<VALUE>> extends AbstractExtension<OWNER> implements IColumnExtension<VALUE, OWNER> {

    public LocalColumnExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execCompleteEdit(ColumnCompleteEditChain<VALUE> chain, ITableRow row, IFormField editingField) {
      getOwner().execCompleteEdit(row, editingField);
    }

    @Override
    public void execInitColumn(ColumnInitColumnChain<VALUE> chain) {
      getOwner().execInitColumn();
    }

    @Override
    public VALUE execParseValue(ColumnParseValueChain<VALUE> chain, ITableRow row, Object rawValue) {
      return getOwner().execParseValue(row, rawValue);
    }

    @Override
    public VALUE execValidateValue(ColumnValidateValueChain<VALUE> chain, ITableRow row, VALUE rawValue) {
      return getOwner().execValidateValue(row, rawValue);
    }

    @Override
    public IFormField execPrepareEdit(ColumnPrepareEditChain<VALUE> chain, ITableRow row) {
      return getOwner().execPrepareEdit(row);
    }

    @Override
    public void execDecorateHeaderCell(ColumnDecorateHeaderCellChain<VALUE> chain, HeaderCell cell) {
      getOwner().execDecorateHeaderCell(cell);
    }

    @Override
    public void execDecorateCell(ColumnDecorateCellChain<VALUE> chain, Cell cell, ITableRow row) {
      getOwner().execDecorateCell(cell, row);
    }

    @Override
    public void execDisposeColumn(ColumnDisposeColumnChain<VALUE> chain) {
      getOwner().execDisposeColumn();
    }

  }

  protected final void interceptCompleteEdit(ITableRow row, IFormField editingField) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnCompleteEditChain<VALUE> chain = new ColumnCompleteEditChain<VALUE>(extensions);
    chain.execCompleteEdit(row, editingField);
  }

  protected final void interceptInitColumn() {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnInitColumnChain<VALUE> chain = new ColumnInitColumnChain<VALUE>(extensions);
    chain.execInitColumn();
  }

  protected final VALUE interceptParseValue(ITableRow row, Object rawValue) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnParseValueChain<VALUE> chain = new ColumnParseValueChain<VALUE>(extensions);
    return chain.execParseValue(row, rawValue);
  }

  protected final VALUE interceptValidateValue(ITableRow row, VALUE rawValue) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnValidateValueChain<VALUE> chain = new ColumnValidateValueChain<VALUE>(extensions);
    return chain.execValidateValue(row, rawValue);
  }

  protected final IFormField interceptPrepareEdit(ITableRow row) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnPrepareEditChain<VALUE> chain = new ColumnPrepareEditChain<VALUE>(extensions);
    return chain.execPrepareEdit(row);
  }

  protected final void interceptDecorateHeaderCell(HeaderCell cell) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnDecorateHeaderCellChain<VALUE> chain = new ColumnDecorateHeaderCellChain<VALUE>(extensions);
    chain.execDecorateHeaderCell(cell);
  }

  protected final void interceptDecorateCell(Cell cell, ITableRow row) {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnDecorateCellChain<VALUE> chain = new ColumnDecorateCellChain<VALUE>(extensions);
    chain.execDecorateCell(cell, row);
  }

  protected final void interceptDisposeColumn() {
    List<? extends IColumnExtension<VALUE, ? extends AbstractColumn<VALUE>>> extensions = getAllExtensions();
    ColumnDisposeColumnChain<VALUE> chain = new ColumnDisposeColumnChain<VALUE>(extensions);
    chain.execDisposeColumn();
  }

}
