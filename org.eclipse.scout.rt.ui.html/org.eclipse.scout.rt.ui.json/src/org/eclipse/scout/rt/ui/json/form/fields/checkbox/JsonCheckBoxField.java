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
package org.eclipse.scout.rt.ui.json.form.fields.checkbox;

import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.JsonEvent;
import org.eclipse.scout.rt.ui.json.JsonEventType;
import org.eclipse.scout.rt.ui.json.JsonResponse;
import org.eclipse.scout.rt.ui.json.form.fields.JsonValueField;
import org.json.JSONObject;

/**
 * This class creates JSON output for an IBooleanField used as a check-box.
 * 
 * @author awe
 */
public class JsonCheckBoxField extends JsonValueField<IBooleanField> {

  public static final String PROP_CHECKED = "checked";

  public JsonCheckBoxField(IBooleanField model, IJsonSession session) {
    super(model, session);
  }

  @Override
  public String getObjectType() {
    return "CheckBoxField";
  }

  @Override
  public JSONObject toJson() {
    return putProperty(super.toJson(), PROP_CHECKED, getModelObject().isChecked());
  }

  @Override
  protected void handleModelPropertyChange(String name, Object newValue) {
    super.handleModelPropertyChange(name, newValue);
    // TODO AWE: (ask C.GU) doch besser value verwenden, weil das BooleanField checked intern doch auf value mapped
    // so ist es ein bisschen hackig
    if (IBooleanField.PROP_VALUE.matches(name)) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), PROP_CHECKED, newValue);
    }
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
    boolean oldChecked = getModelObject().isChecked();
    getModelObject().setChecked(!oldChecked);
  }

  // TODO AWE: unit test JsonCheckBoxField, Jasmine

}
