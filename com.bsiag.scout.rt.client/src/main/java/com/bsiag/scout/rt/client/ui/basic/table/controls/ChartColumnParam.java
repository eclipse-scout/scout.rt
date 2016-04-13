package com.bsiag.scout.rt.client.ui.basic.table.controls;

/**
 * Specify chart column
 */
public class ChartColumnParam implements IChartColumnParam {
  private int m_columnIndex;
  private int m_columnModifier;

  public ChartColumnParam(int columnIndex, int columnModifier) {
    m_columnIndex = columnIndex;
    m_columnModifier = columnModifier;
  }

  @Override
  public int getColumnIndex() {
    return m_columnIndex;
  }

  @Override
  public int getColumnModifier() {
    return m_columnModifier;
  }

}
