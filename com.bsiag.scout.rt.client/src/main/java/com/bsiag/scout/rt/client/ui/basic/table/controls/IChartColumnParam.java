package com.bsiag.scout.rt.client.ui.basic.table.controls;

public interface IChartColumnParam {
  int AGGREGATION_COUNT = -1;
  int AGGREGATION_SUM = 1;
  int AGGREGATION_AVG = 2;

  int DATE_GROUP_YEAR = 256;
  int DATE_GROUP_MONTH = 257;
  int DATE_GROUP_WEEKDAY = 258;

  int getColumnIndex();

  int getColumnModifier();

}
