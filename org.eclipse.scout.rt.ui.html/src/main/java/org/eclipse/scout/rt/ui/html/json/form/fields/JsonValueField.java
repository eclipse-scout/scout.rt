/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONObject;

/**
 * Base class used to create JSON output for Scout form-fields with a value. When a sub-class need to provide a custom
 * <code>valueToJson()</code> method for the value property, it should replace the default JsonProperty for PROP_VALUE ,
 * with it's own implementation by calling <code>putJsonProperty()</code>.
 *
 * @param <VALUE_FIELD>
 */
@SuppressWarnings("squid:S00118")
public abstract class JsonValueField<VALUE_FIELD extends IValueField<?>> extends JsonFormField<VALUE_FIELD> {

  /**
   * This event is used when display-text has changed after field loses focus or when the display-text has changed while
   * typing (this event is send after each key-press). You can distinct the two cases by looking on the while- Typing
   * flag.
   */
  public static final String EVENT_ACCEPT_INPUT = "acceptInput";

  public JsonValueField(VALUE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

  @Override
  protected void initJsonProperties(VALUE_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<VALUE_FIELD>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayText();
      }
    });
    putJsonProperty(new JsonProperty<VALUE_FIELD>(IValueField.PROP_CLEARABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getClearable();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_ACCEPT_INPUT.equals(event.getType())) {
      handleUiAcceptInput(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAcceptInput(JsonEvent event) {
    String displayText = event.getData().optString(IValueField.PROP_DISPLAY_TEXT);
    addPropertyEventFilterCondition(IValueField.PROP_DISPLAY_TEXT, displayText);
    boolean whileTyping = event.getData().optBoolean("whileTyping", false);
    if (whileTyping) {
      handleUiAcceptInputWhileTyping(displayText);
    }
    else {
      handleUiAcceptInputAfterTyping(displayText);
    }
  }

  /**
   * Info: the handle*Change methods below are used by some sub-classes, but not by all sub-classes. Additionally
   * IValueField does not define a getUIFacade() method. The implementations of the UIFacade interfaces are quite
   * different from each other, so there's no easy way to add the method to IValueField and implement an IValueUIFacade.
   */

  protected void handleUiDisplayTextChange(JSONObject data) {
    String displayText = data.optString(IValueField.PROP_DISPLAY_TEXT);
    addPropertyEventFilterCondition(IValueField.PROP_DISPLAY_TEXT, displayText);
    setDisplayTextFromUI(displayText);
  }

  protected void setDisplayTextFromUI(String displayText) {
    // NOP may be implemented by sub-classes, using the individual UI facade implementation
  }

  protected void handleUiValueChange(JSONObject data) {
    Object jsonValue;
    if (data.isNull(IValueField.PROP_VALUE)) {
      jsonValue = null;
    }
    else {
      jsonValue = data.get(IValueField.PROP_VALUE);
    }
    Object value = jsonToValue(jsonValue);
    addPropertyEventFilterCondition(IValueField.PROP_VALUE, value);
    setValueFromUI(value);
  }

  protected void setValueFromUI(Object value) {
    // NOP may be implemented by sub-classes, using the individual UI facade implementation
  }

  protected Object jsonToValue(Object jsonValue) {
    return jsonValue;
  }

  protected void handleUiErrorStatusChange(JSONObject data) {
    JSONObject jsonStatus = data.optJSONObject(IValueField.PROP_ERROR_STATUS);
    addPropertyEventFilterCondition(IValueField.PROP_ERROR_STATUS, jsonStatus);
    ParsingFailedStatus status = null;
    if (jsonStatus != null) {
      status = new ParsingFailedStatus(JsonStatus.toScoutObject(jsonStatus), getModel().getDisplayText());
    }
    setErrorStatusFromUI(status);
  }

  protected void setErrorStatusFromUI(IStatus status) {
    // NOP may be implemented by sub-classes, using the individual UI facade implementation
  }

  /**
   * Called by the UI when the displayText has changed but the editing action has not yet finished (
   * <code>whileTyping = true</code>). The model field does not yet change its value. This method is usually only called
   * when the {@link IBasicField#PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY} flag is set.
   */
  protected void handleUiAcceptInputWhileTyping(String displayText) {
    // NOP may be implemented by sub-classes
  }

  /**
   * Called by the UI when the displayText has changed and the editing action has finished (
   * <code>whileTyping = false</code>). The model field parses the displayText and updates its value.
   */
  protected void handleUiAcceptInputAfterTyping(String displayText) {
    // NOP may be implemented by sub-classes
  }
}
