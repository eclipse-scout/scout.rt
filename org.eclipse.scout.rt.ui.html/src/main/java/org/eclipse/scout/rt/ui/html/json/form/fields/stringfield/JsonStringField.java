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
        return getModel().isUpdateDisplayTextOnModify(); // FIXME ASA: remove parameter whileTyping?
      }
    });
    putJsonProperty(new JsonProperty<IStringField>(IStringField.PROP_INPUT_MASKED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isInputMasked();
      }
    });
  }

  // FIXME AWE: (display-text) rename to handleUiValueChanged after renaming in Scout RT
  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().setTextFromUI(displayText, false);
  }

  @Override
  protected void handleUiDisplayTextChangedImpl(String displayText) {
    getModel().getUIFacade().setDisplayTextFromUI(displayText);
  }

}
