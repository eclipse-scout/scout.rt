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
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.JsonAction;
import org.json.JSONObject;

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
  }

  @Override
  public String getObjectType() {
    return "TableControl";
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    //FIXME CGU create property
    attachGlobalAdapter(getModel().getForm());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isSelected()) {
      putAdapterIdProperty(json, ITableControl.PROP_FORM, getModel().getForm());
      m_contentLoaded = true;
    }
    return json;
  }

  @Override
  protected void handleUiSelected(JsonEvent event) {
    super.handleUiSelected(event);

    // Lazy loading content on selection.
    if (getModel().isSelected() && !m_contentLoaded) {
      handleUiLoadContent();
      m_contentLoaded = true;
    }
  }

  protected void handleUiLoadContent() {
    addPropertyFormChangeEvent();
  }

  private void addPropertyFormChangeEvent() {
    String formId = null;
    if (getModel().getForm() != null) {
      IJsonAdapter<?> formAdapter = attachGlobalAdapter(getModel().getForm());
      formId = formAdapter.getId();
    }
    addPropertyChangeEvent(ITableControl.PROP_FORM, formId);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (ITableControl.PROP_FORM.equals(propertyName) && m_contentLoaded) {
      addPropertyFormChangeEvent();
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }

}
