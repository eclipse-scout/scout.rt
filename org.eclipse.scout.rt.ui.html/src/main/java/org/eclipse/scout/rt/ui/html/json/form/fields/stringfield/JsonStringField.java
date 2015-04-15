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
package org.eclipse.scout.rt.ui.html.json.form.fields.stringfield;

import org.eclipse.scout.rt.client.ui.form.fields.stringfield.IStringField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonStringField<T extends IStringField> extends JsonValueField<T> {

  public JsonStringField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "StringField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_MULTILINE_TEXT, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMultilineText();
      }
    });
    // The JSON layer uses the same property for both cases
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUpdateDisplayTextOnModify() || getModel().isValidateOnAnyKey();
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_INPUT_MASKED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInputMasked();
      }
    });
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText, boolean whileTyping) {
    if (getModel().isUpdateDisplayTextOnModify()) {
      getModel().getUIFacade().setDisplayTextFromUI(displayText);
    }
    // Note: the display text changed event is also sent, when a field loses focus. setTextFromUI
    // also sets the value of the field, setDisplayTextFromUI does not.
    getModel().getUIFacade().setTextFromUI(displayText, whileTyping);
  }
}
