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
package org.eclipse.scout.rt.ui.html.json.form.fields.filechooserfield;

import org.eclipse.scout.rt.client.ui.form.fields.filechooserfield.IFileChooserField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonFileChooserField<T extends IFileChooserField> extends JsonValueField<T> {

  public static final String EVENT_CHOOSE_FILE = "chooseFile";

  public JsonFileChooserField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FileChooserField";
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_CHOOSE_FILE.equals(event.getType())) {
      handleUiChooseFile();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  @Override
  protected void handleUiTextChangedImpl(String displayText) {
    getModel().getUIFacade().parseAndSetValueFromUI(displayText);
  }

  private void handleUiChooseFile() {
    getModel().getUIFacade().startFileChooserFromUI();
  }
}
