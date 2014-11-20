/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.PropertyChangeEventFilterCondition;

/**
 * Base class used to create JSON output for Scout form-fields with a value. When a sub-class need to provide a custom
 * <code>valueToJson()</code> method for the value property, it should replace the default JsonProperty for PROP_VALUE ,
 * with it's own implementation by calling <code>putJsonProperty()</code>.
 *
 * @param <T>
 */
public class JsonValueField<T extends IValueField<?>> extends JsonFormField<T> {
  public static final String EVENT_DISPLAY_TEXT_CHANGED = "displayTextChanged";

  public JsonValueField(T model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IValueField.PROP_DISPLAY_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayText();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "ValueField";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_DISPLAY_TEXT_CHANGED.equals(event.getType())) {
      handleUiDisplayTextChanged(event);
    }
    else {
      super.handleUiEvent(event, res);
    }
  }

  protected void handleUiDisplayTextChanged(JsonEvent event) {
    String displayText = JsonObjectUtility.getString(event.getData(), IValueField.PROP_DISPLAY_TEXT);
    boolean whileTyping = event.getData().optBoolean("whileTyping");

    PropertyChangeEventFilterCondition condition = new PropertyChangeEventFilterCondition(IValueField.PROP_DISPLAY_TEXT, displayText);
    getPropertyEventFilter().addCondition(condition);
    try {
      handleUiDisplayTextChangedImpl(displayText, whileTyping);
    }
    finally {
      getPropertyEventFilter().removeCondition(condition);
    }
  }

  protected void handleUiDisplayTextChangedImpl(String displayText, boolean whileTyping) {
    //NOP may be implemented by subclasses
  }
}
