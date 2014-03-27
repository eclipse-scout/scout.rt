package org.eclipse.scout.rt.ui.json;

import org.json.JSONObject;

public interface IJsonRenderer {

  String getId();

  String getObjectType();

  void init() throws JsonUIException;

  void dispose() throws JsonUIException;

  JSONObject toJson() throws JsonUIException;

  void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonUIException;
}
