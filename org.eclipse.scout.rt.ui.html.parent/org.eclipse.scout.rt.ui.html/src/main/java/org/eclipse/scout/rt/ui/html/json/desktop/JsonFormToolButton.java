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
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.outline.IFormToolButton2;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

//FIXME same code as in JsonTableControl -> refactor to JsonAction?
public class JsonFormToolButton extends AbstractJsonPropertyObserver<IFormToolButton2> {

  public JsonFormToolButton(IFormToolButton2 model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<IFormToolButton2, String>(IFormToolButton2.PROP_TEXT, model) {
      @Override
      protected String getValueImpl(IFormToolButton2 tableControl) {
        return tableControl.getText();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton2, String>(IFormToolButton2.PROP_ICON_ID, model) {
      @Override
      protected String getValueImpl(IFormToolButton2 tableControl) {
        return tableControl.getIconId();
      }
    });

    putJsonProperty(new JsonAdapterProperty<IFormToolButton2, IForm>(IFormToolButton2.PROP_FORM, model, jsonSession) {
      @Override
      protected IForm getValueImpl(IFormToolButton2 button) {
        return button.getForm();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton2, Boolean>(IFormToolButton2.PROP_SELECTED, model) {
      @Override
      protected Boolean getValueImpl(IFormToolButton2 tableControl) {
        return tableControl.isSelected();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton2, Boolean>(IFormToolButton2.PROP_ENABLED, model) {
      @Override
      protected Boolean getValueImpl(IFormToolButton2 tableControl) {
        return tableControl.isEnabled();
      }
    });

    putJsonProperty(new JsonProperty<IFormToolButton2, String>(IFormToolButton2.PROP_KEYSTROKE, model) {
      @Override
      protected String getValueImpl(IFormToolButton2 tableControl) {
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
      getModel().getUIFacade().fireActionFromUI();
    }
  }

}
