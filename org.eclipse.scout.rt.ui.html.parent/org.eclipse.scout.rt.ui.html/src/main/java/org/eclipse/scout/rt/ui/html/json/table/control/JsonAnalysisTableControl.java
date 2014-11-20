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

import org.eclipse.scout.rt.client.ui.basic.table.control.IAnalysisTableControl;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.json.JSONObject;

public class JsonAnalysisTableControl<T extends IAnalysisTableControl> extends JsonTableControl<T> {

  public JsonAnalysisTableControl(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  protected void createChildAdapters() {
    super.createChildAdapters();
    attachAdapter(getModel().getDataModel());
  }

  @Override
  protected void disposeChildAdapters() {
    super.disposeChildAdapters();
    disposeAdapter(getModel().getDataModel());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isSelected()) {
      putProperty(json, "rootEntityRef", "e140"); //FIXME CGU
      putProperty(json, IAnalysisTableControl.PROP_DATA_MODEL, getAdapterIdForModel(getModel().getDataModel()));
      m_contentLoaded = true;
    }
    return json;
  }

  @Override
  public String getObjectType() {
    return "AnalysisTableControl";
  }

  @Override
  protected void handleUiLoadContent() {
    addPropertyChangeEvent("rootEntityRef", "e140"); // FIXME CGU
    addPropertyDataModelChangeEvent();
  }

  private void addPropertyDataModelChangeEvent() {
    IJsonAdapter<?> dataModelAdapter = attachAdapter(getModel().getDataModel());
    addPropertyChangeEvent(IAnalysisTableControl.PROP_DATA_MODEL, dataModelAdapter.getId());
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (IAnalysisTableControl.PROP_DATA_MODEL.equals(propertyName) && m_contentLoaded) {
      addPropertyDataModelChangeEvent();
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }
}
