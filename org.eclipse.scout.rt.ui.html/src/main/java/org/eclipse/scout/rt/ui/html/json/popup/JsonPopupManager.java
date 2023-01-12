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

import java.util.Set;

import org.eclipse.scout.rt.client.ui.popup.IPopup;
import org.eclipse.scout.rt.client.ui.popup.PopupManager;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;

/**
 * @since 9.0
 */
public class JsonPopupManager<T extends PopupManager> extends AbstractJsonPropertyObserver<T> {

  public JsonPopupManager(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "PopupManager";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(PopupManager.PROP_POPUPS, model, getUiSession()) {
      @Override
      protected Set<IPopup> modelValue() {
        return getModel().getPopups();
      }
    });
  }
}
