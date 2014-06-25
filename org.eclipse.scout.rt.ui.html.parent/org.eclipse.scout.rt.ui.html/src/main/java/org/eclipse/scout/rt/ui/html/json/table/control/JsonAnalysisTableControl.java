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
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;

public class JsonAnalysisTableControl extends JsonTableControl<IAnalysisTableControl> {

  public JsonAnalysisTableControl(IAnalysisTableControl model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);

//    putJsonProperty(new JsonAdapterProperty<IAnalysisTableControl, IDataModel>(IAnalysisTableControl.PROP_DATA_MODEL, model, jsonSession) {
//      @Override
//      protected IDataModel getValueImpl(IAnalysisTableControl tableControl) {
//        return tableControl.getDataModel();
//      }
//    });
  }

  @Override
  public String getObjectType() {
    return "AnalysisTableControl";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {

      if (!getModel().isSelected()) {
        //Lazy loading on selection
        getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), IAnalysisTableControl.PROP_DATA_MODEL, modelToJson(getModel().getDataModel()));
        getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), "rootEntityRef", "e140"); //FIXME CGU
      }

      getModel().fireActivatedFromUI();
    }
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object newValue) {
    if (IAnalysisTableControl.PROP_DATA_MODEL.equals(propertyName)) {
      getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), IAnalysisTableControl.PROP_DATA_MODEL, modelToJson(getModel().getDataModel()));
    }
    else {
      super.handleModelPropertyChange(propertyName, newValue);
    }
  }
}
