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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.columns;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.mobile.Activator;
import org.eclipse.scout.rt.client.mobile.Icons;
import org.eclipse.scout.rt.client.mobile.ui.basic.table.DrillDownStyleMap;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

/**
 * @since 3.9.0
 */
public class AbstractRowSummaryColumn extends AbstractStringColumn implements IRowSummaryColumn {
  private boolean m_initialized;

  private IColumn<?> m_cellHeaderColumn;
  private List<IColumn<?>> m_cellDetailColumns;
  private String m_htmlCellTemplate;
  private String m_htmlDrillDown;
  private String m_htmlDrillDownButton;

  public AbstractRowSummaryColumn() {
    try {
      m_htmlCellTemplate = initHtmlCellTemplate();
      m_htmlDrillDown = initHtmlDrillDown();
      m_htmlDrillDownButton = initHtmlDrillDownButton();
    }
    catch (ProcessingException e) {
      SERVICES.getService(IExceptionHandlerService.class).handleException(e);
    }
  }

  @Override
  protected void initConfig() {
    super.initConfig();

    setDefaultDrillDownStyle(getConfiguredDefaultDrillDownStyle());
  }

  protected String initHtmlCellTemplate() throws ProcessingException {
    try {
      return new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/html/MobileTableCellContent.html").openStream()), "iso-8859-1");
    }
    catch (Throwable t) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", t);
    }
  }

  protected String initHtmlDrillDown() throws ProcessingException {
    try {
      return new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/html/MobileTableDrillDown.html").openStream()), "iso-8859-1");
    }
    catch (Throwable t) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", t);
    }
  }

  protected String initHtmlDrillDownButton() throws ProcessingException {
    try {
      return new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/html/MobileTableDrillDownButton.html").openStream()), "iso-8859-1");
    }
    catch (Throwable t) {
      throw new ProcessingException("Exception while loading html cell template for mobile table", t);
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

  public String getConfiguredDefaultDrillDownStyle() {
    return null;
  }

  public boolean isInitialized() {
    return m_initialized;
  }

  public static boolean isDrillDownButtonUrl(URL url, String path, boolean local) {
    if (!local) {
      return false;
    }

    String query = url.getQuery();
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

  public void updateValue(ITableRow row, ITableRow modelRow, DrillDownStyleMap drillDownStyle) throws ProcessingException {
    setValue(row, computeContentColumnValue(modelRow, drillDownStyle));
  }

  public void updateValue(ITableRow row, ITableRow modelRow) throws ProcessingException {
    updateValue(row, modelRow, null);
  }

  /**
   * Analyzes the content of the table to find optimal columns for the displayed texts.
   */
  public void initializeDecorationConfiguration(ITable table, int maxCellDetailColumns) {
    m_cellHeaderColumn = null;
    m_cellDetailColumns = new ArrayList<IColumn<?>>(maxCellDetailColumns);

    for (IColumn<?> column : table.getColumnSet().getVisibleColumns()) {
      if (m_cellDetailColumns.size() >= maxCellDetailColumns) {
        break;
      }

      if (m_cellHeaderColumn == null && useColumnForCellHeader(table, column)) {
        m_cellHeaderColumn = column;
      }
      else if (useColumnForCellDetail(table, column)) {
        m_cellDetailColumns.add(column);
      }

    }

    m_initialized = true;
  }

  private boolean useColumnForCellHeader(ITable table, IColumn<?> column) {
    //Only use the given column if there are no summary columns defined
    if (table.getColumnSet().getSummaryColumns().length == 0) {
      return true;
    }

    return false;
  }

  private boolean useColumnForCellDetail(ITable table, IColumn<?> column) {
    boolean columnEmpty = true;
    int maxRowsToConsider = 10;

    for (int row = 0; row < Math.min(maxRowsToConsider, table.getRowCount()); row++) {
      ITableRow tableRow = table.getRow(row);
      final String columnDisplayText = column.getDisplayText(tableRow);
      if (StringUtility.hasText(columnDisplayText)) {
        columnEmpty = false;

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

  private String computeContentColumnValue(ITableRow row, DrillDownStyleMap drillDownStyles) throws ProcessingException {
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
      if (row.isChecked()) {
        iconId = Icons.CheckboxYes;
      }
      else {
        iconId = Icons.CheckboxNo;
      }
    }
    else {
      iconId = row.getIconId();
      if (iconId == null) {
        iconId = row.getCell(0).getIconId();
      }
    }

    if (iconId == null) {
      return "";
    }
    else {
      return "<img width=\"16\" height=\"16\" src=\"cid:" + iconId + "\"/>";
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
    for (IColumn column : m_cellDetailColumns) {
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
   * Replace spaces with non breaking spaces.
   * Can't use &nbsp; because of https://bugs.eclipse.org/bugs/show_bug.cgi?id=379088
   */
  private String replaceSpaces(String text) {
    return text.replaceAll("\\s", "&#160;");
  }

  private String extractCellDisplayText(IColumn column, ITableRow row) {
    String displayText = column.getDisplayText(row);

    displayText = cleanupText(displayText);

    return displayText;
  }

  private String extractColumnHeader(IColumn column) {
    String header = column.getHeaderCell().getText();

    header = cleanupText(header);

    return header;
  }

  /**
   * Columns with a reasonable text don't need the header description.
   */
  private boolean isHeaderDescriptionNeeded(ITableRow row, IColumn column) {
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
