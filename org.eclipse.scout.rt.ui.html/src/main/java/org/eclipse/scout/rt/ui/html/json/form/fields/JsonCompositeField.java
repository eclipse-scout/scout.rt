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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.json.JSONObject;

public class JsonCompositeField<T extends ICompositeField, F extends IFormField> extends JsonFormField<T> {

  public JsonCompositeField(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CompositeField";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModelFields(), new DisplayableFormFieldFilter<F>());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), getModelFieldsName(), getModelFields(), new DisplayableFormFieldFilter<F>());
  }

  @SuppressWarnings("unchecked")
  protected List<F> getModelFields() {
    return (List<F>) getModel().getFields();
  }

  protected String getModelFieldsName() {
    return "fields";
  }

}
