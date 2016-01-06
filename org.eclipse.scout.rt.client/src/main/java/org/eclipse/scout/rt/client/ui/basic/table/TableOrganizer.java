package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ICustomColumn;
import org.eclipse.scout.rt.client.ui.basic.table.customizer.ITableCustomizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.IShowInvisibleColumnsForm;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ITableOrganizer;
import org.eclipse.scout.rt.client.ui.basic.table.organizer.ShowInvisibleColumnsForm;

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
    return isCustomizable() || hasInvisibleColumns();
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
    if (isCustomizable()) {
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
      if (isCustomizable()) {
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
    ColumnSet columnSet = m_table.getColumnSet();
    List<IColumn<?>> visibleColumns = columnSet.getVisibleColumns();
    visibleColumns.remove(column);
    columnSet.setVisibleColumns(visibleColumns);
  }

  @Override
  public void modifyColumn(IColumn column) {
    if (isCustomizable()) {
      getCustomizer().modifyColumn((ICustomColumn) column);
    }
  }

  private boolean isCustom(IColumn column) {
    return column instanceof ICustomColumn;
  }

  private boolean isCustomizable() {
    return m_table.isCustomizable();
  }

  private ITableCustomizer getCustomizer() {
    return m_table.getTableCustomizer();
  }

}
