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

import org.eclipse.scout.rt.client.ui.action.IAction;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.client.ui.form.IForm;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonGlobalAdapterProperty;

public class JsonTableControl<T extends ITableControl> extends JsonAction<T> {
  protected boolean m_contentLoaded = false;

  public JsonTableControl(T model, IJsonSession jsonSession, String id, IJsonAdapter<?> parent) {
    super(model, jsonSession, id, parent);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

    putJsonProperty(new JsonProperty<ITableControl>("group", model) {
      @Override
      protected String modelValue() {
        return getModel().getGroup();
      }
    });
    putJsonProperty(new JsonGlobalAdapterProperty<ITableControl>(ITableControl.PROP_FORM, model, getJsonSession()) {
      @Override
      protected IForm modelValue() {
        return getModel().getForm();
      }

      @Override
      public boolean accept() {
        return getModel().isSelected();
      }
    });
    getJsonProperty(IAction.PROP_SELECTED).addSlaveProperty(getJsonProperty(ITableControl.PROP_FORM));
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
