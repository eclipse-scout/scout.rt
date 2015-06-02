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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonDateField<T extends IDateField> extends JsonValueField<T> {

  public JsonDateField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "DateField";
  }

  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IDateField>(IDateField.PROP_HAS_TIME, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasTime();
      }
    });

    putJsonProperty(new JsonProperty<IDateField>(IDateField.PROP_HAS_DATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHasDate();
      }
    });
    putJsonProperty(new JsonProperty<IDateField>("timeFormatPattern", model) {
      @Override
      protected String modelValue() {
        DateFormat format = getModel().getIsolatedTimeFormat();
        return format != null && format instanceof SimpleDateFormat ? ((SimpleDateFormat) format).toPattern() : "";
      }
    });
    putJsonProperty(new JsonProperty<IDateField>("dateFormatPattern", model) {
      @Override
      protected String modelValue() {
        DateFormat format = getModel().getIsolatedDateFormat();
        return format != null && format instanceof SimpleDateFormat ? ((SimpleDateFormat) format).toPattern() : "";
      }
    });

    putJsonProperty(new JsonProperty<IDateField>("fullDatePattern", model) {
      @Override
      protected String modelValue() {
        DateFormat format = getModel().getDateFormat();
        return format != null && format instanceof SimpleDateFormat ? ((SimpleDateFormat) format).toPattern() : "";
      }
    });

    putJsonProperty(new JsonProperty<IDateField>("timestamp", model) {
      @Override
      protected Long modelValue() {
        return getModel().getValue() == null ? null : getModel().getValue().getTime();
      }
    });

  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    String propertyName = event.getPropertyName();
    if (IDateField.PROP_VALUE.equals(propertyName)) {
      Date newValue = (Date) event.getNewValue();
      addPropertyChangeEvent("timestamp", newValue == null ? null : newValue.getTime());
    }
    else {
      super.handleModelPropertyChange(event);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if ("timestampChanged".equals(event.getType())) {
      handleTimestampChanged(event.getData().optLong("timestamp"));
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleTimestampChanged(Long timestamp) {
    getModel().getUIFacade().setDateFromUI(timestamp == null ? null : new Date(timestamp));
  }

}
