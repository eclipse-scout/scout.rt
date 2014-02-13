package org.eclipse.scout.rt.ui.json;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.json.JSONObject;

public interface IJsonRenderer {
  String getId();

  JSONObject toJson() throws ProcessingException;

  void handleUiEvent(String type) throws ProcessingException;

  void init() throws ProcessingException;

  void dispose() throws ProcessingException;
}
