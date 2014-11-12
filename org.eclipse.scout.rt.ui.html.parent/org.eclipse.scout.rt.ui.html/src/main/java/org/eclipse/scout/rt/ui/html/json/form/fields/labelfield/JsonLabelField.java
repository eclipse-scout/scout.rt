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
package org.eclipse.scout.rt.ui.html.json.form.fields.labelfield;

import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonLabelField extends JsonValueField<ILabelField> {

  public JsonLabelField(ILabelField model, IJsonSession session, String id) {
    super(model, session, id);
  }

  @Override
  public String getObjectType() {
    return "LabelField";
  }

  @Override
  protected void initJsonProperties(ILabelField model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<ILabelField>(ILabelField.PROP_WRAP_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isWrapText();
      }
    });
    putJsonProperty(new JsonProperty<ILabelField>(ILabelField.PROP_SELECTABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelectable();
      }
    });
  }
}
