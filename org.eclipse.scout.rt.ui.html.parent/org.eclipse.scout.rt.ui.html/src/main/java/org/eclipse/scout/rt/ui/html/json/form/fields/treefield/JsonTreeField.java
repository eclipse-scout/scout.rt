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
package org.eclipse.scout.rt.ui.html.json.form.fields.treefield;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonTreeField extends JsonFormField<ITreeField> {

  public JsonTreeField(ITreeField model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  protected void initProperties(ITreeField model) {
    super.initProperties(model);

    putJsonProperty(new JsonAdapterProperty<ITreeField>(ITreeField.PROP_TREE, model, getJsonSession()) {
      @Override
      protected ITree modelValue() {
        return getModel().getTree();
      }
    });
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapter(getModel().getTree());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAdapter(getModel().getTree());
  }

  @Override
  public String getObjectType() {
    return "TreeField";
  }

}
