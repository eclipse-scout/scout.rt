/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.scout.rt.client.services.lookup.ILookupCallResult;
import org.eclipse.scout.rt.client.ui.basic.table.columns.ColumnDescriptor;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.smartfield.ISmartField;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.TriState;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.services.lookup.ILookupRow;
import org.eclipse.scout.rt.shared.services.lookup.LookupRow;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.eclipse.scout.rt.ui.html.json.lookup.JsonLookupCallResult;
import org.eclipse.scout.rt.ui.html.json.lookup.JsonLookupRow;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonSmartField<VALUE, MODEL extends ISmartField<VALUE>> extends JsonValueField<MODEL> {

  // Contains always the mapping from the last performed lookup operation
  // all values are reset each time a new lookup starts
  private final Map<Object, Integer> m_keyToIdMap = new HashMap<>();
  private final Map<Integer, Object> m_idToKeyMap = new HashMap<>();
  private int m_id = 0;

  public JsonSmartField(MODEL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(MODEL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(IValueField.PROP_VALUE, model) {
      @Override
      protected VALUE modelValue() {
        return getModel().getValue();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return JsonSmartField.this.valueToJson((VALUE) value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_RESULT, model) {

      @Override
      public boolean accept() {
        return getModel().getResult() != null;
      }

      @Override
      protected Object modelValue() {
        return getModel().getResult();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return resultToJson((ILookupCallResult<VALUE>) value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_LOOKUP_ROW, model) {
      @Override
      protected Object modelValue() {
        return getModel().getLookupRow();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        return lookupRowToJson((LookupRow<VALUE>) value, hasMultipleColumns());
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_BROWSE_MAX_ROW_COUNT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getBrowseMaxRowCount();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_BROWSE_AUTO_EXPAND_ALL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBrowseAutoExpandAll();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_BROWSE_LOAD_INCREMENTAL, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBrowseLoadIncremental();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_BROWSE_HIERARCHY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isBrowseHierarchy();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_ACTIVE_FILTER_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isActiveFilterEnabled();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_SEARCH_REQUIRED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSearchRequired();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_ACTIVE_FILTER, model) {
      @Override
      protected TriState modelValue() {
        return getModel().getActiveFilter();
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_ACTIVE_FILTER_LABELS, model) {
      @Override
      protected String[] modelValue() {
        return getModel().getActiveFilterLabels();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JSONArray(value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_COLUMN_DESCRIPTORS, model) {
      @Override
      protected ColumnDescriptor[] modelValue() {
        return getModel().getColumnDescriptors();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return columnDescriptorsToJson((ColumnDescriptor[]) value);
      }
    });
    putJsonProperty(new JsonProperty<ISmartField<VALUE>>(ISmartField.PROP_MAX_LENGTH, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getMaxLength();
      }
    });
  }

  @Override
  public String getObjectType() {
    if (getModel().isMultilineText()) {
      return "SmartFieldMultiline";
    }
    else {
      return "SmartField";
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("lookupByAll".equals(event.getType())) {
      handleUiLookupByAll();
    }
    else if ("lookupByText".equals(event.getType())) {
      handleUiLookupByText(event);
    }
    else if ("lookupByKey".equals(event.getType())) {
      handleUiLookupByKey(event);
    }
    else if ("lookupByRec".equals(event.getType())) {
      handleUiLookupByRec(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    JSONObject data = event.getData();
    boolean valueSet = false;
    VALUE valueFromUi = null;

    if (data.has(IValueField.PROP_DISPLAY_TEXT)) {
      handleUiDisplayTextChange(data);
    }

    // When we have a lookup row, we prefer the lookup row over the value
    if (data.has(ISmartField.PROP_LOOKUP_ROW)) {
      valueFromUi = valueFromJsonLookupRow(data);
      handleUiLookupRowChange(data);
      valueSet = true;
    }
    else if (data.has(IValueField.PROP_VALUE)) {
      valueFromUi = valueFromJsonValue(data);
      handleUiValueChange(data);
      valueSet = true;
    }

    // ensure the correct model status is sent to the client.
    // In case the UI resets the error status and the validation ends with the same error the field has before the status is not displayed correctly.
    addPropertyChangeEvent(IValueField.PROP_ERROR_STATUS, JsonStatus.toJson(getModel().getErrorStatus()));

    // In case the model changes its value to something other than what the UI
    // sends, we cannot set display text and error status. This can happen if
    // execValidateValue is overridden.
    if (valueSet) {
      VALUE valueFromModel = getModel().getValue();
      if (!ObjectUtility.equals(valueFromUi, valueFromModel)) {
        addPropertyChangeEvent(ISmartField.PROP_LOOKUP_ROW, lookupRowToJson(getModel().getLookupRow(), hasMultipleColumns()));
        String displayTextFromModel = getModel().getDisplayText();
        addPropertyChangeEvent(IValueField.PROP_DISPLAY_TEXT, displayTextFromModel);
        return;
      }
    }

    // Don't use error status from UI when value has been set
    if (valueSet) {
      return;
    }

    if (data.has(IValueField.PROP_ERROR_STATUS)) {
      handleUiErrorStatusChange(data);
    }
  }

  @SuppressWarnings("unchecked")
  protected VALUE valueFromJsonValue(JSONObject data) {
    return (VALUE) jsonToValue(data.optString(IValueField.PROP_VALUE, null));
  }

  protected VALUE valueFromJsonLookupRow(JSONObject data) {
    JSONObject jsonLookupRow = data.optJSONObject(ISmartField.PROP_LOOKUP_ROW);
    ILookupRow<VALUE> lookupRow = lookupRowFromJson(jsonLookupRow);
    return lookupRow == null ? null : lookupRow.getKey();
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IValueField.PROP_VALUE.equals(propertyName)) {
      handleUiValueChange(data);
    }
    else if (IValueField.PROP_DISPLAY_TEXT.equals(propertyName)) {
      handleUiDisplayTextChange(data);
    }
    else if (ISmartField.PROP_LOOKUP_ROW.equals(propertyName)) {
      handleUiLookupRowChange(data);
    }
    else if (ISmartField.PROP_ACTIVE_FILTER.equals(propertyName)) {
      String activeFilterString = data.optString(propertyName, null);
      TriState activeFilter = TriState.valueOf(activeFilterString);
      addPropertyEventFilterCondition(propertyName, activeFilter);
      getModel().getUIFacade().setActiveFilterFromUI(activeFilter);
    }
    else if (IFormField.PROP_ERROR_STATUS.equals(propertyName)) {
      handleUiErrorStatusChange(data);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  @Override
  protected Object jsonToValue(Object jsonValue) {
    return getLookupRowKeyForId((String) jsonValue); // jsonValue == mapped key
  }

  protected Object valueToJson(VALUE value) {
    if (value == null) {
      return null;
    }
    return getIdForLookupRowKey(value);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueFromUI((VALUE) value);
  }

  @Override
  protected void setDisplayTextFromUI(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

  @Override
  protected void setErrorStatusFromUI(IStatus status) {
    getModel().getUIFacade().setErrorStatusFromUI(status);
  }

  protected void handleUiLookupRowChange(JSONObject data) {
    JSONObject jsonLookupRow = data.optJSONObject(ISmartField.PROP_LOOKUP_ROW);
    ILookupRow<VALUE> lookupRow = lookupRowFromJson(jsonLookupRow);
    VALUE value = lookupRow == null ? null : lookupRow.getKey();
    addPropertyEventFilterCondition(ISmartField.PROP_LOOKUP_ROW, lookupRow);
    addPropertyEventFilterCondition(IValueField.PROP_VALUE, value);
    getModel().getUIFacade().setLookupRowFromUI(lookupRow);
  }

  protected void handleUiLookupByText(JsonEvent event) {
    String text = event.getData().optString("text");
    getModel().lookupByText(text);
  }

  protected void handleUiLookupByRec(JsonEvent event) {
    String mappedParentKey = event.getData().optString("rec", null);
    VALUE rec = getLookupRowKeyForId(mappedParentKey);
    getModel().lookupByRec(rec);
  }

  protected void handleUiLookupByKey(JsonEvent event) {
    String mappedKey = event.getData().optString("key", null);
    VALUE key = getLookupRowKeyForId(mappedKey);
    getModel().lookupByKey(key);
  }

  /**
   * Why resolve current key and not resolve key with a parameter? Because it is not guaranteed that the key is
   * serializable / comparable. So we cannot simply send the key from the UI to the server. Additionally we do not have
   * a list of lookup rows as we have in lookupByText
   */
  protected void handleUiLookupByAll() {
    getModel().lookupByAll();
  }

  protected void resetKeyMap() {
    m_keyToIdMap.clear();
    m_idToKeyMap.clear();
    m_id = 0;
  }

  /**
   * Returns a numeric ID for the given lookup row key. If the key is already mapped to an ID the existing ID is
   * returned. Otherwise a new ID is returned.
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

  protected boolean hasMultipleColumns() {
    return getModel().getColumnDescriptors() != null;
  }

  protected Object resultToJson(ILookupCallResult<VALUE> result) {
    return new P_JsonLookupCallResult(result, hasMultipleColumns(), this::getIdForLookupRowKey).toJson();
  }

  protected ILookupRow<VALUE> lookupRowFromJson(JSONObject json) {
    if (json == null) {
      return null;
    }

    VALUE lookupRowKey = getLookupRowKeyForId(json.optString("key", null));
    String lookupRowText = json.optString("text");
    return createLookupRow(lookupRowKey, lookupRowText, json);
  }

  protected ILookupRow<VALUE> createLookupRow(VALUE key, String text, JSONObject json) {
    return applyJson(new LookupRow<>(key, text), json);
  }

  protected <LOOKUP_ROW extends ILookupRow<VALUE>> LOOKUP_ROW applyJson(LOOKUP_ROW lookupRow, JSONObject json) {
    if (json.has("iconId")) {
      lookupRow.withIconId(json.optString("iconId", null));
    }
    if (json.has("enabled")) {
      lookupRow.withEnabled(json.getBoolean("enabled"));
    }
    if (json.has("tooltipText")) {
      lookupRow.withTooltipText(json.optString("tooltipText", null));
    }
    if (json.has("backgroundColor")) {
      lookupRow.withBackgroundColor(json.optString("backgroundColor", null));
    }
    if (json.has("foregroundColor")) {
      lookupRow.withForegroundColor(json.optString("foregroundColor", null));
    }
    if (json.has("font")) {
      lookupRow.withFont(FontSpec.parse(json.optString("font", null)));
    }
    if (json.has("parentKey")) {
      lookupRow.withParentKey(getLookupRowKeyForId(json.optString("parentKey", null)));
    }
    if (json.has("active")) {
      lookupRow.withActive(json.getBoolean("active"));
    }
    if (json.has("cssClass")) {
      lookupRow.withCssClass(json.optString("cssClass", null));
    }
    // Info: cannot de-serialize 'additionalTableRowData' because it uses generic
    // JSON serialization. See JsonLookupRow#tableRowDataToJson - this shouldn't
    // be a problem because that data is only used in the proposal chooser
    return lookupRow;
  }

  protected Object lookupRowToJson(ILookupRow<?> lookupRow, boolean multipleColumns) {
    return JsonLookupRow.toJson(lookupRow, multipleColumns, this::getIdForLookupRowKey);
  }

  @SuppressWarnings("unchecked")
  protected VALUE getLookupRowKeyForId(String id) {
    if (StringUtility.isNullOrEmpty(id)) {
      return null;
    }
    return (VALUE) m_idToKeyMap.get(NumberUtility.parseInt(id));
  }

  protected JSONArray columnDescriptorsToJson(ColumnDescriptor[] descriptors) {
    if (descriptors == null) {
      return null;
    }
    JSONArray array = new JSONArray();
    for (ColumnDescriptor desc : descriptors) {
      JSONObject json = columnDescriptorToJson(desc);
      if (json != null) {
        array.put(json);
      }
    }
    return array;
  }

  protected JSONObject columnDescriptorToJson(ColumnDescriptor descriptor) {
    if (descriptor == null) {
      return null;
    }
    JSONObject json = new JSONObject();
    json.put("propertyName", descriptor.getPropertyName());
    json.put("objectType", descriptor.getObjectType());
    json.put("text", descriptor.getText());
    json.put("headerIconId", descriptor.getHeaderIconId());
    json.put("cssClass", descriptor.getCssClass());
    json.put("width", descriptor.getWidth());
    json.put("fixedWidth", descriptor.isFixedWidth());
    json.put("fixedPosition", descriptor.isFixedPosition());
    json.put("autoOptimizeWidth", descriptor.isAutoOptimizeWidth());
    json.put("horizontalAlignment", descriptor.getHorizontalAlignment());
    json.put("visible", descriptor.isVisible());
    json.put("htmlEnabled", descriptor.isHtmlEnabled());
    return json;
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    json.put(ISmartField.PROP_DISPLAY_STYLE, getModel().getDisplayStyle());
    return json;
  }

  /**
   * Subclass is required so sub classes of the SmartField can provide their own lookupRowToJson method.
   */
  class P_JsonLookupCallResult extends JsonLookupCallResult<VALUE> {

    public P_JsonLookupCallResult(ILookupCallResult<VALUE> result, boolean multipleColumns, Function<VALUE, ?> keyMapper) {
      super(result, multipleColumns, keyMapper);
    }

    @Override
    protected Object lookupRowToJson(ILookupRow<VALUE> lookupRow, boolean multipleColumns) {
      return JsonSmartField.this.lookupRowToJson(lookupRow, multipleColumns);
    }
  }
}
