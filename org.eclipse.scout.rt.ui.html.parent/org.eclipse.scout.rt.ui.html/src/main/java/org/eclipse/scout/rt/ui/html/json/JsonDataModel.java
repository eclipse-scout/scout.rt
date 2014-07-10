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
package org.eclipse.scout.rt.ui.html.json;

import java.io.IOException;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.ui.html.Activator;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDataModel extends AbstractJsonAdapter<IDataModel> {

  public JsonDataModel(IDataModel model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "DataModel";
  }

  @Override
  public JSONObject toJson() {
    try {
      //FIXME read from model
      String jsonData = new String(IOUtility.getContent(Activator.getDefault().getBundle().getResource("resources/dummy_datamodel.json").openStream()), "utf-8");
      JSONObject json = new JSONObject(jsonData);
      putProperty(json, "objectType", getObjectType());
      putProperty(json, "id", getId());
      return json;
    }
    catch (JSONException | ProcessingException | IOException e) {
      throw new JsonException(e);
    }
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

}
