/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
