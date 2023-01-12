/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.desktop;

import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.client.ui.form.IFormMenu;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;
import org.eclipse.scout.rt.ui.html.json.menu.JsonMenu;

public class JsonFormMenu<FORM_MENU extends IFormMenu<? extends IForm>> extends JsonMenu<FORM_MENU> {

  public JsonFormMenu(FORM_MENU model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "FormMenu";
  }

  @Override
  protected void initJsonProperties(FORM_MENU model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonAdapterProperty<IFormMenu<? extends IForm>>(IFormMenu.PROP_FORM, model, getUiSession()) {
      @Override
      protected IForm modelValue() {
        return getModel().getForm();
      }

      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }
    });
    putJsonProperty(new JsonProperty<IFormMenu<? extends IForm>>(IFormMenu.PROP_POPUP_CLOSABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isPopupClosable();
      }
    });
    putJsonProperty(new JsonProperty<IFormMenu<? extends IForm>>(IFormMenu.PROP_POPUP_MOVABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isPopupMovable();
      }
    });
    putJsonProperty(new JsonProperty<IFormMenu<? extends IForm>>(IFormMenu.PROP_POPUP_RESIZABLE, model) {
      @Override
      protected Object modelValue() {
        return getModel().isPopupResizable();
      }
    });
  }
}
