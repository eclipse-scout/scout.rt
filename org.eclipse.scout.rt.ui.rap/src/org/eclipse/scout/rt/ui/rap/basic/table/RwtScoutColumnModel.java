package org.eclipse.scout.rt.ui.rap.basic.table;

import java.util.Arrays;
import java.util.HashMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rwt.RWT;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.NumberUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.ui.rap.RwtIcons;
import org.eclipse.scout.rt.ui.rap.ext.table.TableEx;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class RwtScoutColumnModel extends ColumnLabelProvider {
  private static final long serialVersionUID = 1L;

  private static final int HTML_ROW_LINE_HIGHT = 19;
  private static final int NEWLINE_LINE_HIGHT = 15;

  private transient ListenerList listenerList = null;
  private final ITable m_scoutTable;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final IRwtScoutTableForPatch m_uiTable;
  private final TableColumnManager m_columnManager;
  private Image m_imgCheckboxFalse;
  private Image m_imgCheckboxTrue;
  private Color m_disabledForegroundColor;
  private boolean m_multiline;
  private double[][] m_newlines = null;
  private double[][] m_htmlTableRows = null;
  private int m_defaultRowHeight;

  public RwtScoutColumnModel(ITable scoutTable, IRwtScoutTableForPatch uiTable, TableColumnManager columnManager) {
    m_scoutTable = scoutTable;
    m_uiTable = uiTable;
    m_columnManager = columnManager;
    m_imgCheckboxTrue = getUiTable().getUiEnvironment().getIcon(RwtIcons.CheckboxYes);
    m_imgCheckboxFalse = getUiTable().getUiEnvironment().getIcon(RwtIcons.CheckboxNo);
    m_disabledForegroundColor = getUiTable().getUiEnvironment().getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    m_defaultRowHeight = UiDecorationExtensionPoint.getLookAndFeel().getTableRowHeight();
    rebuildCache();
  }

  protected ITable getScoutTable() {
    return m_scoutTable;
  }

  public IRwtScoutTableForPatch getUiTable() {
    return m_uiTable;
  }

  @Override
  public void update(ViewerCell cell) {
    ITableRow element = (ITableRow) cell.getElement();
    int columnIndex = cell.getColumnIndex();

    cell.setText(getColumnText(element, columnIndex));
    cell.setImage(getColumnImage(element, columnIndex));

    cell.setBackground(getBackground(element, columnIndex));
    cell.setForeground(getForeground(element, columnIndex));
    cell.setFont(getFont(element, columnIndex));
  }

  public String getColumnText(ITableRow element, int columnIndex) {
    ICell cell = getCell(element, columnIndex);
    if (cell == null) {
      return "";
    }

    String text = cell.getText();
    if (text == null) {
      text = "";
    }
    else if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
      text = getUiTable().getUiEnvironment().adaptHtmlCell(getUiTable(), text);
      text = getUiTable().getUiEnvironment().convertLinksWithLocalUrlsInHtmlCell(getUiTable(), text);
    }
    else {
      boolean multiline = false;
      if (text.indexOf("\n") >= 0) {
        multiline = getScoutTable().isMultilineText();
        if (!multiline) {
          text = StringUtility.replaceNewLines(text, " ");
        }
      }
      boolean markupEnabled = Boolean.TRUE.equals(getUiTable().getUiField().getData(RWT.MARKUP_ENABLED));
      if (markupEnabled || multiline) {
        text = HtmlTextUtility.transformPlainTextToHtml(text);
      }
    }

    IColumn<?> column = m_columnManager.getColumnByModelIndex(columnIndex - 1);
    if (getScoutTable().getRowHeightHint() < 0 && column.isVisible()) {
      updateTableRowHeight(text, element, columnIndex);
    }
    return text;
  }

  private void updateTableRowHeight(String cellText, ITableRow element, int columnIndex) {
    TableEx table = getUiTable().getUiField();
    if (HtmlTextUtility.isTextWithHtmlMarkup(cellText)) {
      m_htmlTableRows = updateRowArray(m_htmlTableRows, ((ITableRow) element).getRowIndex(), columnIndex - 1);
      int htmlTableRowRowHeight = calculateHtmlTableRowHeight(m_htmlTableRows, cellText, ((ITableRow) element).getRowIndex(), columnIndex - 1);
      if (table.getData(RWT.CUSTOM_ITEM_HEIGHT) == null
          || ((Integer) table.getData(RWT.CUSTOM_ITEM_HEIGHT)).compareTo(htmlTableRowRowHeight) != 0) {
        table.setData(RWT.CUSTOM_ITEM_HEIGHT, Double.valueOf(NumberUtility.max(getDefaultRowHeight(), htmlTableRowRowHeight)).intValue());
      }
    }
    else {
      m_newlines = updateRowArray(m_newlines, ((ITableRow) element).getRowIndex(), columnIndex - 1);
      int newLineRowHeight = calculateNewLineRowHeight(m_newlines, cellText, ((ITableRow) element).getRowIndex(), columnIndex - 1);
      if (table.getData(RWT.CUSTOM_ITEM_HEIGHT) == null
          || ((Integer) table.getData(RWT.CUSTOM_ITEM_HEIGHT)).compareTo(newLineRowHeight) != 0) {
        table.setData(RWT.CUSTOM_ITEM_HEIGHT, Double.valueOf(NumberUtility.max(getDefaultRowHeight(), newLineRowHeight)).intValue());
      }
    }
  }

  private double[][] updateRowArray(double[][] rowArray, int rowIndex, int columnIndex) {
    double[][] tempRowArray = rowArray;
    if (rowArray == null || rowArray.length <= rowIndex) {
      if (rowArray == null) {
        tempRowArray = new double[rowIndex + 1][columnIndex + 1];
      }
      else {
        tempRowArray = Arrays.copyOf(rowArray, rowIndex + 1);
      }
    }
    for (int i = 0; i < tempRowArray.length; i++) {
      if (tempRowArray[i] == null || tempRowArray[i].length <= columnIndex) {
        double[] tempColumnArray = null;
        if (tempRowArray[i] == null) {
          tempColumnArray = new double[columnIndex + 1];
        }
        else {
          tempColumnArray = Arrays.copyOf(tempRowArray[i], columnIndex + 1);
        }
        tempRowArray[i] = tempColumnArray;
      }

      for (int j = 0; j < tempRowArray[i].length; j++) {
        if (tempRowArray[i][j] == 0) {
          tempRowArray[i][j] = 1;
        }
      }
    }
    return tempRowArray;
  }

  private int calculateHtmlTableRowHeight(double[][] htmlTableRows, String text, int rowIndex, int columnIndex) {
    htmlTableRows[rowIndex][columnIndex] = HtmlTextUtility.countHtmlTableRows(text);
    int htmlTableRowHeight = calculateRowHeigtMedian(htmlTableRows, HTML_ROW_LINE_HIGHT);
    return htmlTableRowHeight;
  }

  private int calculateNewLineRowHeight(double[][] newlines, String text, int rowIndex, int columnIndex) {
    newlines[rowIndex][columnIndex] = HtmlTextUtility.countLineBreaks(text);
    int newLineRowHeight = calculateRowHeigtMedian(newlines, NEWLINE_LINE_HIGHT);
    return newLineRowHeight;
  }

  private int calculateRowHeigtMedian(double[][] rows, int lineHeight) {
    boolean hasMultilines = false;
    double[] columnMedians = new double[rows.length];
    for (int i = 0; i < rows.length; i++) {
      columnMedians[i] = NumberUtility.max(rows[i]);
      if (NumberUtility.max(rows[i]) > 1) {
        hasMultilines = true;
      }
    }
    double median = NumberUtility.median(columnMedians);
    if (hasMultilines && median < 2) {
      median = 2;
    }
    int newLineRowHeight = NumberUtility.toDouble(NumberUtility.round(median, 1.0)).intValue() * lineHeight;
    return newLineRowHeight;
  }

  protected int getDefaultRowHeight() {
    return m_defaultRowHeight;
  }

  public Image getColumnImage(ITableRow element, int columnIndex) {
    int[] columnOrder = getUiTable().getUiField().getColumnOrder();
    if (columnOrder.length <= 1) {
      return null;
    }
    IColumn col = m_columnManager.getColumnByModelIndex(columnIndex - 1);
    ICell cell = getCell(element, columnIndex);
    //checkbox
    Image checkBoxImage = null;
    if (columnOrder[1] == columnIndex && getUiTable().getScoutObject() != null && getUiTable().getScoutObject().isCheckable()) {
      if (element.isChecked()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    else if (col != null && cell != null && col.getDataType() == Boolean.class && (!(col instanceof ISmartColumn) || ((ISmartColumn) col).getLookupCall() == null)) {
      Boolean b = (Boolean) cell.getValue();
      if (b != null && b.booleanValue()) {
        checkBoxImage = m_imgCheckboxTrue;
      }
      else {
        checkBoxImage = m_imgCheckboxFalse;
      }
    }
    //deco
    String iconId = null;
    if (cell != null && cell.getIconId() != null) {
      iconId = cell.getIconId();
    }
    else if (columnOrder[1] == columnIndex) {
      iconId = element.getIconId();
    }
    Image decoImage = getUiTable().getUiEnvironment().getIcon(iconId);
    //merge
    if (checkBoxImage != null && decoImage != null) {
      //TODO rap/rwt: new GC(Image) is not possible since in rwt an image does not implement Drawable.
      return checkBoxImage;
    }
    if (checkBoxImage != null) {
      return checkBoxImage;
    }
    if (decoImage != null) {
      return decoImage;
    }
    return null;
  }

  public Color getBackground(ITableRow element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        return getUiTable().getUiEnvironment().getColor(cell.getBackgroundColor());
      }
    }
    return null;
  }

  public Color getForeground(ITableRow element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        Color col = getUiTable().getUiEnvironment().getColor(cell.getForegroundColor());
        if (col == null) {
          if (!element.isEnabled() || !cell.isEnabled()) {
            col = m_disabledForegroundColor;
          }
        }
        return col;
      }
    }
    return null;
  }

  public Font getFont(ITableRow element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        return getUiTable().getUiEnvironment().getFont(cell.getFont(), getUiTable().getUiField().getFont());
      }
    }
    return null;
  }

  @Override
  public String getToolTipText(Object element) {
    Display display = getUiTable().getUiEnvironment().getDisplay();
    Point cursorOnTable = display.map(null, getUiTable().getUiField(), display.getCursorLocation());
    ViewerCell uiCell = getUiTable().getUiTableViewer().getCell(cursorOnTable);
    String text = "";
    if (uiCell != null) {
      int columnIndex = uiCell.getColumnIndex();

      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        text = cell.getTooltipText();
        if (text == null) {
          text = cell.getText();
          if (HtmlTextUtility.isTextWithHtmlMarkup(text)) {
            //Tooltips don't support html -> convert to plain text
            text = HTMLUtility.getPlainText(text);
          }
          if (text == null || text.indexOf("\n") <= 0) {
            text = "";
          }
        }
      }
      text = StringUtility.wrapWord(text, 80);
    }
    return text;
  }

  public void consumeColumnModelEvent(RwtScoutTableEvent uiTableEvent) {
    rebuildCache();
  }

  protected ICell getCell(Object row, int colIndex) {
    IColumn<?> column = m_columnManager.getColumnByModelIndex(colIndex - 1);
    if (column != null) {
      if (m_cachedCells == null || m_cachedCells.get(row) == null) {
        rebuildCache();
      }
      return m_cachedCells.get(row).get(column);
    }
    else {
      return null;
    }
  }

  private void rebuildCache() {
    m_cachedCells = new HashMap<ITableRow, HashMap<IColumn<?>, ICell>>();
    if (getScoutTable() != null) {
      for (ITableRow scoutRow : getScoutTable().getRows()) {
        HashMap<IColumn<?>, ICell> cells = new HashMap<IColumn<?>, ICell>();
        for (IColumn<?> col : getScoutTable().getColumnSet().getVisibleColumns()) {
          cells.put(col, getScoutTable().getCell(scoutRow, col));
        }
        m_cachedCells.put(scoutRow, cells);
      }
    }
  }
}
