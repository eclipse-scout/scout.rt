package com.bsiag.scout.rt.client.ui.basic.table.controls;

import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;

/**
 * Specify chart column
 */
public class ChartColumnParam implements IChartColumnParam {
  private final IColumn<?> m_column;
  private final int m_columnModifier;

  public ChartColumnParam(int columnModifier) {
    m_column = null;
    m_columnModifier = columnModifier;
  }

  /**
   * @param column
   *          may be <code>null</code>
   */
  public ChartColumnParam(IColumn<?> column, int columnModifier) {
    m_column = column;
    m_columnModifier = columnModifier;
  }

  @Override
  public int getColumnIndex() {
    if (getColumn() != null) {
      return getColumn().getColumnIndex();
    }
    return -1;
  }

  @Override
  public int getColumnModifier() {
    return m_columnModifier;
  }

  @Override
  public IColumn<?> getColumn() {
    return m_column;
  }

}
