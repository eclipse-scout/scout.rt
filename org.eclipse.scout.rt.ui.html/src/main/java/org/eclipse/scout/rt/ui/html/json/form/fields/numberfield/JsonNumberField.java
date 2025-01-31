/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.numberfield;

import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonBasicField;

public class JsonNumberField<T extends INumberField<? extends Number>> extends JsonBasicField<T> {

  public JsonNumberField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "NumberField";
  }

  @Override
  protected void handleUiAcceptInputWhileTyping(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

  @Override
  protected void handleUiAcceptInputAfterTyping(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<INumberField<?>>(INumberField.PROP_DECIMAL_FORMAT, model) {
      @Override
      protected Object modelValue() {
        return getModel().getFormat();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        DecimalFormat format = (DecimalFormat) value;
        return MainJsonObjectFactory.get().createJsonObject(format).toJson();
      }
    });
  }
}
