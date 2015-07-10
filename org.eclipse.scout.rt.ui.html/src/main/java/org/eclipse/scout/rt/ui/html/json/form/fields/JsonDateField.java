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

import java.beans.PropertyChangeEvent;
import java.util.Date;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.status.IMultiStatus;
import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONObject;

public class JsonDateField<DATE_FIELD extends IDateField> extends JsonValueField<DATE_FIELD> {

  private static final String PROP_TIMESTAMP = "timestamp";
  private static final String PROP_INVALID_DISPLAY_TEXT = "invalidDisplayText";
  private static final String PROP_INVALID_DATE_TEXT = "invalidDateText";
  private static final String PROP_INVALID_TIME_TEXT = "invalidTimeText";
  // UI events
  private static final String EVENT_TIMESTAMP_CHANGED = "timestampChanged";
  private static final String EVENT_PARSING_ERROR = "parsingError";

  public JsonDateField(DATE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "DateField";
  }

  @Override
  protected void initJsonProperties(DATE_FIELD model) {
    super.initJsonProperties(model);

    // Don't send the displayText to the UI. Formatting and parsing is done completely on the UI,
    // the JSON layer only needs to send the format and the time stamp.
    removeJsonProperty(IValueField.PROP_DISPLAY_TEXT);

    putJsonProperty(new JsonProperty<DATE_FIELD>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getErrorStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        JSONObject jsonStatus = JsonStatus.toJson((IStatus) value);
        // Override default from JsonFormField: Send invalid input texts back to the UI.
        if (jsonStatus != null && value instanceof IMultiStatus) {
          IMultiStatus multiStatus = (IMultiStatus) value;
          for (IStatus status : multiStatus.getChildren()) {
            if (status instanceof ParsingFailedStatus) {
              ParsingFailedStatus parsingFailedStatus = (ParsingFailedStatus) status;
              String parseInputString = StringUtility.nvl(parsingFailedStatus.getParseInputString(), "");
              String[] texts = parseInputString.split("\n", -1); // -1 preserves trailing empty strings
              if (texts.length == 2) {
                jsonStatus.put(PROP_INVALID_DATE_TEXT, texts[0]);
                jsonStatus.put(PROP_INVALID_TIME_TEXT, texts[1]);
              }
            }
          }
        }
        return jsonStatus;
      }
    });

    putJsonProperty(new JsonProperty<DATE_FIELD>(PROP_TIMESTAMP, model) {
      @Override
      protected Date modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return new JsonDate((Date) value).asJsonString(false, getModel().isHasDate(), getModel().isHasTime());
      }
    });
    putJsonProperty(new JsonProperty<DATE_FIELD>(IDateField.PROP_HAS_TIME, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasTime();
      }
    });
    putJsonProperty(new JsonProperty<DATE_FIELD>(IDateField.PROP_HAS_DATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasDate();
      }
    });
    putJsonProperty(new JsonProperty<DATE_FIELD>(IDateField.PROP_DATE_FORMAT_PATTERN, model) {
      @Override
      protected String modelValue() {
        return getModel().getDateFormatPattern();
      }
    });
    putJsonProperty(new JsonProperty<DATE_FIELD>(IDateField.PROP_TIME_FORMAT_PATTERN, model) {
      @Override
      protected String modelValue() {
        return getModel().getTimeFormatPattern();
      }
    });
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    if (IDateField.PROP_VALUE.equals(propertyName)) {
      PropertyChangeEvent filteredEvent = filterPropertyChangeEvent(event);
      if (filteredEvent != null) {
        // Translate "value changed" to "timestamp changed"
        Date newValue = (Date) event.getNewValue();
        String timestamp = new JsonDate(newValue).asJsonString(false, getModel().isHasDate(), getModel().isHasTime());
        addPropertyChangeEvent(PROP_TIMESTAMP, timestamp);
      }
    }
    else {
      super.handleModelPropertyChange(event);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_TIMESTAMP_CHANGED.equals(event.getType())) {
      handleUiTimestampChanged(event);
    }
    else if (EVENT_PARSING_ERROR.equals(event.getType())) {
      handleUiParsingError(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiTimestampChanged(JsonEvent event) {
    Date date = new JsonDate(event.getData().optString(PROP_TIMESTAMP)).asJavaDate();
    addPropertyEventFilterCondition(IValueField.PROP_VALUE, date);
    getModel().getUIFacade().removeParseErrorFromUI();
    getModel().getUIFacade().setDateTimeFromUI(date);
  }

  protected void handleUiParsingError(JsonEvent event) {
    String invalidDisplayText = event.getData().optString(PROP_INVALID_DISPLAY_TEXT);
    String invalidDateText = event.getData().optString(PROP_INVALID_DATE_TEXT);
    String invalidTimeText = event.getData().optString(PROP_INVALID_TIME_TEXT);
    getModel().getUIFacade().removeParseErrorFromUI();
    getModel().getUIFacade().setParseErrorFromUI(invalidDisplayText, invalidDateText, invalidTimeText);
  }

  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    throw new IllegalStateException("DisplayText may not be set manually");
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText) {
    throw new IllegalStateException("DisplayText may not be set manually");
  }
}
