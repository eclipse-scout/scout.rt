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
package org.eclipse.scout.rt.ui.html.json.action;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;

public class JsonAction<T extends IAction> extends AbstractJsonPropertyObserver<T> {
  public static final String EVENT_DO_ACTION = "doAction";

  public JsonAction(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "Action";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<T>(IAction.PROP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getText();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });

    putJsonProperty(new JsonProperty<T>(IAction.PROP_KEYSTROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getKeyStroke();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_DO_ACTION.equals(event.getType())) {
      handleUiDoAction(event, res);
    }
    else if (IAction.PROP_SELECTED.equals(event.getType())) {
      handleUiSelected(event);
    }
  }

  public void handleUiDoAction(JsonEvent event, JsonResponse res) {
    getModel().getUIFacade().fireActionFromUI();
  }

  protected void handleUiSelected(JsonEvent event) {
    boolean selected = JsonObjectUtility.getBoolean(event.getData(), IAction.PROP_SELECTED);
    addPropertyEventFilterCondition(IAction.PROP_SELECTED, selected);
    getModel().getUIFacade().setSelectedFromUI(selected);
  }

}
