/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.form.fields.composer;

import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.composer.IComposerField;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonComposerField<COMPOSER_FIELD extends IComposerField> extends JsonFormField<COMPOSER_FIELD> {

  public JsonComposerField(COMPOSER_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ComposerField";
  }

  @Override
  protected void initJsonProperties(COMPOSER_FIELD model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<COMPOSER_FIELD>(ITreeField.PROP_TREE, model, getUiSession()) {
      @Override
      protected ITree modelValue() {
        return getModel().getTree();
      }
    });
  }
}
