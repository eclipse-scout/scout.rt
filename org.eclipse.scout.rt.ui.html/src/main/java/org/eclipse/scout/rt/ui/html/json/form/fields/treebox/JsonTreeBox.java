/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.treebox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.treebox.ITreeBox;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;
import org.json.JSONObject;

public class JsonTreeBox<TREE_BOX extends ITreeBox<?>> extends JsonFormField<TREE_BOX> {

  public JsonTreeBox(TREE_BOX model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TreeBox";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getTree());
    attachAdapter(getTreeBoxFilterBoxModel());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "tree", getModel().getTree());
    putAdapterIdProperty(json, "filterBox", getTreeBoxFilterBoxModel());
    return json;
  }

  protected IFormField getTreeBoxFilterBoxModel() {
    List<IFormField> childFields = getModel().getFields();
    if (CollectionUtility.hasElements(childFields)) {
      return CollectionUtility.firstElement(childFields);
    }
    return null;
  }
}
