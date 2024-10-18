/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.toggleswitch;

import org.eclipse.scout.rt.client.ui.toggleswitch.IToggleSwitch;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonWidget;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.json.JSONObject;

public class JsonToggleSwitch<T extends IToggleSwitch> extends AbstractJsonWidget<T> {

  public JsonToggleSwitch(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Switch";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_ACTIVATED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isActivated();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_LABEL_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isLabelHtmlEnabled();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_LABEL_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().getLabelVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_ICON_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isIconVisible();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_DISPLAY_STYLE, model) {
      @Override
      protected String modelValue() {
        return getModel().getDisplayStyle();
      }
    });
    putJsonProperty(new JsonProperty<>(IToggleSwitch.PROP_TABBABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTabbable();
      }
    });
  }

  @Override
  protected void handleUiPropertyChange(String propertyName, JSONObject data) {
    if (IToggleSwitch.PROP_ACTIVATED.equals(propertyName)) {
      boolean activated = data.getBoolean(propertyName);
      addPropertyEventFilterCondition(propertyName, activated);
      getModel().getUIFacade().setActivatedFromUI(activated);
    }
    else {
      super.handleUiPropertyChange(propertyName, data);
    }
  }
}
