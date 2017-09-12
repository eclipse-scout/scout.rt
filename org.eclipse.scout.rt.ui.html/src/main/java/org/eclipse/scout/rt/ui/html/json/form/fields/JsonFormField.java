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
import java.util.List;

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
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.JsonStatus;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S00118")
public abstract class JsonFormField<FORM_FIELD extends IFormField> extends AbstractJsonPropertyObserver<FORM_FIELD> {

  private static final Logger LOG = LoggerFactory.getLogger(JsonFormField.class);

  private static final String PROP_LABEL_WIDTH_IN_PIXEL = "labelWidthInPixel";

  public JsonFormField(FORM_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FormField";
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
        return (int) getModel().getLabelPosition();
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
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_STATUS_POSITION, model) {
      @Override
      protected String modelValue() {
        return getModel().getStatusPosition();
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
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_LABEL_FOREGROUND_COLOR, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabelForegroundColor();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(PROP_LABEL_WIDTH_IN_PIXEL, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getLabelWidthInPixel();
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
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<>()).build();
      }

      @Override
      protected List<IKeyStroke> modelValue() {
        return getModel().getKeyStrokes();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_PREVENT_INITIAL_FOCUS, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isPreventInitialFocus();
      }
    });
    putJsonProperty(new JsonProperty<FORM_FIELD>(IFormField.PROP_DISABLED_STYLE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisabledStyle();
      }
    });
  }

  @Override
  protected void handleModelPropertyChange(PropertyChangeEvent event) {
    // If a field is set to visibleGranted=false, a PROP_VISIBLE property change event is fired. In most cases,
    // the JsonAdapter is not yet attached, so this event will not be received here. The adapter will not be
    // attached because of the DisplayableFormFieldFilter. There are however rare cases, where the adapter
    // is already attached when visibleGranted is set to false. If the adapter is not yet sent to the UI,
    // we still have the chance to dispose the adapter and pretend it was never attached in the first place.
    // [Similar code exist in JsonAction]
    if (IFormField.PROP_VISIBLE.equals(event.getPropertyName()) && !getModel().isVisibleGranted()) {
      JsonResponse response = getUiSession().currentJsonResponse();
      if (response.containsAdapter(this) && response.isWritable()) {
        dispose();
        return;
      }
      LOG.warn("Setting visibleGranted=false has no effect, because JsonAdapter {} ({}) is already sent to the UI.", getId(), getModel());
    }
    super.handleModelPropertyChange(event);
  }
}
