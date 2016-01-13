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
package org.eclipse.scout.rt.ui.html.json.form.fields.numberfield;

import java.text.DecimalFormat;

import org.eclipse.scout.rt.client.ui.form.fields.numberfield.INumberField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonBasicField;

public class JsonNumberField<T extends INumberField> extends JsonBasicField<T> {

  public JsonNumberField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "NumberField";
  }

  @Override
  protected void handleUiDisplayTextChangedAfterTyping(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<INumberField>(INumberField.PROP_DECIMAL_FORMAT, model) {
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
