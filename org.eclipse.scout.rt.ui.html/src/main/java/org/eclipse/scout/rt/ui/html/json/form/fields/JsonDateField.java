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
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.status.IStatus;
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
        // Always send date and time in case hasDate or hasTime is toggled dynamically
        return dateToJson((Date) value, true, true);
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_HAS_TIME, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasTime();
      }
    });
    putJsonProperty(new JsonProperty<T>(IDateField.PROP_TIMEPICKER_RESOLUTION, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getTimePickerResolution();
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
    return dateToJson(date, getModel().isHasDate(), getModel().isHasTime());
  }

  protected String dateToJson(Date date, boolean hasDate, boolean hasTime) {
    if (date == null) {
      return null;
    }
    return new JsonDate(date).asJsonString(false, hasDate, hasTime);
  }

  @Override
  protected void handleUiAcceptInput(JsonEvent event) {
    JSONObject data = event.getData();
    if (data.has(IValueField.PROP_DISPLAY_TEXT)) {
      this.handleUiDisplayTextChange(data);
    }
    if (data.has(IValueField.PROP_ERROR_STATUS)) {
      this.handleUiErrorStatusChange(data);
    }
    if (data.has(IValueField.PROP_VALUE)) {
      this.handleUiValueChange(data);
    }
  }

  @Override
  protected Object jsonToValue(String jsonValue) {
    return new JsonDate(jsonValue).asJavaDate();
  }

  @Override
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueFromUI((Date) value);
  }

  @Override
  protected void setDisplayTextFromUI(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

  @Override
  protected void setErrorStatusFromUI(IStatus status) {
    getModel().getUIFacade().setErrorStatusFromUI(status);
  }

}
