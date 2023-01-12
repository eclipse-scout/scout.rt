/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.breadcrumbbarfield;

import org.eclipse.scout.rt.client.ui.basic.breadcrumbbar.IBreadcrumbBar;
import org.eclipse.scout.rt.client.ui.form.fields.breadcrumbbarfield.IBreadcrumbBarField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonFormField;

public class JsonBreadcrumbBarField<T extends IBreadcrumbBarField> extends JsonFormField<T> {

  public JsonBreadcrumbBarField(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "BreadcrumbBarField";
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<T>(IBreadcrumbBarField.PROP_BREADCRUMB_BAR, model, getUiSession()) {
      @Override
      protected IBreadcrumbBar modelValue() {
        return getModel().getBreadcrumbBar();
      }

      @Override
      public String jsonPropertyName() {
        return "breadcrumbBar";
      }
    });
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getBreadcrumbBar());
  }
}
