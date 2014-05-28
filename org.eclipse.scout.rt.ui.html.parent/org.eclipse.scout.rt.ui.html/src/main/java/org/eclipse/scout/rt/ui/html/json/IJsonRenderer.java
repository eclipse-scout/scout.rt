package org.eclipse.scout.rt.ui.html.json;

// TODO CGU: re-name to IJsonAdapter when merging the two projects (also AbstractJsonRenderer)

/**
 * Creates JSON output for a Scout model object.
 * 
 * @param <T>
 *          Type of Scout model
 */
public interface IJsonRenderer<T extends Object> extends IJsonMapper {

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
  T getModelObject();

  void init();

  void dispose();

  void handleUiEvent(JsonEvent event, JsonResponse response);

}
