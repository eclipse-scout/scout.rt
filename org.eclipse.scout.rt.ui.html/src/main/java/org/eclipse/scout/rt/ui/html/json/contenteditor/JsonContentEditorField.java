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
package org.eclipse.scout.rt.ui.html.json.contenteditor;

import org.eclipse.scout.rt.client.ui.contenteditor.IContentEditorField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonContentEditorField extends JsonFormField<IContentEditorField> {

  public JsonContentEditorField(IContentEditorField model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ContentEditorField";
  }

  @Override
  protected void initJsonProperties(IContentEditorField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IContentEditorField>(IContentEditorField.PROP_CONTENT, model) {
      @Override
      protected String modelValue() {
        return getModel().getContent();
      }
    });
  }
}
