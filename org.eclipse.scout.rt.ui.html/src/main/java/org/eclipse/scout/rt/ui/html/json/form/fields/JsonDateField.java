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

import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class JsonDateField<T extends IDateField> extends JsonValueField<T> {

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
    putJsonProperty(new JsonProperty<T>(IValueField.PROP_VALUE, model) {
      @Override
      protected Date modelValue() {
        return getModel().getValue();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return dateToJson((Date) value);
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_AUTO_DATE, model) {
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
  protected void handleUiClear(JsonEvent event) {
    getModel().getUIFacade().setDisplayTextFromUI(null);
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    if (event.getData().has(IValueField.PROP_DISPLAY_TEXT)) {
      String displayText = event.getData().getString(IValueField.PROP_DISPLAY_TEXT);
      addPropertyEventFilterCondition(IValueField.PROP_DISPLAY_TEXT, displayText);
      getModel().getUIFacade().setDisplayTextFromUI(displayText);
    }

    if (event.getData().has(IValueField.PROP_ERROR_STATUS)) {
      JSONObject status = event.getData().optJSONObject(IValueField.PROP_ERROR_STATUS);
      addPropertyEventFilterCondition(IValueField.PROP_ERROR_STATUS, status);
      ParsingFailedStatus parseError = null;
      if (status != null) {
        String message = status.optString("message", null);
        parseError = new ParsingFailedStatus(message, getModel().getDisplayText());
      }
      getModel().getUIFacade().setErrorStatusFromUI(parseError);
    }

    if (event.getData().has(IValueField.PROP_VALUE)) {
      Date value = new JsonDate(event.getData().optString(IValueField.PROP_VALUE, null)).asJavaDate();
      addPropertyEventFilterCondition(IValueField.PROP_VALUE, value);
      getModel().getUIFacade().setValueFromUI(value);
    }
  }
}
