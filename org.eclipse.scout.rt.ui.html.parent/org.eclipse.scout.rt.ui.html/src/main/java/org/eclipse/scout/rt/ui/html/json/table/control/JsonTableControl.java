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
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.eclipse.scout.rt.ui.html.json.form.fields.JsonProperty;
import org.json.JSONObject;

public class JsonTableControl<T extends ITableControl> extends AbstractJsonPropertyObserver<T> {
  protected boolean m_contentLoaded = false;

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
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isSelected()) {
      putProperty(json, ITableControl.PROP_FORM, getOrCreateJsonAdapter(getModel().getForm()));
      m_contentLoaded = true;
    }
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {

      //Lazy loading content on selection. FIXME CGU Should this be controlled by the model?
      if (!getModel().isSelected() && !m_contentLoaded) {
        handleUiLoadContent();
        m_contentLoaded = true;
      }
      getModel().fireActivatedFromUI();
    }
  }

  protected void handleUiLoadContent() {
    getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), ITableControl.PROP_FORM, getOrCreateJsonAdapter(getModel().getForm()));
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (ITableControl.PROP_FORM.equals(propertyName) && m_contentLoaded) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), ITableControl.PROP_FORM, getOrCreateJsonAdapter(getModel().getForm()));
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }

}
