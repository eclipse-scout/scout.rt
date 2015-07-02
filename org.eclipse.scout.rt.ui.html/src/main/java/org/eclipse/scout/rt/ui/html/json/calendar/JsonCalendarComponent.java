package org.eclipse.scout.rt.ui.html.json.calendar;

import java.util.Date;

import org.eclipse.scout.rt.client.ui.basic.calendar.CalendarComponent;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.json.JSONObject;

public class JsonCalendarComponent<CALENDAR_COMPONENT extends CalendarComponent> extends AbstractJsonAdapter<CALENDAR_COMPONENT> {

  public JsonCalendarComponent(CALENDAR_COMPONENT model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CalendarComponent";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put("item", new JsonCalendarItem(getModel().getItem()).toJson());
    // FIXME AWE: (calendar) check if we can really remove the cell property. When we don't need it here
    // we should clean-up the Scout model and remove the cell from the model completely.
    // json.put("cell", new JsonCell(getModel().getCell()).toJson());
    json.put("fromDate", new JsonDate(getModel().getFromDate()).asJsonString());
    json.put("toDate", new JsonDate(getModel().getToDate()).asJsonString());
    if (getModel().getCoveredDays() != null) {
      for (Date coveredDay : getModel().getCoveredDays()) {
        json.append("coveredDays", new JsonDate(coveredDay).asJsonString());
      }
    }
    json.put("fullDay", getModel().isFullDay());
    json.put("draggable", getModel().getProvider().isMoveItemEnabled());
    return json;
  }

}
