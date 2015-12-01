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

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.client.ui.form.fields.GridData;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonGridData;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;

public abstract class JsonFormField<FORM_FIELD extends IFormField> extends AbstractJsonPropertyObserver<FORM_FIELD> {

  public JsonFormField(FORM_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(FORM_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>("labelPosition", model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLabelPosition();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_MANDATORY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMandatory();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_STATUS_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStatusVisible();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_CSS_CLASS, model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_FONT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getFont();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return value instanceof FontSpec ? ((FontSpec) value).toPattern() : null;
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_BACKGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getBackgroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FONT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getLabelFont();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return value instanceof FontSpec ? ((FontSpec) value).toPattern() : null;
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_BACKGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelBackgroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_ERROR_STATUS, model) {
      @Override
      protected IStatus modelValue() {
        return getModel().getErrorStatus();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonStatus.toJson((IStatus) value);
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>("gridData", model) {
      @Override
      protected GridData modelValue() {
        return getModel().getGridData();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return JsonGridData.toJson((GridData) value);
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LOADING, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLoading();
      }
    });
    putJsonProperty(new JsonAdapterProperty<FORM_FIELD>(IFormField.PROP_KEY_STROKES, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<IAction>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "FormField";
  }

}
