/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.table.control;

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.controls.IFormTableControl;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;

public class JsonFormTableControl<TABLE_CONTROL extends IFormTableControl> extends JsonTableControl<TABLE_CONTROL> {

  public JsonFormTableControl(TABLE_CONTROL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(TABLE_CONTROL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<TABLE_CONTROL>(TABLE_CONTROL.PROP_FORM, model, getUiSession()) {
      @Override
      protected JsonAdapterPropertyConfig createConfig() {
        return JsonAdapterPropertyConfigBuilder.globalConfig();
      }

      @Override
      protected IForm modelValue() {
        return getModel().getForm();
      }

      @Override
      public boolean accept() {
        return getModel().isSelected() || getModel().getForm() == null;
      }
    });
    getJsonProperty(IAction.PROP_SELECTED).addLazyProperty(getJsonProperty(TABLE_CONTROL.PROP_FORM));
  }

  @Override
  public String getObjectType() {
    return "FormTableControl";
  }
}
