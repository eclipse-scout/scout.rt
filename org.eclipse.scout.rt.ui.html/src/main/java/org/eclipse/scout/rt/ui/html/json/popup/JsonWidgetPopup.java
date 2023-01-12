/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.popup;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.popup.IWidgetPopup;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 9.0
 */
public class JsonWidgetPopup<T extends IWidgetPopup> extends JsonPopup<T> {

  public JsonWidgetPopup(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WidgetPopup";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IWidgetPopup.PROP_CONTENT, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getContent();
      }
    });
    putJsonProperty(new JsonProperty<T>(IWidgetPopup.PROP_CLOSABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isClosable();
      }
    });
    putJsonProperty(new JsonProperty<T>(IWidgetPopup.PROP_MOVABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isMovable();
      }
    });
    putJsonProperty(new JsonProperty<T>(IWidgetPopup.PROP_RESIZABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isResizable();
      }
    });
  }
}
