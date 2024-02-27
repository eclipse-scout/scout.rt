/*
 * Copyright (c) 2010-2024 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.smartfield;

import org.eclipse.scout.rt.client.ui.form.fields.smartfield.IProposalField;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonProposalField<VALUE, MODEL extends IProposalField<VALUE>> extends JsonSmartField<VALUE, MODEL> {

  public JsonProposalField(MODEL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "ProposalField";
  }

  @Override
  protected void initJsonProperties(MODEL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IProposalField<VALUE>>(IProposalField.PROP_TRIM_TEXT_ON_VALIDATE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isTrimText();
      }
    });
  }

  @Override
  protected Object jsonToValue(Object jsonValue) {
    return jsonValue; // simply return the string
  }

  @Override
  protected Object valueToJson(VALUE value) {
    if (value == null) {
      return null;
    }
    Assertions.assertInstance(value, String.class);
    return value;
  }

  @Override
  protected void setValueFromUI(Object value) {
    getModel().getUIFacade().setValueAsStringFromUI((String) value);
  }
}
