package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;

/**
 * @since 5.2
 */
public class TableOrganizer implements ITableOrganizer {

  private ITable m_table;

  public TableOrganizer(ITable table) {
    m_table = table;
  }

  @Override
  public boolean isColumnAddable() {
    return hasCustomizer() || hasInvisibleColumns();
  }

  private boolean hasInvisibleColumns() {
    ColumnSet columnSet = m_table.getColumnSet();
    return columnSet.getVisibleColumnCount() < columnSet.getDisplayableColumnCount();
  }

  @Override
  public boolean isColumnRemovable(IColumn column) {
    return true;
  }

  @Override
  public boolean isColumnModifiable(IColumn column) {
    return isCustom(column);
  }

  @Override
  public void addColumn() {
    if (hasCustomizer()) {
      getCustomizer().addColumn();
    }
    else if (hasInvisibleColumns()) {
      showInvisibleColumnsForm();
    }
  }

  private void showInvisibleColumnsForm() {
    IShowInvisibleColumnsForm form = new ShowInvisibleColumnsForm(m_table);
    form.startModify();
    form.waitFor();
  }

  @Override
  public void removeColumn(IColumn column) {
    if (isCustom(column)) {
      if (hasCustomizer()) {
        getCustomizer().removeColumn((ICustomColumn) column);
      }
    }
    else {
      if (column.isVisible()) {
        hideColumn(column);
      }
    }
  }

  private void hideColumn(IColumn column) {
    if (column.isColumnFilterActive()) { // FIXME AWE: (organize) don't remove filter (only do for custom columns)
      m_table.getUserFilterManager().removeFilterByKey(column);
    }
    ColumnSet columnSet = m_table.getColumnSet();
    List<IColumn<?>> visibleColumns = columnSet.getVisibleColumns();
    visibleColumns.remove(column);
    columnSet.setVisibleColumns(visibleColumns);
  }

  @Override
  public void modifyColumn(IColumn column) {
    if (hasCustomizer()) {
      getCustomizer().modifyColumn((ICustomColumn) column);
    }
  }

  private boolean isCustom(IColumn column) {
    return column instanceof ICustomColumn;
  }

  private boolean hasCustomizer() {
    return getCustomizer() != null;
  }

  private ITableCustomizer getCustomizer() {
    return m_table.getTableCustomizer();
  }

  // FIXME AWE: (organizer) also check permission
  // BEANS.get(IAccessControlService.class).checkPermission(new CreateCustomColumnPermission())) {
}
