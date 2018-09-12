/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutlineTileField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonOutlineTileField extends JsonFormField<IOutlineTileField> {

  public JsonOutlineTileField(IOutlineTileField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "OutlineTileField";
  }

  @Override
  protected void initJsonProperties(IOutlineTileField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<IOutlineTileField>(IOutlineTileField.PROP_OUTLINE, model, getUiSession()) {
      @Override
      protected IOutline modelValue() {
        return getModel().getOutline();
      }
    });
  }

}
