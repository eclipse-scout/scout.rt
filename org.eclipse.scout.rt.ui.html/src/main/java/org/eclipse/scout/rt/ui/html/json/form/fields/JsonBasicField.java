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
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.services.common.clipboard.IClipboardService;
import org.eclipse.scout.rt.client.ui.form.fields.IBasicField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonBasicField<T extends IBasicField<?>> extends JsonValueField<T> {

  public static final String EVENT_EXPORT_TO_CLIPBOARD = "exportToClipboard";

  public JsonBasicField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IBasicField>(IBasicField.PROP_UPDATE_DISPLAY_TEXT_ON_MODIFY, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isUpdateDisplayTextOnModify();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_EXPORT_TO_CLIPBOARD.equals(event.getType())) {
      handleUiExportToClipboard();
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiExportToClipboard() {
    try {
      BEANS.get(IClipboardService.class).setTextContents(getModel().getDisplayText());
    }
    catch (ProcessingException e) {
      BEANS.get(ExceptionHandler.class).handle(e);
    }
  }

}
