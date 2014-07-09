/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.control;

import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;

public class JsonTableControl<T extends ITableControl> extends AbstractJsonPropertyObserver<T> {

  public JsonTableControl(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

    putJsonProperty(new JsonProperty<ITableControl>(ITableControl.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });

    putJsonProperty(new JsonProperty<ITableControl>("cssClass", model) {
      @Override
      protected String modelValue() {
        return getModel().getCssClass();
      }
    });

    putJsonProperty(new JsonProperty<ITableControl>("group", model) {
      @Override
      protected String modelValue() {
        return getModel().getGroup();
      }
    });

    putJsonProperty(new JsonAdapterProperty<ITableControl>(ITableControl.PROP_FORM, model, jsonSession) {
      @Override
      protected IForm modelValue() {
        return getModel().getForm();
      }
    });

    putJsonProperty(new JsonProperty<ITableControl>(ITableControl.PROP_SELECTED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isSelected();
      }
    });

    putJsonProperty(new JsonProperty<ITableControl>(ITableControl.PROP_ENABLED, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isEnabled();
      }
    });

  }

  @Override
  public String getObjectType() {
    return "TableControl";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {
      getModel().fireActivatedFromUI();
    }
  }

}
