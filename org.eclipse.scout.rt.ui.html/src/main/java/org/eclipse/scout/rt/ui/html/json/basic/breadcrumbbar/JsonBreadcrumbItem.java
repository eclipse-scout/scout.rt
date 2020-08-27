/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
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
