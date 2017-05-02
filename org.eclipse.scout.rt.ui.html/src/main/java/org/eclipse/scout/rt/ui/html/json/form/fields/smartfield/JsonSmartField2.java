package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.SmartField2Result;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField2<VALUE> extends JsonValueField<ISmartField2<VALUE>> {

  // Contains always the mapping from the last performed lookup operation
  // all values are reset each time a new lookup starts
  private Map<Object, Integer> m_keyToIdMap = new HashMap<>();
  private Map<Integer, Object> m_idToKeyMap = new HashMap<>();
  private int m_id = 0;

  public JsonSmartField2(ISmartField2<VALUE> model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(ISmartField2<VALUE> model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_RESULT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getResult();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return resultToJson((SmartField2Result<VALUE>) value);
      }
    });
  }

  @Override
  public String getObjectType() {
    return "SmartField2";
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("lookup".equals(event.getType())) {
      handleUiLookup(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiLookup(JsonEvent event) {
    resetKeyMap();
    String query = event.getData().optString("query");
    query = "*";
    String filterKey = event.getData().optString("filterKey");
    if (StringUtility.hasText(query)) {
      getModel().query(query, filterKey);
    }
  }

  protected void resetKeyMap() {
    m_keyToIdMap.clear();
    m_idToKeyMap.clear();
    m_id = 0;
  }

  protected void mapKey(Object key) {
    int id = m_id++;
    m_keyToIdMap.put(key, id); // TODO [awe] 7.0 - SF2: anstatt der map könnten wir auch einfach den index der lookupRow verwenden (und müssten dann eine list nehmen)
    m_idToKeyMap.put(id, key);
  }

  @SuppressWarnings("unchecked")
  protected JSONObject resultToJson(SmartField2Result result) {
    if (result == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    JSONArray lookupRows = new JSONArray();
    for (LookupRow<?> lookupRow : (Collection<LookupRow<?>>) result.getLookupRows()) {
      lookupRows.put(lookupRowToJson(lookupRow));
    }
    json.put("lookupRows", lookupRows);
    return json;
  }

  protected JSONObject lookupRowToJson(LookupRow<?> lookupRow) {
    mapKey(lookupRow.getKey());
    JSONObject json = new JSONObject();
    json.put("key", lookupRow.getKey());
    json.put("text", lookupRow.getText());
    return json;
  }

}
