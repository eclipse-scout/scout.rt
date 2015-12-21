package org.eclipse.scout.rt.ui.html.json.table;

import org.eclipse.scout.rt.client.ui.basic.table.columns.ISmartColumn;
import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.eclipse.scout.rt.ui.html.json.table.userfilter.JsonTextColumnUserFilter;
import org.json.JSONObject;

public class JsonSmartColumn extends JsonColumn<ISmartColumn> {

  public JsonSmartColumn(ISmartColumn model) {
    super(model);
  }

  /**
   * Use same object-type as StringColumn for the UI String- and SmartColumns work the same way.
   */
  @Override
  public String getObjectType() {
    return JsonStringColumn.OBJECT_TYPE;
  }

  @Override
  protected ColumnUserFilterState createFilterStateFromJson(JSONObject json) {
    return new JsonTextColumnUserFilter(null).createFilterStateFromJson(getColumn(), json);
  }

}
