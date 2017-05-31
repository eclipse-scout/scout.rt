package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.ISmartField2;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield2.SmartField2Result;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.basic.table.AbstractTableRowData;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.json.JSONArray;
import org.json.JSONObject;

// FIXME [awe] 7.0 - SF2: key problem lösen... angenommen wir tippen im GUI "b*" und der lookup liefert drei rows zurück "0: B1", "1: B2", "2: B3"
// wir wählen B1 (key=0). Jetzt tippen wir im Suchfeld B2. Nun wird die Map auf dem Server resetted und für B2 ein key 0 angelegt. Das GUI hat nun
// das gefühl, dass es nichts ändern muss, weil der value ja schon 0 ist --> Die Keys müssen stabil bleiben!

public class JsonSmartField2<VALUE, T extends ISmartField2<VALUE>> extends JsonValueField<T> {

  // Contains always the mapping from the last performed lookup operation
  // all values are reset each time a new lookup starts
  private Map<Object, Integer> m_keyToIdMap = new HashMap<>();
  private Map<Integer, Object> m_idToKeyMap = new HashMap<>();
  private int m_id = 0;

  public JsonSmartField2(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) { // FIXME [awe] 7.0 - SF2: hier auch den value hoch schicken?
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_RESULT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getResult();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return resultToJson((SmartField2Result<VALUE>) value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_LOOKUP_ROW, model) {
      @Override
      protected Object modelValue() {
        return getModel().getLookupRow();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return lookupRowToJson((LookupRow<VALUE>) value, false); // FIXME [awe] 7.0 - set multipleColumns parameter correctly
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_BROWSE_MAX_ROW_COUNT, model) {
      @Override
      protected TriState modelValue() {
        return getModel().getActiveFilter();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_BROWSE_AUTO_EXPAND_ALL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBrowseAutoExpandAll();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_BROWSE_LOAD_INCREMENTAL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBrowseLoadIncremental();
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
      protected String[] modelValue() {
        return getModel().getActiveFilterLabels();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JSONArray(value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField2<VALUE>>(ISmartField2.PROP_COLUMN_DESCRIPTORS, model) {
      @Override
      protected ColumnDescriptor[] modelValue() {
        return getModel().getColumnDescriptors();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return columnDescriptorsToJson(value);
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
    else if ("lookupByRec".equals(event.getType())) {
      handleUiLookupByRec(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  // FIXME [awe] 7.0 - SF2: use UI facade here?

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IValueField.PROP_VALUE.equals(propertyName)) {
      handleUiPropertyChangeValue(data);
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

  protected void handleUiPropertyChangeValue(JSONObject data) {
    String mappedKey = data.optString("value");
    VALUE key = getLookupRowKeyForId(mappedKey);
    addPropertyEventFilterCondition(IValueField.PROP_VALUE, key);
    getModel().setValue(key);
  }

  protected void handleUiLookupByText(JsonEvent event) {
    String searchText = event.getData().optString("searchText");
    getModel().lookupByText(searchText);
  }

  protected void handleUiLookupByRec(JsonEvent event) {
    String mappedParentKey = event.getData().optString("rec");
    VALUE rec = getLookupRowKeyForId(mappedParentKey);
    getModel().lookupByRec(rec);
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

  /**
   * Returns a numeric ID for the given lookup row key. If the key is already mapped to an ID the existing ID is
   * returned. Otherwise a new ID is returned.
   *
   * @param key
   * @return
   */
  protected int getIdForLookupRowKey(Object key) {
    if (m_keyToIdMap.containsKey(key)) {
      return m_keyToIdMap.get(key);
    }

    int id = m_id++;
    m_keyToIdMap.put(key, id);
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
    boolean multipleColumns = getModel().getColumnDescriptors() != null;
    for (LookupRow<?> lookupRow : (Collection<LookupRow<?>>) result.getLookupRows()) {
      lookupRows.put(lookupRowToJson(lookupRow, multipleColumns));
    }
    json.put("lookupRows", lookupRows);
    json.put("searchText", result.getSearchText());
    if (result.isLookupFailed()) {
      json.put("lookupFailed", result.isLookupFailed());
    }
    if (result.isNoData()) {
      json.put("noData", result.isNoData());
    }
    return json;
  }

  protected JSONObject lookupRowToJson(LookupRow<?> lookupRow, boolean multipleColumns) {
    if (lookupRow == null) {
      return null;
    }

    JSONObject json = new JSONObject();
    json.put("key", getIdForLookupRowKey(lookupRow.getKey()));
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
      json.put("parentKey", getIdForLookupRowKey(lookupRow.getParentKey()));
    }
    if (!lookupRow.isActive()) {
      json.put("active", lookupRow.isActive());
    }
    if (multipleColumns && lookupRow.getAdditionalTableRowData() != null) {
      json.put("additionalTableRowData", tableRowDataToJson(lookupRow.getAdditionalTableRowData()));
    }
    if (StringUtility.hasText(lookupRow.getCssClass())) {
      json.put("cssClass", lookupRow.getCssClass());
    }
    return json;
  }

  protected Object tableRowDataToJson(AbstractTableRowData tableRowData) {
    if (tableRowData == null) {
      return null;
    }
    return MainJsonObjectFactory.get().createJsonObject(tableRowData).toJson();
  }

  @SuppressWarnings("unchecked")
  protected VALUE getLookupRowKeyForId(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    else {
      return (VALUE) m_idToKeyMap.get(NumberUtility.parseInt(id));
    }
  }

  protected JSONArray columnDescriptorsToJson(Object value) {
    if (value == null) {
      return null;
    }
    ColumnDescriptor[] descs = (ColumnDescriptor[]) value;
    JSONArray array = new JSONArray();
    for (ColumnDescriptor desc : descs) {
      JSONObject json = new JSONObject();
      json.put("propertyName", desc.getPropertyName());
      json.put("width", desc.getWidth());
      json.put("text", desc.getText());
      array.put(json);
    }
    return array;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(ISmartField2.PROP_DISPLAY_STYLE, getModel().getDisplayStyle());
    json.put(ISmartField2.PROP_BROWSE_HIERARCHY, getModel().isBrowseHierarchy());
    return json;
  }
}
