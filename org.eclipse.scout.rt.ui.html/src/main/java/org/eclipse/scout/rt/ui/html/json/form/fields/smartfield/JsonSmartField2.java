package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.SmartField2Result;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
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
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_ACTIVE_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isActiveFilterEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_ACTIVE_FILTER, model) {
      @Override
      protected TriState modelValue() {
        return getModel().getActiveFilter();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_ACTIVE_FILTER_LABELS, model) {
      @Override
      protected String[] modelValue() { // FIXME [awe] 7.0 - SF2: convert to JsonArray?
        return getModel().getActiveFilterLabels();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JSONArray(value);
      }
    });
  }

  @Override
  public String getObjectType() {
    return "SmartField2";
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("lookupByText".equals(event.getType())) {
      handleUiLookupByText(event);
    }
    else if ("lookupAll".equals(event.getType())) {
      handleUiLookupAll();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  // FIXME [awe] 7.0 - SF2: use UI facade here?

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IValueField.PROP_VALUE.equals(propertyName)) {
      String mappedKey = data.optString("value");
      VALUE key = (VALUE) m_idToKeyMap.get(NumberUtility.parseInt(mappedKey));
      addPropertyEventFilterCondition(IValueField.PROP_VALUE, key);
      getModel().setValue(key);
    }
    else if (ISmartField2.PROP_ACTIVE_FILTER.equals(propertyName)) {
      String activeFilterString = data.optString("activeFilter", null);
      TriState activeFilter = TriState.valueOf(activeFilterString);
      addPropertyEventFilterCondition(ISmartField2.PROP_ACTIVE_FILTER, activeFilter);
      getModel().setActiveFilter(activeFilter);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleUiLookupByText(JsonEvent event) {
    resetKeyMap();
    String searchText = event.getData().optString("searchText");
    getModel().lookupByText(searchText);
  }

  /**
   * Why resolve current key and not resolve key with a parameter? Because it is not guaranteed that the key is
   * serializable / comparable. So we cannot simply send the key from the UI to the server. Additionally we do not have
   * a list of lookup rows as we have in lookupByText
   *
   * @param event
   */
  protected void handleUiLookupAll() {
    getModel().lookupAll();
  }

  protected void resetKeyMap() {
    m_keyToIdMap.clear();
    m_idToKeyMap.clear();
    m_id = 0;
  }

  protected int mapKey(Object key) {
    int id = m_id++;
    m_keyToIdMap.put(key, id); // TODO [awe] 7.0 - SF2: anstatt der map könnten wir auch einfach den index der lookupRow verwenden (und müssten dann eine list nehmen)
    m_idToKeyMap.put(id, key);
    return id;
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
    int mappedKey = mapKey(lookupRow.getKey());
    JSONObject json = new JSONObject();
    json.put("key", mappedKey);
    json.put("text", lookupRow.getText());
    if (StringUtility.hasText(lookupRow.getIconId())) {
      json.put("iconId", BinaryResourceUrlUtility.createIconUrl(lookupRow.getIconId()));
    }
    if (StringUtility.hasText(lookupRow.getTooltipText())) {
      json.put("tooltipText", lookupRow.getTooltipText());
    }
    if (StringUtility.hasText(lookupRow.getBackgroundColor())) {
      json.put("backgroundColor", lookupRow.getBackgroundColor());
    }
    if (StringUtility.hasText(lookupRow.getForegroundColor())) {
      json.put("foregroundColor", lookupRow.getForegroundColor());
    }
    if (lookupRow.getFont() != null) {
      json.put("font", lookupRow.getFont().toPattern());
    }
    if (!lookupRow.isEnabled()) {
      json.put("enabled", lookupRow.isEnabled());
    }
    if (lookupRow.getParentKey() != null) {
      json.put("parentKey", lookupRow.getParentKey()); // FIXME [awe] 7.0 - SF2: how to map the parentKey?
    }
    if (!lookupRow.isActive()) {
      json.put("active", lookupRow.isActive());
    }
    if (lookupRow.getAdditionalTableRowData() != null && lookupRow.getAdditionalTableRowData().getCustomValues() != null) { // FIXME [awe] 7.0 - SF2: create isEmpty() method on AdditionalTableRowData
      json.put("additionalTableRowData", lookupRow.getAdditionalTableRowData()); // FIXME [awe] 7.0 - SF2: impl. toJson for AdditionalTableRowData?
    }
    if (StringUtility.hasText(lookupRow.getCssClass())) {
      json.put("cssClass", lookupRow.getCssClass());
    }
    return json;
  }
}
