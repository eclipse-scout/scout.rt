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
import org.eclipse.scout.rt.client.ui.basic.table.controls.ITableControl;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterProperty;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfig;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonAdapterPropertyConfigBuilder;

public class JsonTableControl<TABLE_CONTROL extends ITableControl> extends JsonAction<TABLE_CONTROL> {
  protected boolean m_contentLoaded = false;

  public JsonTableControl(TABLE_CONTROL model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  protected void initJsonProperties(TABLE_CONTROL model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonAdapterProperty<ITableControl>(ITableControl.PROP_FORM, model, getUiSession()) {
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
        return getModel().isSelected();
      }
    });
    getJsonProperty(IAction.PROP_SELECTED).addLazyProperty(getJsonProperty(ITableControl.PROP_FORM));
  }

  @Override
  public String getObjectType() {
    return "TableControl";
  }

  @Override
  protected void handleUiSelected(JsonEvent event) {
    super.handleUiSelected(event);

    // Lazy loading content on selection (temporary used for subclasses until subclasses are finally implemented)
    if (getModel().isSelected() && !m_contentLoaded) {
      handleUiLoadContent();
      m_contentLoaded = true;
    }
  }

  protected void handleUiLoadContent() {
  }

}
