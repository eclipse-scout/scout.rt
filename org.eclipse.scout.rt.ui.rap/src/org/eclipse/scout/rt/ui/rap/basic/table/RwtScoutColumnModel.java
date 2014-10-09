package org.eclipse.scout.rt.ui.rap.basic.table;

import java.util.HashMap;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IProposalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.rap.RwtIcons;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

public class RwtScoutColumnModel extends ColumnLabelProvider {
  private static final long serialVersionUID = 1L;

  private static final int HTML_ROW_LINE_HIGHT = 19;
  private static final int NEWLINE_LINE_HIGHT = 15;

  private transient ListenerList listenerList = null;
  private final ITable m_scoutTable;
  private HashMap<ITableRow, HashMap<IColumn<?>, ICell>> m_cachedCells;
  private final RwtScoutTable m_uiTable;
  private final TableColumnManager m_columnManager;
  private Image m_imgCheckboxFalse;
  private Image m_imgCheckboxTrue;
  private Color m_disabledForegroundColor;
  private int m_defaultRowHeight;

  public RwtScoutColumnModel(ITable scoutTable, RwtScoutTable uiTable, TableColumnManager columnManager) {
    m_scoutTable = scoutTable;
    m_uiTable = uiTable;
    m_columnManager = columnManager;
    m_imgCheckboxTrue = getUiTable().getUiEnvironment().getIcon(RwtIcons.CheckboxYes);
    m_imgCheckboxFalse = getUiTable().getUiEnvironment().getIcon(RwtIcons.CheckboxNo);
    m_disabledForegroundColor = getUiTable().getUiEnvironment().getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
    rebuildCache();
  }

  protected ITable getScoutTable() {
    return m_scoutTable;
  }

  public RwtScoutTable getUiTable() {
    return m_uiTable;
  }

  @Override
  public void update(ViewerCell cell) {
    ITableRow row = (ITableRow) cell.getElement();
    int columnIndex = cell.getColumnIndex();

    cell.setText(getColumnText(row, columnIndex));
    cell.setImage(getColumnImage(row, columnIndex));
    cell.setBackground(getBackground(row, columnIndex));
    cell.setForeground(getForeground(row, columnIndex));
    cell.setFont(getFont(row, columnIndex));

    // Encode the information of which cell is editable into the custom variant of the 'TableItem' to display a visual marker for editable cells.
    // This is a workaround because there is no actual RAP support to set an individual variant on table-cells (the CSS-element 'Table-Cell' applies to all cells of the table).
    // The marker itself is added in the JavaScript patch 'EditableCellMarkerPatch.js' which patches 'GridRow.js'.
    if (cell.getColumnIndex() == 1) {
      cell.getItem().setData(RWT.CUSTOM_VARIANT, createTableRowVariant(row)); // A 'TableItem' represents not a single cell but the whole row instead. Therefore, the encoding is only done for the first cell being updated.
    }
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
      text = getUiTable().getUiEnvironment().convertLinksInHtmlCell(getUiTable(), text);
    }
    else {
      boolean multiline = isMultiline(text);
      if (!multiline) {
        text = replaceLineBreaksInMultilineText(text);
      }
      boolean isMultilineTable = getScoutTable().isMultilineText();
      boolean markupEnabled = Boolean.TRUE.equals(getUiTable().getUiField().getData(RWT.MARKUP_ENABLED));

      if (markupEnabled || multiline) {
        boolean replaceBreakableChars = true;
        IColumn<?> column = m_columnManager.getColumnByModelIndex(columnIndex - 1);
        if (column instanceof IStringColumn && isMultilineTable) {
          IStringColumn stringColumn = (IStringColumn) column;
          replaceBreakableChars = !stringColumn.isTextWrap();
        }
        text = HtmlTextUtility.transformPlainTextToHtml(text, replaceBreakableChars);
      }
    }
    return text;
  }

  private boolean isMultiline(String text) {
    return isMultilineText(text) && getScoutTable().isMultilineText();
  }

  private boolean isMultilineText(String text) {
    return text.indexOf("\n") >= 0;
  }

  private String replaceLineBreaksInMultilineText(String text) {
    if (isMultilineText(text)) {
      text = StringUtility.replaceNewLines(text, " ");
    }
    return text;
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
    else if (col != null && cell != null && col.getDataType() == Boolean.class && (!(col instanceof IProposalColumn) || ((IProposalColumn) col).getLookupCall() == null)) {
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
    if (cell != null && cell.getErrorStatus() != null && cell.getErrorStatus().getSeverity() == IStatus.ERROR) {
      iconId = AbstractIcons.StatusError;
    }
    else if (cell != null && cell.getIconId() != null) {
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

  /**
   * Creates a custom variant to be set as {@link TableItem}'s data to identify editable cells of that row. This
   * identifier is used in <code>GridRow.js</code> to install a visual marker for editable cells.<br/>
   * Format: the cells are delimited with a '_' and editable cells are marked with the literal 'EDITABLE'.
   *
   * @param row
   *          the current row.
   * @return the variant for that row.
   */
  protected String createTableRowVariant(ITableRow row) {
    StringBuilder builder = new StringBuilder();
    for (int column = 0; column < row.getCellCount(); column++) {
      if (column > 0) {
        builder.append("_");
      }

      if (isEditableIconNeeded(row, m_columnManager.getColumnByModelIndex(column))) {
        builder.append("EDITABLE");
      }
    }

    String variant = builder.toString();
    if (variant.contains("EDITABLE")) {
      return "EDITABLE_CELL_VARIANT_" + variant;
    }
    else {
      return null;
    }
  }

  /**
   * Determines whether the visual marker for a cell is to be displayed.
   *
   * @return <code>true</code> if the cell is editable and not of the type {@link Boolean}.
   */
  protected boolean isEditableIconNeeded(ITableRow row, IColumn<?> column) {
    return getScoutTable().isCellEditable(row, column) && !column.getDataType().isAssignableFrom(Boolean.class);
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
