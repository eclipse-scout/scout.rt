/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.button;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.button.IButton;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceHolder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;
import org.eclipse.scout.rt.ui.html.res.IBinaryResourceProvider;
import org.json.JSONObject;

public class JsonButton<BUTTON extends IButton> extends JsonFormField<BUTTON> implements IBinaryResourceProvider {

  public static final String PROP_SYSTEM_TYPE = "systemType";
  public static final String PROP_PROCESS_BUTTON = "processButton";
  public static final String PROP_DEFAULT_BUTTON = "defaultButton";
  public static final String PROP_DISPLAY_STYLE = "displayStyle";

  public JsonButton(BUTTON model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Button";
  }

  @Override
  protected void initJsonProperties(BUTTON model) {
    super.initJsonProperties(model);
    // Overrides label-handling from super class
    putJsonProperty(new JsonProperty<BUTTON>(IFormField.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.replaceImageUrls(JsonButton.this, (String) value);
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_SYSTEM_TYPE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSystemType();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_PROCESS_BUTTON, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isProcessButton();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_DEFAULT_BUTTON, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getDefaultButton();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(PROP_DISPLAY_STYLE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_KEY_STROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getKeyStroke();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_PREVENT_DOUBLE_CLICK, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isPreventDoubleClick();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_STACKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isStackable();
      }
    });
    putJsonProperty(new JsonProperty<IButton>(IButton.PROP_SHRINKABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isShrinkable();
      }
    });
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    IJsonAdapter<?> adapter = null;
    if (getModel().getKeyStrokeScope() != null) {
      List<?> adapterList = getUiSession().getJsonAdapters(getModel().getKeyStrokeScope());
      if (!adapterList.isEmpty()) {
        adapter = (IJsonAdapter<?>) adapterList.get(0);
      }
    }
    if (adapter != null) {
      json.put(IButton.PROP_KEY_STROKE_SCOPE, adapter.getId());
    }
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.CLICK.matches(event)) {
      getModel().getUIFacade().fireButtonClickFromUI();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IButton.PROP_SELECTED.equals(propertyName)) {
      handleUiSelectedChange(data);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }

  protected void handleUiSelectedChange(JSONObject data) {
    boolean selected = data.getBoolean(IButton.PROP_SELECTED);
    addPropertyEventFilterCondition(IButton.PROP_SELECTED, selected);
    getModel().getUIFacade().setSelectedFromUI(selected);
  }

  @Override
  public BinaryResourceHolder provideBinaryResource(String filenameWithFingerprint) {
    return BinaryResourceUrlUtility.provideBinaryResource(filenameWithFingerprint, getModel()::getAttachment);
  }
}
