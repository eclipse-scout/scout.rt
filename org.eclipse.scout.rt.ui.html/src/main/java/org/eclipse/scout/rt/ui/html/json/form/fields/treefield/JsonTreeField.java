/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.treefield;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonTreeField<TREE_FIELD extends ITreeField> extends JsonFormField<TREE_FIELD> {

  public JsonTreeField(TREE_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "TreeField";
  }

  @Override
  protected void initJsonProperties(TREE_FIELD model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<TREE_FIELD>(ITreeField.PROP_TREE, model, getUiSession()) {
      @Override
      protected ITree modelValue() {
        return getModel().getTree();
      }
    });
  }
}
