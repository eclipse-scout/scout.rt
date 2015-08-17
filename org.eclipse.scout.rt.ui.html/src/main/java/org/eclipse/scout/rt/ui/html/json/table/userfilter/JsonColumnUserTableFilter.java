package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserTableFilter;
import org.json.JSONObject;

public class JsonColumnUserTableFilter<T extends ColumnUserTableFilter> extends JsonUserTableFilter<T> {

  public JsonColumnUserTableFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "ColumnUserTableFilter";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("column", getJsonTable().getColumnId(getFilter().getColumn()));
    json.put("selectedValues", getFilter().getSelectedValues());
    return json;
  }
}
