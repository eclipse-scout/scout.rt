package org.eclipse.scout.rt.ui.html.json.desktop.bench.layout;

import org.eclipse.scout.rt.client.ui.desktop.bench.layout.BenchColumnData;
import org.eclipse.scout.rt.client.ui.desktop.bench.layout.FlexboxLayoutData;
import org.json.JSONObject;

/**
 * <h3>{@link JsonColumnData}</h3>
 *
 * @author Andreas Hoegger
 */
public class JsonColumnData extends JsonLayoutData {

  /**
   * @param layoutData
   */
  public JsonColumnData(BenchColumnData layoutData) {
    super(layoutData);
  }

  @Override
  public BenchColumnData getLayoutData() {
    return (BenchColumnData) super.getLayoutData();
  }

  @Override
  public JSONObject toJson() {
    if (getLayoutData() == null) {
      return null;
    }
    JSONObject json = super.toJson();
    for (FlexboxLayoutData rowData : getLayoutData().getRows()) {
      json.append("rows", JsonLayoutData.toJson(rowData));
    }
    return json;
  }

  public static JSONObject toJson(BenchColumnData layoutData) {
    if (layoutData == null) {
      return null;
    }
    return new JsonColumnData(layoutData).toJson();
  }
}
