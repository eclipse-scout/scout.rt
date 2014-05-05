package org.eclipse.scout.rt.ui.json;

import org.json.JSONObject;

public interface IJsonRenderer<T extends Object> {

  String getId();

  String getObjectType();

  T getModelObject();

  void init() throws JsonException;

  void dispose() throws JsonException;

  JSONObject toJson() throws JsonException;

  void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonException;
}
