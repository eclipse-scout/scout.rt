/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
