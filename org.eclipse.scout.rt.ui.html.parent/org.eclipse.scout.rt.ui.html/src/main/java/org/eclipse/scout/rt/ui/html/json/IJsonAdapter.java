package org.eclipse.scout.rt.ui.html.json;

import org.json.JSONObject;

/**
 * Creates JSON output for a Scout model object.
 * 
 * @param <T>
 *          Type of Scout model
 */
public interface IJsonAdapter<T extends Object> extends IJsonMapper {

  String getId();

  /**
   * Returns a string used to identify the object-type in the JSON output
   * (JSON attribute 'objectType').
   * 
   * @return
   */
  String getObjectType();

  /**
   * Returns the Scout model object.
   * 
   * @return
   */
  T getModel();

  void startup();

  void attach();

  boolean isAttached();

  void dispose();

  void handleUiEvent(JsonEvent event, JsonResponse res);

  /**
   * If the adapter is attached the id is returned instead of the whole object. Otherwise {@link #toJson()}() is called.
   */
  JSONObject write();
}
