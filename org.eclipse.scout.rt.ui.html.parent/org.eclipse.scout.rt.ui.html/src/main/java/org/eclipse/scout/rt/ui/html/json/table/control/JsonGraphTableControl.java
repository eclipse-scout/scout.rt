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

import org.eclipse.scout.rt.client.ui.basic.table.control.IGraphTableControl;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.json.JSONObject;

public class JsonGraphTableControl<T extends IGraphTableControl> extends JsonTableControl<T> {

  //FIXME add to model
  private static final String GRAPH = "{\"nodes\": [{\"id\": 0, \"name\": \"Daniel Anders\", \"type\": \"center\"}," +
      "                            {\"id\": 1, \"name\": \"VISECA\", \"type\": \"company\"}," +
      "                            {\"id\": 2, \"name\": \"Markus Brunold\", \"type\": \"internal\"}," +
      "                            {\"id\": 3, \"name\": \"Hansruedi Näf\", \"type\": \"person\"}," +
      "                            {\"id\": 4, \"name\": \"Christina Rusche\", \"type\": \"internal\"}," +
      "                            {\"id\": 5, \"name\": \"Stefan Kämpfer\", \"type\": \"person\"}," +
      "                            {\"id\": 6, \"name\": \"Andrea Mafioretti\", \"type\": \"person\"}," +
      "                            {\"id\": 7, \"name\": \"Herbert Bucheli\", \"type\": \"person\"}," +
      "                            {\"id\": 8, \"name\": \"ITS2.3\", \"type\": \"department\"}]," +
      "                 \"links\": [{\"source\": 0, \"target\": 1, \"label\": \"\"}," +
      "                           {\"source\": 0, \"target\": 2, \"label\": \"Betreuer\"}," +
      "                           {\"source\": 0, \"target\": 3, \"label\": \"Studienfreund\"}," +
      "                           {\"source\": 0, \"target\": 4, \"label\": \"Hauptbetreuer\"}," +
      "                           {\"source\": 0, \"target\": 8, \"label\": \"Vorgesetzer\"}," +
      "                           {\"source\": 5, \"target\": 8, \"label\": \"Mitarbeiter\"}," +
      "                           {\"source\": 6, \"target\": 8, \"label\": \"Mitarbeiter\"}," +
      "                           {\"source\": 7, \"target\": 8, \"label\": \"Mitarbeiter\"}]}";

  public JsonGraphTableControl(T model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
  }

  @Override
  public String getObjectType() {
    return "GraphTableControl";
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    if (getModel().isSelected()) {
      putProperty(json, "graph", JsonObjectUtility.newJSONObject(GRAPH));
      m_contentLoaded = true;
    }
    return json;
  }

  @Override
  protected void handleUiLoadContent() {
    addPropertyChangeEvent("graph", JsonObjectUtility.newJSONObject(GRAPH));
  }

}
