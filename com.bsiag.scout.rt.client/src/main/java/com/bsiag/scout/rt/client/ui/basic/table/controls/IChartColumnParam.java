package com.bsiag.scout.rt.client.ui.basic.table.controls;

public interface IChartColumnParam {
  public static final int AGGREGATION_COUNT = -1;
  public static final int AGGREGATION_SUM = 1;
  public static final int AGGREGATION_AVG = 2;

  int getColumnIndex();

  int getColumnModifier();

}
