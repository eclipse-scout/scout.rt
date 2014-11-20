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
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.json.JSONObject;

public class JsonTableControl<T extends ITableControl> extends AbstractJsonPropertyObserver<T> {
  protected boolean m_contentLoaded = false;

  public JsonTableControl(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void initJsonProperties(T model) {
    super.initJsonProperties(model);

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
  protected void createChildAdapters() {
    super.createChildAdapters();
    optAttachAdapter(getModel().getForm());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    optDisposeAdapter(getModel().getForm());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isSelected()) {
      optPutAdapterIdProperty(json, ITableControl.PROP_FORM, getModel().getForm());
      m_contentLoaded = true;
    }
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {
      // Lazy loading content on selection. FIXME CGU Should this be controlled by the model?
      if (!getModel().isSelected() && !m_contentLoaded) {
        handleUiLoadContent();
        m_contentLoaded = true;
      }
      getModel().fireActivatedFromUI();
    }
  }

  protected void handleUiLoadContent() {
    addPropertyFormChangeEvent();
  }

  private void addPropertyFormChangeEvent() {
    String formId = null;
    if (getModel().getForm() != null) {
      IJsonAdapter<?> formAdapter = attachAdapter(getModel().getForm());
      formId = formAdapter.getId();
    }
    addPropertyChangeEvent(ITableControl.PROP_FORM, formId);
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (ITableControl.PROP_FORM.equals(propertyName) && m_contentLoaded) {
      addPropertyFormChangeEvent();
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }

}
