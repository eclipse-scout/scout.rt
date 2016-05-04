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

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONArray;

public class JsonDateField<T extends IDateField> extends JsonValueField<T> {

  private static final String PROP_TIMESTAMP = "timestamp";
  private static final String PROP_AUTO_TIMESTAMP = "autoTimestamp";
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
        for (Date date : allowedDates) {
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
    getModel().getUIFacade().removeParseErrorFromUI();
    getModel().getUIFacade().setParseErrorFromUI();
  }

  @Override
  protected void handleUiDisplayTextChangedWhileTyping(String displayText) {
    throw new IllegalStateException("While typing is not supported by the date field.");
  }

  @Override
  protected void handleUiDisplayTextChangedAfterTyping(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }
}
