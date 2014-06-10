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
package org.eclipse.scout.rt.ui.html.json.desktop;

import java.io.IOException;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.control.IAnalysisTableControl;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;
import org.eclipse.scout.rt.ui.html.json.Activator;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonException;
import org.eclipse.scout.rt.ui.html.json.JsonResponse;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonAnalysisTableControl extends JsonTableControl<IAnalysisTableControl> {

  public JsonAnalysisTableControl(ITableControl model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "AnalysisTableControl";
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
    if ("selected".equals(event.getType())) {
      try {
        //FIXME add to model
        String jsonData = new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/dummy_data_model.json").openStream()), "utf-8");
        getJsonSession().currentJsonResponse().addPropertyChangeEvent(getId(), "dataModel", new JSONObject(jsonData));
      }
      catch (JSONException | ProcessingException | IOException e) {
        throw new JsonException(e);
      }

      getModel().fireActivatedFromUI();
    }
  }
}
