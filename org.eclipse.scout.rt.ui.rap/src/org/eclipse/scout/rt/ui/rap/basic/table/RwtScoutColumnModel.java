package org.eclipse.scout.rt.ui.rap.basic.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.HTMLUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.basic.cell.ICell;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IProposalColumn;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IStringColumn;
import org.eclipse.scout.rt.shared.AbstractIcons;
import org.eclipse.scout.rt.ui.rap.IRwtEnvironment;
import org.eclipse.scout.rt.ui.rap.RwtIcons;
import org.eclipse.scout.rt.ui.rap.basic.AbstractRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutCellTextHelper;
import org.eclipse.scout.rt.ui.rap.basic.IRwtScoutComposite;
import org.eclipse.scout.rt.ui.rap.extension.UiDecorationExtensionPoint;
import org.eclipse.scout.rt.ui.rap.util.HtmlTextUtility;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

public class RwtScoutColumnModel extends ColumnLabelProvider {

  public static final String EDITABLE_VARIANT_PREFIX = "EDITABLE_CELL_VARIANT_";
  public static final String EDITABLE_VARIANT_CELL_MARKER = "EDITABLE";

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(RwtScoutColumnModel.class);
  private static final long serialVersionUID = 1L;

  private final ITable m_scoutTable;
  private final RwtScoutTable m_uiTable;
  private final TableColumnManager m_columnManager;
  private final Image m_imgCheckboxFalse;
  private final Image m_imgCheckboxTrue;
  private final Color m_disabledForegroundColor;

  private volatile Map<ITableRow, Map<IColumn<?>, P_CachedCell>> m_cachedCells;
  private final IRwtEnvironment m_env;

  public RwtScoutColumnModel(ITable scoutTable, RwtScoutTable uiTable, TableColumnManager columnManager) {
    m_scoutTable = scoutTable;
    m_uiTable = uiTable;
    m_columnManager = columnManager;
    m_env = getUiTable().getUiEnvironment();
    m_imgCheckboxTrue = m_env.getIcon(RwtIcons.CheckboxYes);
    m_imgCheckboxFalse = m_env.getIcon(RwtIcons.CheckboxNo);
    m_disabledForegroundColor = m_env.getColor(UiDecorationExtensionPoint.getLookAndFeel().getColorForegroundDisabled());
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
    final ICell cell = getCell(element, columnIndex);
    final IColumn<?> column = m_columnManager.getColumnByModelIndex(columnIndex - 1);
    final IRwtScoutCellTextHelper cellTextHelper = createCellTextHelper(m_env, getUiTable(), column);

    return cellTextHelper.processCellText(cell);
  }

