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
package org.eclipse.scout.rt.ui.html.json.form.fields.radiobutton;

import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

public class JsonRadioButtonGroup<RADIO_BUTTON_GROUP extends IRadioButtonGroup> extends JsonValueField<RADIO_BUTTON_GROUP> {

  private static final String FORM_FIELDS = "formFields";

  public JsonRadioButtonGroup(RADIO_BUTTON_GROUP model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButtonGroup";
  }

  @Override
  protected void initJsonProperties(RADIO_BUTTON_GROUP model) {
    super.initJsonProperties(model);
    removeJsonProperty(IValueField.PROP_DISPLAY_TEXT);
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModel().getFields());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), FORM_FIELDS, getModel().getFields());
  }
}
