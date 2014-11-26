package org.eclipse.scout.rt.ui.html.json;

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

  void attach();

  boolean isAttached();

  void dispose();

  void handleUiEvent(JsonEvent event, JsonResponse res);

  /**
   * An adapter may clean up it's event filters when this method is called after an UI event has been processed.
   * By default this method does nothing.
   */
  void cleanUpEventFilters();

}
