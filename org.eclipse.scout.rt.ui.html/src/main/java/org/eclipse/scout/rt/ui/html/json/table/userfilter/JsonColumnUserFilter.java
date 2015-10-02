package org.eclipse.scout.rt.ui.html.json.table.userfilter;

import org.eclipse.scout.rt.client.ui.basic.table.userfilter.ColumnUserFilterState;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonColumnUserFilter<T extends ColumnUserFilterState> extends JsonTableUserFilter<T> {

  public JsonColumnUserFilter(T filter) {
    super(filter);
  }

  @Override
  public String getObjectType() {
    return "ColumnUserFilter";
  }

  @Override
  public boolean isValid() {
    return getJsonTable().getColumnId(getFilterState().getColumn()) != null;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("column", getJsonTable().getColumnId(getFilterState().getColumn()));
    json.put("selectedValues", new JSONArray(getFilterState().getSelectedValues()));
    return json;
  }

  @Override
  public String toString() {
    return getObjectType() + ", " + getFilterState().getColumn();
  }
}
