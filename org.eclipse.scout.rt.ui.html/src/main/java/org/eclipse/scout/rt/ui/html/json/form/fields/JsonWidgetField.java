/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.IWidgetField;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;

public class JsonWidgetField<WIDGET_FIELD extends IWidgetField<? extends IWidget>> extends JsonFormField<WIDGET_FIELD> {

  public JsonWidgetField(WIDGET_FIELD model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "WidgetField";
  }

  @Override
  protected void initJsonProperties(WIDGET_FIELD model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<IWidgetField>(IWidgetField.PROP_SCROLLABLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isScrollable();
      }
    });
    putJsonProperty(new JsonAdapterProperty<WIDGET_FIELD>(IWidgetField.PROP_FIELD_WIDGET, model, getUiSession()) {
      @Override
      protected IWidget modelValue() {
        return getModel().getFieldWidget();
      }
    });
  }
}
