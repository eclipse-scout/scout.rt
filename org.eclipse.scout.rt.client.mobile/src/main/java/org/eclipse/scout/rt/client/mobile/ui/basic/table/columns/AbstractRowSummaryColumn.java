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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.columns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.DrillDownStyleMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IBooleanColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.html.HTMLUtility;
import org.eclipse.scout.rt.platform.util.BooleanUtility;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * @since 3.9.0
 */
@ClassId("349c912d-0e60-42a0-bd8d-b4c6c08ec62a")
public abstract class AbstractRowSummaryColumn extends AbstractStringColumn implements IRowSummaryColumn {
  private boolean m_decorationConfigurationInitialized;

  private IColumn<?> m_cellHeaderColumn;
  private List<IColumn<?>> m_cellDetailColumns;
  private String m_htmlCellTemplate;
  private String m_htmlDrillDown;
  private String m_htmlDrillDownButton;
  private int m_maxCellDetailColumns;

  public AbstractRowSummaryColumn() {
    try {
      m_htmlCellTemplate = initHtmlCellTemplate();
      m_htmlDrillDown = initHtmlDrillDown();
      m_htmlDrillDownButton = initHtmlDrillDownButton();
    }
    catch (RuntimeException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setDefaultDrillDownStyle(getConfiguredDefaultDrillDownStyle());
  }

  protected String initHtmlCellTemplate() {
    try {
      return new String(IOUtility.getContent(getClass().getResource("org/eclipse/scout/rt/client/mobile/html/MobileTableCellContent.html").openStream()), "iso-8859-1");
    }
    catch (IOException e) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", e);
    }
  }

