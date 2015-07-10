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
package org.eclipse.scout.rt.ui.html.json.form.fields.numberfield;

import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonNumberField<NUMBER_FIELD extends INumberField> extends JsonValueField<NUMBER_FIELD> {

  public JsonNumberField(NUMBER_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "NumberField";
  }

  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  @Override
  protected void initJsonProperties(NUMBER_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<INumberField>(INumberField.PROP_DECIMAL_FORMAT, model) {
      @Override
      protected String modelValue() {
        DecimalFormat format = getModel().getFormat();
        return format != null && format instanceof DecimalFormat ? ((DecimalFormat) format).toPattern() : "";
      }
    });
  }

}
