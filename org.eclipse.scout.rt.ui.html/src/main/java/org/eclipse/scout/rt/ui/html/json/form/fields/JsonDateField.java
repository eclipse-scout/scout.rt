/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.status.IMultiStatus;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonDateField<T extends IDateField> extends JsonValueField<T> {

  private static final String PROP_TIMESTAMP = "timestamp";
  private static final String PROP_AUTO_TIMESTAMP = "autoTimestamp";
  private static final String PROP_INVALID_DISPLAY_TEXT = "invalidDisplayText";
  private static final String PROP_INVALID_DATE_TEXT = "invalidDateText";
  private static final String PROP_INVALID_TIME_TEXT = "invalidTimeText";
  // UI events
  private static final String EVENT_TIMESTAMP_CHANGED = "timestampChanged";
  private static final String EVENT_PARSING_ERROR = "parsingError";

  public JsonDateField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "DateField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    // Don't send the displayText to the UI. Formatting and parsing is done completely on the UI,
    // the JSON layer only needs to send the format and the time stamp.
    removeJsonProperty(IValueField.PROP_DISPLAY_TEXT);

    putJsonProperty(new JsonProperty<T>(IFormField.PROP_ERROR_STATUS, model) {
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
    putJsonProperty(new JsonProperty<T>(PROP_TIMESTAMP, model) {
      @Override
      protected Date modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return dateToJson((Date) value);
      }
    });
    putJsonProperty(new JsonProperty<T>(PROP_AUTO_TIMESTAMP, model) {
      @Override
      protected Date modelValue() {
        return getModel().getAutoDate();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return dateToJson((Date) value);
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_HAS_TIME, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasTime();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_HAS_DATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasDate();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_DATE_FORMAT_PATTERN, model) {
      @Override
      protected String modelValue() {
        return getModel().getDateFormatPattern();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_TIME_FORMAT_PATTERN, model) {
      @Override
      protected String modelValue() {
        return getModel().getTimeFormatPattern();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_ALLOWED_DATES, model) {
      @Override
      protected List<Date> modelValue() {
        return getModel().getAllowedDates();
      }

      @Override
      @SuppressWarnings("unchecked")
      public Object prepareValueForToJson(Object value) {
        List<Date> allowedDates = (List<Date>) value;
        if (allowedDates == null || allowedDates.isEmpty()) {
          return null;
        }
        JSONArray dateArray = new JSONArray();
        for (Date date : allowedDates) { // FIXME awe: (date-field) order dates!
          dateArray.put(dateToJson(date));
        }
        return dateArray;
      }
    });
  }

  protected String dateToJson(Date date) {
    if (date == null) {
      return null;
    }
    return new JsonDate(date).asJsonString(false, getModel().isHasDate(), getModel().isHasTime());
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    // Translate "value changed" to "timestamp changed"
    if (IDateField.PROP_VALUE.equals(propertyName)) {
      PropertyChangeEvent filteredEvent = filterPropertyChangeEvent(event);
      if (filteredEvent != null) {
        addPropertyChangeEvent(PROP_TIMESTAMP, dateToJson((Date) event.getNewValue()));
      }
    }
    // Translate "auto date changed" to "auto timestamp changed"
    else if (IDateField.PROP_AUTO_DATE.equals(propertyName)) {
      PropertyChangeEvent filteredEvent = filterPropertyChangeEvent(event);
      if (filteredEvent != null) {
        addPropertyChangeEvent(PROP_AUTO_TIMESTAMP, dateToJson((Date) event.getNewValue()));
      }
    }
    else if (IDateField.PROP_ERROR_STATUS.equals(propertyName)) {
      //if error occured thru wrong input and model is resetting value to null it has to be submitted to client.
      // -> only displayText is changed on model so there will be no event for value changed
      if (getModel().getValue() == null && getModel().getDisplayText() == "" && event.getNewValue() == null) {
        addPropertyChangeEvent(PROP_TIMESTAMP, dateToJson((Date) event.getNewValue()));
      }
      super.handleModelPropertyChange(event);
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
    Date uiValue = new JsonDate(event.getData().optString(PROP_TIMESTAMP, null)).asJavaDate();
    addPropertyEventFilterCondition(IValueField.PROP_VALUE, uiValue);
    getModel().getUIFacade().removeParseErrorFromUI();
    getModel().getUIFacade().setDateTimeFromUI(uiValue);

    // If the model value is changed during validation, it needs to be updated in the GUI again.
    Date modelValue = getModel().getValue();
    if (!DateUtility.equals(uiValue, modelValue)) {
      addPropertyChangeEvent(PROP_TIMESTAMP, dateToJson((Date) modelValue));
    }

  }

  protected void handleUiParsingError(JsonEvent event) {
    String invalidDisplayText = event.getData().optString(PROP_INVALID_DISPLAY_TEXT, null);
    String invalidDateText = event.getData().optString(PROP_INVALID_DATE_TEXT, null);
    String invalidTimeText = event.getData().optString(PROP_INVALID_TIME_TEXT, null);
    getModel().getUIFacade().removeParseErrorFromUI();
    getModel().getUIFacade().setParseErrorFromUI(invalidDisplayText, invalidDateText, invalidTimeText);
  }

  @Override
  protected void handleUiDisplayTextChangedWhileTyping(String displayText) {
    throw new IllegalStateException("DisplayText may not be set manually");
  }

  @Override
  protected void handleUiDisplayTextChangedAfterTyping(String displayText) {
    throw new IllegalStateException("DisplayText may not be set manually");
  }
}