  protected String initHtmlDrillDown() {
    try {
      return new String(IOUtility.getContent(getClass().getResource("org/eclipse/scout/rt/client/mobile/html/MobileTableDrillDown.html").openStream()), "iso-8859-1");
    }
    catch (IOException e) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", e);
    }
  }

  protected String initHtmlDrillDownButton() {
    try {
      return new String(IOUtility.getContent(getClass().getResource("org/eclipse/scout/rt/client/mobile/html/MobileTableDrillDownButton.html").openStream()), "iso-8859-1");
    }
    catch (IOException e) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", e);
    }
  }

  @Override
  public String getDefaultDrillDownStyle() {
    return propertySupport.getPropertyString(PROP_DEFAULT_DRILL_DOWN_STYLE);
  }

  @Override
  public void setDefaultDrillDownStyle(String drillDownStyle) {
    propertySupport.setPropertyString(PROP_DEFAULT_DRILL_DOWN_STYLE, drillDownStyle);
  }

  protected String getConfiguredDefaultDrillDownStyle() {
    return null;
  }

  public boolean isDecorationConfigurationInitialized() {
    return m_decorationConfigurationInitialized;
  }

  public static boolean isDrillDownButtonUrl(String ref) {
    String query = ref;
    if (query == null) {
      return false;
    }

    for (String s : query.split("[\\?\\&]")) {
      Matcher m = Pattern.compile("action=drill_down").matcher(s);
      if (m.matches()) {
        return true;
      }
    }
    return false;
  }

  public void updateValue(ITableRow row, ITableRow modelRow, DrillDownStyleMap drillDownStyle) {
    setValue(row, computeContentColumnValue(modelRow, drillDownStyle));
  }

  public void updateValue(ITableRow row, ITableRow modelRow) {
    updateValue(row, modelRow, null);
  }

  /**
   * Analyzes the content of the table to find optimal columns for the displayed texts.
   */
  public void initializeDecorationConfiguration(ITable table, int maxCellDetailColumns) {
    m_cellHeaderColumn = null;
    m_cellDetailColumns = new ArrayList<IColumn<?>>(maxCellDetailColumns);
    m_maxCellDetailColumns = maxCellDetailColumns;

    int columnVisibleIndex = 0;
    for (IColumn<?> column : table.getColumnSet().getVisibleColumns()) {
      if (m_cellDetailColumns.size() >= maxCellDetailColumns) {
        break;
      }

      if (m_cellHeaderColumn == null && useColumnForCellHeader(table, column)) {
        m_cellHeaderColumn = column;
      }
      else if (useColumnForCellDetail(table, column, columnVisibleIndex)) {
        m_cellDetailColumns.add(column);
      }
      columnVisibleIndex++;
    }

    m_decorationConfigurationInitialized = true;
  }

  private boolean useColumnForCellHeader(ITable table, IColumn<?> column) {
    if (isCheckBoxColumn(column)) {
      return false;
    }
    //Only use the given column if there are no summary columns defined
    if (table.getColumnSet().getSummaryColumns().size() == 0) {
      return true;
    }

    return false;
  }

  private boolean useColumnForCellDetail(ITable table, IColumn<?> column, int columnVisibleIndex) {
    boolean columnEmpty = true;
    int maxRowsToConsider = 10;

    if (isCheckBoxColumn(column)) {
      return false;
    }

    //Always use column if there is enough space left in m_cellDetailColumns for every remaining column
    int freeSlots = m_maxCellDetailColumns - m_cellDetailColumns.size();
    int remainingColumns = table.getColumnSet().getVisibleColumns().size() - (columnVisibleIndex + 1);
    if (remainingColumns < freeSlots) {
      return true;
    }

    for (int row = 0; row < Math.min(maxRowsToConsider, table.getRowCount()); row++) {
      ITableRow tableRow = table.getRow(row);
      final String columnDisplayText = column.getDisplayText(tableRow);
      if (StringUtility.hasText(columnDisplayText)) {
        columnEmpty = false;

        //If column contains similar data to the header column, don't use it
        String cellHeaderText = getCellHeaderText(tableRow);
        if (cellHeaderText != null && cellHeaderText.contains(columnDisplayText)) {
          return false;
        }

      }

    }

    if (columnEmpty) {
      return false;
    }

    return true;
  }

  private String getCellHeaderText(ITableRow row) {
    if (m_cellHeaderColumn != null) {
      return m_cellHeaderColumn.getDisplayText(row);
    }
    else {
      return row.getTable().getSummaryCell(row).getText();
    }
  }

  private static final Pattern bodyPartPattern = Pattern.compile("(.*<body[^>]*>)(.*)(</body>.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern htmlPartPattern = Pattern.compile("(.*<html[^>]*>)(.*)(</html>.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  /**
   * Wraps the existing html with a div having a border on the bottom to visually separate the rows. This would actually
   * be the job of the GUI, but it seems not to be possible with the list widget of rap.
   */
  private String addGridLine(String existingHtml) {
    String borderDivStart = "<div style=\"width: 100%; height: 100%; border-bottom:1px solid #e1efec\">";
    String borderDivEnd = "</div>";
    String prePart = "";
    String mainPart = "";
    String postPart = "";

    Matcher m = bodyPartPattern.matcher(existingHtml);
    boolean found = m.find();
    if (!found) {
      m = htmlPartPattern.matcher(existingHtml);
      found = m.find();
    }
    if (found) {
      prePart = m.group(1);
      mainPart = m.group(2);
      postPart = m.group(3);
    }
    else {
      mainPart = existingHtml;
    }

    return prePart + borderDivStart + mainPart + borderDivEnd + postPart;
  }

  protected String adaptExistingHtmlInCellHeader(String cellHeaderHtml) {
    cellHeaderHtml = addGridLine(cellHeaderHtml);

    return cellHeaderHtml;
  }

  /**
   * @return true if the text starts with {@code <html>}, false if not. The check is case insensitive.
   */
  private boolean containsHtml(String text) {
    if (text == null || text.length() < 6) {
      return false;
    }
    if (text.charAt(0) == '<' && text.charAt(5) == '>') {
      String tag = text.substring(1, 5);
      return tag.equalsIgnoreCase("html");
    }

    return false;
  }

  private String computeContentColumnValue(ITableRow row, DrillDownStyleMap drillDownStyles) {
    if (row == null) {
      return null;
    }

    String cellHeaderText = getCellHeaderText(row);
    if (cellHeaderText == null) {
      cellHeaderText = "";
    }
    //Don't generate cell content if the only column contains html.
    //It is assumed that such a column is already optimized for mobile devices.
    if (m_cellDetailColumns.size() == 0 && containsHtml(cellHeaderText)) {
      cellHeaderText = adaptExistingHtmlInCellHeader(cellHeaderText);

      //Make sure drill down style is set to none
      if (drillDownStyles != null) {
        drillDownStyles.put(row, IRowSummaryColumn.DRILL_DOWN_STYLE_NONE);
      }
      return cellHeaderText;
    }

    String content = "";
    boolean cellHasHeader = false;
    if (StringUtility.hasText(cellHeaderText)) {
      content = createCellHeader(cellHeaderText);
      content += "<br/>";
      cellHasHeader = true;
    }

    content += createCellDetail(row, cellHasHeader);

    String drillDownStyle = null;
    if (drillDownStyles != null) {
      drillDownStyle = drillDownStyles.get(row);
    }
    String icon = createCellIcon(row);
    String output = m_htmlCellTemplate.replace("#ICON#", icon);
    output = output.replace("#ICON_COL_WIDTH#", createCellIconColWidth(row, icon));
    output = output.replace("#CONTENT#", content);
    output = output.replace("#DRILL_DOWN#", createCellDrillDown(row, drillDownStyle));
    output = output.replace("#DRILL_DOWN_COL_WIDTH#", createCellDrillDownColWidth(row, drillDownStyle));

    return output;
  }

  private String createCellIcon(ITableRow row) {
    if (row == null) {
      return "";
    }

    String iconId = null;
    if (row.getTable().isCheckable()) {
      iconId = computeCheckboxIconId(row.isChecked());
    }
    else {
      iconId = row.getIconId();
      IColumn<?> firstVisibleColumn = row.getTable().getColumnSet().getFirstVisibleColumn();
      if (iconId == null) {
        iconId = row.getCell(firstVisibleColumn).getIconId();
      }
      if (iconId == null) {
        if (isCheckBoxColumn(firstVisibleColumn)) {
          IBooleanColumn booleanColumn = (IBooleanColumn) firstVisibleColumn;
          iconId = computeCheckboxIconId(BooleanUtility.nvl(booleanColumn.getValue(row)));
        }
      }
    }

    if (iconId == null) {
      return "";
    }
    else {
      return "<img width=\"16\" height=\"16\" src=\"cid:" + iconId + "\"/>";
    }
  }

  /**
   * @return true if the column should display a checkbox icon rather than a text
   */
  private boolean isCheckBoxColumn(IColumn<?> column) {
    return column.getDataType() == Boolean.class && (!(column instanceof ISmartColumn) || ((ISmartColumn) column).getLookupCall() == null);
  }

  private String computeCheckboxIconId(boolean checked) {
    if (checked) {
      return Icons.CheckboxYes;
    }
    else {
      return Icons.CheckboxNo;
    }
  }

  private String createCellIconColWidth(ITableRow row, String icon) {
    if (StringUtility.hasText(icon)) {
      return "32";
    }
    else {
      //If there is no icon set a small width as left padding for the text.
      return "6";
    }
  }

  private String createCellDrillDown(ITableRow row, String drillDownStyle) {
    if (drillDownStyle == null) {
      drillDownStyle = getDefaultDrillDownStyle();
    }

    if (DRILL_DOWN_STYLE_ICON.equals(drillDownStyle)) {
      return m_htmlDrillDown;
    }
    else if (DRILL_DOWN_STYLE_BUTTON.equals(drillDownStyle)) {
      return m_htmlDrillDownButton;
    }
    else {
      return "";
    }
  }

  private String createCellDrillDownColWidth(ITableRow row, String drillDownStyle) {
    if (drillDownStyle == null) {
      drillDownStyle = getDefaultDrillDownStyle();
    }

    if (DRILL_DOWN_STYLE_ICON.equals(drillDownStyle)) {
      return "32";
    }
    else if (DRILL_DOWN_STYLE_BUTTON.equals(drillDownStyle)) {
      return "60";
    }
    else {
      return "0";
    }
  }

  private String createCellHeader(String cellHeaderText) {
    String content = "";

    content = cleanupText(cellHeaderText);
    content = "<b>" + content + "</b>";

    return content;
  }

  private String createCellDetail(ITableRow row, boolean cellHasHeader) {
    if (row == null) {
      return "";
    }

    String content = "";
    int col = 0;
    for (IColumn<?> column : m_cellDetailColumns) {
      String displayText = extractCellDisplayText(column, row);

      if (StringUtility.hasText(displayText)) {
        if (isHeaderDescriptionNeeded(row, column)) {
          content += extractColumnHeader(column);
          content += ": ";
        }
        content += displayText;
      }

      if (col < m_cellDetailColumns.size() - 1) {
        content += "<br/>";
      }

      col++;
    }

    if (cellHasHeader) {
      //Make the font a little smaller if there is a cell header
      content = "<span style=\"font-size:12px\">" + content + "</span>";
    }

    return content;
  }

  private String cleanupText(String text) {
    if (text == null) {
      return null;
    }

    boolean containsHtml = text.contains("<html>");
    if (containsHtml) {
      //Ignore every html code by removing all the tags to make sure it does not destroy the layout
      String textWithoutHtml = HTMLUtility.getPlainText(text);
      if (textWithoutHtml != null) {
        text = textWithoutHtml;
      }
    }
    text = StringUtility.removeNewLines(text);
    text = StringUtility.trim(text);
    if (!containsHtml) {
      //If the text is not surrounded by <html> the html must not be interpreted but displayed as it is including all the html tags.
      text = StringUtility.htmlEncode(text);
    }
    text = replaceSpaces(text);

    return text;
  }

  /**
   * Replace spaces with non breaking spaces. Can't use &nbsp; because of
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=379088
   */
  private String replaceSpaces(String text) {
    return text.replaceAll("\\s", "&#160;");
  }

  private String extractCellDisplayText(IColumn<?> column, ITableRow row) {
    String displayText = column.getDisplayText(row);

    displayText = cleanupText(displayText);

    return displayText;
  }

  private String extractColumnHeader(IColumn<?> column) {
    String header = column.getHeaderCell().getText();

    header = cleanupText(header);

    return header;
  }

  /**
   * Columns with a reasonable text don't need the header description.
   */
  private boolean isHeaderDescriptionNeeded(ITableRow row, IColumn<?> column) {
    if (column instanceof ISmartColumn<?>) {
      return column.getValue(row) instanceof Boolean;
    }

    if (column instanceof IStringColumn) {
      return isNumber(((IStringColumn) column).getValue(row));
    }

    return true;
  }

  private boolean isNumber(String value) {
    if (value == null) {
      return false;
    }

    try {
      Double.parseDouble(value);
    }
    catch (NumberFormatException e) {
      return false;
    }
    return true;
  }
}
