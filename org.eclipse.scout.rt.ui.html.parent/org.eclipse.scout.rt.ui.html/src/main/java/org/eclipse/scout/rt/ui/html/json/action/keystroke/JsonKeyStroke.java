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
package org.eclipse.scout.rt.ui.html.json.action.keystroke;

import org.eclipse.scout.rt.client.ui.action.keystroke.IKeyStroke;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

// TODO BSH Combine with JsonMenu --> JsonAction?
public class JsonKeyStroke extends AbstractJsonPropertyObserver<IKeyStroke> {

  public static final String EVENT_KEYSTROKE_ACTION = "keystrokeAction";

  public JsonKeyStroke(IKeyStroke model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<IKeyStroke, Boolean>(IKeyStroke.PROP_ENABLED, model) {
      @Override
      protected Boolean getValueImpl(IKeyStroke keystroke) {
        return keystroke.isEnabled();
      }
    });
    putJsonProperty(new JsonProperty<IKeyStroke, String>(IKeyStroke.PROP_KEYSTROKE, model) {
      @Override
      protected String getValueImpl(IKeyStroke keystroke) {
        return keystroke.getKeyStroke();
      }
    });
  }

  @Override
  public String getObjectType() {
    return "KeyStroke";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (EVENT_KEYSTROKE_ACTION.equals(event.getType())) {
      handleUiKeyStrokeAction(event, res);
    }
  }

  public void handleUiKeyStrokeAction(JsonEvent event, JsonResponse res) {
    getModel().getUIFacade().fireActionFromUI();
  }
}
