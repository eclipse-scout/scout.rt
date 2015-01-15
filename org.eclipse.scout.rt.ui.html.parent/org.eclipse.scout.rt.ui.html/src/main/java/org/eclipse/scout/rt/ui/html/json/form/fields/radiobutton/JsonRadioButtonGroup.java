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

import org.eclipse.scout.rt.client.ui.form.fields.radiobuttongroup.IRadioButtonGroup;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;
import org.json.JSONObject;

/**
 *
 */
public class JsonRadioButtonGroup<T extends IRadioButtonGroup> extends JsonValueField<T> {

  private static final String FORM_FIELDS = "formFields";

  /**
   * @param model
   * @param jsonSession
   * @param id
   * @param parent
   */
  public JsonRadioButtonGroup(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "RadioButtonGroup";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IRadioButtonGroup>(IRadioButtonGroup.PROP_VALUE, model) {
      @Override
      protected Object modelValue() {
        return getModel().getValue();
      }
    });

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
