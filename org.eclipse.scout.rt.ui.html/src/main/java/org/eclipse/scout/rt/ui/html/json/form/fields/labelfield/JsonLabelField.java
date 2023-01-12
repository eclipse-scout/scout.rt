/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.labelfield;

import org.eclipse.scout.rt.client.ui.IHtmlCapable;
import org.eclipse.scout.rt.client.ui.form.fields.labelfield.ILabelField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonEventType;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonValueField;

public class JsonLabelField<LABEL_FIELD extends ILabelField> extends JsonValueField<LABEL_FIELD> {

  public JsonLabelField(LABEL_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "LabelField";
  }

  @Override
  protected void initJsonProperties(LABEL_FIELD model) {
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
    putJsonProperty(new JsonProperty<ILabelField>(IHtmlCapable.PROP_HTML_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHtmlEnabled();
      }
    });
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (JsonEventType.APP_LINK_ACTION.matches(event.getType())) {
      handleUiAppLinkAction(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiAppLinkAction(JsonEvent event) {
    String ref = event.getData().optString("ref", null);
    getModel().getUIFacade().fireAppLinkActionFromUI(ref);
  }
}
