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
import java.net.URL;

import org.eclipse.scout.commons.IOUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.model.IDataModel;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.ResourceBase;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonDataModel<T extends IDataModel> extends AbstractJsonAdapter<T> {

  public JsonDataModel(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    return "DataModel";
  }

  @Override
  public JSONObject toJson() {
    URL url = ResourceBase.class.getResource("dummy_datamodel.json");
    if (url == null) {
      throw new JsonException("Failed to load dummy_datamodel.json");
    }
    try {
      // FIXME read from model
      // TODO BSH Maybe load this as cacheable file
      String jsonData = new String(IOUtility.getContent(url.openStream()), "utf-8");
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
  public void handleUiEvent(JsonEvent event) {
  }

}
