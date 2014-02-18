package org.eclipse.scout.rt.ui.json;

import org.json.JSONObject;

public interface IJsonRenderer {

  String getId();

  void init() throws JsonUIException;

  void dispose() throws JsonUIException;

  JSONObject toJson() throws JsonUIException;

  void handleUiEvent(JsonRequest req, JsonResponse res) throws JsonUIException;

}
