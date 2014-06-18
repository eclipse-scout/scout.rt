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
package org.eclipse.scout.rt.ui.html.json.table.control;

import java.beans.PropertyChangeEvent;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton5;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

//FIXME same code as in JsonTableControl -> refactor to JsonAction?
public class JsonFormToolButton extends AbstractJsonPropertyObserver<IFormToolButton5> {

  public JsonFormToolButton(IFormToolButton5 model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<IFormToolButton5, String>(IFormToolButton5.PROP_TEXT, model) {
      @Override
      protected String getValueImpl(IFormToolButton5 tableControl) {
        return tableControl.getText();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton5, String>(IFormToolButton5.PROP_ICON_ID, model) {
      @Override
      protected String getValueImpl(IFormToolButton5 tableControl) {
        return tableControl.getIconId();
      }
    });

    putJsonProperty(new JsonAdapterProperty<IFormToolButton5, IForm>(IFormToolButton5.PROP_FORM, model, jsonSession) {
      @Override
      protected IForm getValueImpl(IFormToolButton5 button) {
        return button.getForm();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton5, Boolean>(IFormToolButton5.PROP_SELECTED, model) {
      @Override
      protected Boolean getValueImpl(IFormToolButton5 tableControl) {
        return tableControl.isSelected();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton5, Boolean>(IFormToolButton5.PROP_ENABLED, model) {
      @Override
      protected Boolean getValueImpl(IFormToolButton5 tableControl) {
        return tableControl.isEnabled();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton5, String>(IFormToolButton5.PROP_KEYSTROKE, model) {
      @Override
      protected String getValueImpl(IFormToolButton5 tableControl) {
        return tableControl.getKeyStroke();
      }
    });

  }

  @Override
  public String getObjectType() {
    return "ToolButton";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {
      handleUiSelected(event);
    }
  }

  protected void handleUiSelected(JsonEvent event) {
    boolean selected = JsonObjectUtility.getBoolean(event.getJsonObject(), IAction.PROP_SELECTED);

    PropertyChangeEvent propertyEvent = new PropertyChangeEvent(getModel(), IAction.PROP_SELECTED, null, selected);
    getPropertyEventFilter().addIgnorableModelEvent(propertyEvent);
    try {
      getModel().getUIFacade().setSelectedFromUI(selected);
    }
    finally {
      getPropertyEventFilter().removeIgnorableModelEvent(propertyEvent);
    }
  }

}
