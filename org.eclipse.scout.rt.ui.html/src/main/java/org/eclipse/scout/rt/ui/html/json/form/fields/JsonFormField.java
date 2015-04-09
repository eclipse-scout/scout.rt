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

import org.eclipse.scout.commons.status.IStatus;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.json.JSONObject;

public abstract class JsonFormField<T extends IFormField> extends AbstractJsonPropertyObserver<T> {

  public JsonFormField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_LABEL_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>("labelPosition", model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLabelPosition();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_MANDATORY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMandatory();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_STATUS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStatusVisible();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_CSS_CLASS, model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });
    putJsonProperty(new JsonProperty<T>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getErrorStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
    putJsonProperty(new JsonProperty<T>("gridData", model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return new JsonGridData((GridData) value).toJson();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getKeyStrokes());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), "keyStrokes", getModel().getKeyStrokes());
  }

}
