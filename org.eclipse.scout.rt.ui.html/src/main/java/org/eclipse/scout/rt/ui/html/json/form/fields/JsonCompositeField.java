/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.json.JSONObject;

public class JsonCompositeField<COMPOSITE_FIELD extends ICompositeField, F extends IFormField> extends JsonFormField<COMPOSITE_FIELD> {

  public JsonCompositeField(COMPOSITE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "CompositeField";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapters(getModelFields(), new DisplayableFormFieldFilter<>());
  }

  @Override
  public JSONObject toJson() {
    return putAdapterIdsProperty(super.toJson(), getModelFieldsPropertyName(), getModelFields(), new DisplayableFormFieldFilter<>());
  }

  @SuppressWarnings("unchecked")
  protected List<F> getModelFields() {
    return (List<F>) getModel().getFields();
  }

  protected String getModelFieldsPropertyName() {
    return "fields";
  }

}
