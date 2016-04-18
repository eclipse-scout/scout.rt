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
package org.eclipse.scout.rt.ui.html.json.action;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.res.BinaryResourceUrlUtility;

public abstract class JsonAction<ACTION extends IAction> extends AbstractJsonPropertyObserver<ACTION> {
  public static final String EVENT_DO_ACTION = "doAction";

  public JsonAction(ACTION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "Action";
  }

  @Override
  protected void initJsonProperties(ACTION model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getText();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_ICON_ID, model) {
      @Override
      protected String modelValue() {
        return getModel().getIconId();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        return BinaryResourceUrlUtility.createIconUrl((String) value);
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_TOOLTIP_TEXT, model) {
      @Override
      protected String modelValue() {
        return getModel().getTooltipText();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>("toggleAction", model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isToggleAction();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isVisible();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_KEY_STROKE, model) {
      @Override
      protected String modelValue() {
        return getModel().getKeyStroke();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_HORIZONTAL_ALIGNMENT, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getHorizontalAlignment();
      }
    });

    putJsonProperty(new JsonProperty<ACTION>(IAction.PROP_CSS_CLASS, model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });

    putJsonProperty(new JsonAdapterProperty<ACTION>(IActionNode.PROP_CHILD_ACTIONS, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return new JsonAdapterPropertyConfigBuilder().filter(new DisplayableActionFilter<IAction>()).build();
      }

      @Override
      protected Object modelValue() {
        if (getModel() instanceof IActionNode) {
          return ((IActionNode) getModel()).getChildActions();
        }
        return null;
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_DO_ACTION.equals(event.getType())) {
      handleUiDoAction(event);
    }
    else if (IAction.PROP_SELECTED.equals(event.getType())) {
      handleUiSelected(event);
    }
  }

  protected void handleUiDoAction(JsonEvent event) {
    getModel().getUIFacade().fireActionFromUI();
  }

  protected void handleUiSelected(JsonEvent event) {
    boolean selected = event.getData().getBoolean(IAction.PROP_SELECTED);
    addPropertyEventFilterCondition(IAction.PROP_SELECTED, selected);
    getModel().getUIFacade().setSelectedFromUI(selected);
  }

}