  protected IRwtScoutCellTextHelper createCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite, IColumn<?> column) {
    return new P_RwtScoutColumnModelCellTextHelper(env, uiComposite, column);
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
    Image decoImage = m_env.getIcon(iconId);
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
        return m_env.getColor(cell.getBackgroundColor());
      }
    }
    return null;
  }

  public Color getForeground(ITableRow element, int columnIndex) {
    if (columnIndex > 0) {
      ICell cell = getCell(element, columnIndex);
      if (cell != null) {
        Color col = m_env.getColor(cell.getForegroundColor());
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
        return m_env.getFont(cell.getFont(), getUiTable().getUiField().getFont());
      }
    }
    return null;
  }

  @Override
  public String getToolTipText(Object element) {
    Display display = m_env.getDisplay();
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
            //Tooltips with HTML are supported since RAP 2.2, see org.eclipse.rap.rwt.RWT.TOOLTIP_MARKUP_ENABLED
            //However, since this property is not set to true in Scout, HTML encoding is currently not necessary
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
      Map<ITableRow, Map<IColumn<?>, P_CachedCell>> currentCache = m_cachedCells;
      if (currentCache == null || currentCache.get(row) == null) {
        rebuildCache();
      }

      P_CachedCell cachedCell = m_cachedCells.get(row).get(column);
      if (cachedCell != null) {
        return cachedCell.m_cell;
      }
    }
    return null;
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

    Map<IColumn<?>, P_CachedCell> cachedRow = m_cachedCells.get(row);
    int[] visibleColumnIndexes = getScoutTable().getColumnSet().getVisibleColumnIndexes();
    boolean isEditable = false;
    for (int i = 0; i < visibleColumnIndexes.length; i++) {
      if (i > 0) {
        builder.append('_');
      }

      IColumn<?> col = getScoutTable().getColumnSet().getColumn(visibleColumnIndexes[i]);
      if (col != null && cachedRow != null) {
        P_CachedCell cachedCell = cachedRow.get(col);
        if (cachedCell != null && cachedCell.m_isEditable) {
          builder.append(EDITABLE_VARIANT_CELL_MARKER);
          isEditable = true;
        }
      }
    }

    if (isEditable) {
      builder.insert(0, EDITABLE_VARIANT_PREFIX);
      return builder.toString();
    }

    return null;
  }

  /**
   * Determines whether the visual marker for a cell is to be displayed.
   *
   * @return <code>true</code> if the cell is editable and not of the type {@link Boolean}.
   */
  protected boolean isEditableIconNeededInScoutThread(final ITableRow row, final IColumn<?> column) {
    if (column == null || row == null) {
      return false;
    }

    if (column.getDataType().isAssignableFrom(Boolean.class)) {
      return false;
    }

    ITable scoutTable = getScoutTable();
    if (scoutTable == null) {
      return false;
    }
    return scoutTable.isCellEditable(row, column);
  }

  private void rebuildCache() {
    if (getScoutTable() == null) {
      m_cachedCells = CollectionUtility.emptyHashMap();
      return;
    }

    final Holder<Map<ITableRow, Map<IColumn<?>, P_CachedCell>>> cacheResultHolder = new Holder<Map<ITableRow, Map<IColumn<?>, P_CachedCell>>>();
    Runnable r = new Runnable() {
      @Override
      public void run() {
        List<ITableRow> rows = getScoutTable().getRows();
        Map<ITableRow, Map<IColumn<?>, P_CachedCell>> tmp = new HashMap<ITableRow, Map<IColumn<?>, P_CachedCell>>(rows.size());
        for (ITableRow scoutRow : rows) {
          List<IColumn<?>> visibleColumns = getScoutTable().getColumnSet().getVisibleColumns();
          Map<IColumn<?>, P_CachedCell> cells = new HashMap<IColumn<?>, P_CachedCell>(visibleColumns.size());
          for (IColumn<?> col : visibleColumns) {
            ICell cell = getScoutTable().getCell(scoutRow, col);
            boolean isCellEditable = isEditableIconNeededInScoutThread(scoutRow, col);
            cells.put(col, new P_CachedCell(cell, isCellEditable));
          }
          tmp.put(scoutRow, cells);
        }
        cacheResultHolder.setValue(tmp);
      }
    };

    try {
      getUiTable().getUiEnvironment().invokeScoutLater(r, -1).join();
      m_cachedCells = cacheResultHolder.getValue();
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for the model.", e);
    }
  }

  private static final class P_CachedCell {
    private final ICell m_cell;
    private final boolean m_isEditable;

    private P_CachedCell(ICell cell, boolean isEditable) {
      m_cell = cell;
      m_isEditable = isEditable;
    }
  }

  private class P_RwtScoutColumnModelCellTextHelper extends AbstractRwtScoutCellTextHelper {

    private final IColumn<?> m_column;

    public P_RwtScoutColumnModelCellTextHelper(IRwtEnvironment env, IRwtScoutComposite<?> uiComposite, IColumn<?> column) {
      super(env, uiComposite);
      m_column = column;
    }

    @Override
    protected Map<String, String> createAdditionalLinkParams() {
      return null;
    }

    @Override
    protected boolean isMultilineScoutObject() {
      return getScoutTable().isMultilineText();
    }

    @Override
    protected boolean isWrapText() {
      if (m_column instanceof IStringColumn && isMultilineScoutObject()) {
        return ((IStringColumn) m_column).isTextWrap();
      }
      return false;
    }
  }
}
