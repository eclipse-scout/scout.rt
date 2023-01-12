/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.basic.breadcrumbbar;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbItem;
import org.eclipse.scout.rt.client.ui.form.fields.mode.IMode;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;

public class JsonBreadcrumbItem<BC extends IBreadcrumbItem> extends JsonAction<BC> {

  public JsonBreadcrumbItem(BC model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BreadcrumbItem";
  }

  @Override
  protected void initJsonProperties(BC model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<BC>(IMode.PROP_REF, model) {
      @Override
      protected Object modelValue() {
        return getModel().getRef();
      }
    });
  }
}
