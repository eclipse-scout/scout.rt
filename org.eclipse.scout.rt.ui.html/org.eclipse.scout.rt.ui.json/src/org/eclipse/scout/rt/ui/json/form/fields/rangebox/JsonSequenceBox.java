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
package org.eclipse.scout.rt.ui.json.form.fields.rangebox;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.sequencebox.ISequenceBox;
import org.eclipse.scout.rt.ui.json.IJsonSession;
import org.eclipse.scout.rt.ui.json.form.fields.IJsonFormField;
import org.eclipse.scout.rt.ui.json.form.fields.JsonFormField;
import org.json.JSONObject;

/**
 * This class creates JSON output for an <code>ISequenceBox</code>.
 * 
 * @author awe
 */
public class JsonSequenceBox extends JsonFormField<ISequenceBox> {

  private List<IJsonFormField<?>> m_jsonFormFields = new ArrayList<>();

  public JsonSequenceBox(ISequenceBox model, IJsonSession session) {
    super(model, session);
  }

  @Override
  public String getObjectType() {
    return "SequenceBox";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    m_jsonFormFields = toJsonFormField(getModelObject().getFields());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putFormFields(json, m_jsonFormFields);
    return json;
  }

}
