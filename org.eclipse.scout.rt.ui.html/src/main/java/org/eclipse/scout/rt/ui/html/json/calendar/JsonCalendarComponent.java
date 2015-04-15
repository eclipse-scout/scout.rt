package org.eclipse.scout.rt.ui.html.json.calendar;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.basic.cell.JsonCell;
import org.json.JSONObject;

public class JsonCalendarComponent<T extends CalendarComponent> extends AbstractJsonAdapter<T> {

  public JsonCalendarComponent(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CalendarComponent";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    JsonObjectUtility.putProperty(json, "item", new JsonCalendarItem(getModel().getItem()).toJson());
    JsonObjectUtility.putProperty(json, "cell", new JsonCell(getModel().getCell()).toJson());
    JsonObjectUtility.putProperty(json, "fromDate", new JsonDate(getModel().getFromDate()).asJsonString());
    JsonObjectUtility.putProperty(json, "toDate", new JsonDate(getModel().getToDate()).asJsonString());
    if (getModel().getCoveredDays() != null) {
      for (Date coveredDay : getModel().getCoveredDays()) {
        JsonObjectUtility.append(json, "coveredDays", new JsonDate(coveredDay).asJsonString());
      }
    }
    JsonObjectUtility.putProperty(json, "fullDay", getModel().isFullDay());
    JsonObjectUtility.putProperty(json, "draggable", getModel().getProvider().isMoveItemEnabled());
    return json;
  }

}
