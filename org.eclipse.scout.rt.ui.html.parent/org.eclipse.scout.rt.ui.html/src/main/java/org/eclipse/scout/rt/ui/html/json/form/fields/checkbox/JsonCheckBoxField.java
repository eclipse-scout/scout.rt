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
package org.eclipse.scout.rt.ui.html.json.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

/**
 * This class creates JSON output for an IBooleanField used as a check-box.
 * 
 * @author awe
 */
public class JsonCheckBoxField extends JsonValueField<IBooleanField> {

  public JsonCheckBoxField(IBooleanField model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "CheckBoxField";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if (JsonEventType.CLICK.matches(event)) {
      handleUiClick(event, res);
    }
    else {
      throw new IllegalArgumentException("unsupported event type");
    }
  }

  private void handleUiClick(JsonEvent event, JsonResponse res) {
    boolean uiChecked = !getModelObject().isChecked();
    getModelObject().setChecked(uiChecked);
    boolean modelChecked = getModelObject().isChecked();
    /* In some cases the widget in the UI is clicked, which causes the check-box to be de-/selected, but the model rejects the value-change.
     * in that case we must "revert" the click in the UI, so that UI and model are in-sync again. This may happen, when the model-field throws
     * a VetoExeception in its execValidateValue() method.
     */
    if (uiChecked != modelChecked) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), IValueField.PROP_VALUE, modelChecked);
    }
  }

}
