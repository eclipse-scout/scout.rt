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

  /**
   * Root adapters (such as the desktop and other custom adapters) are created in the session 'initialize' event on the
   * javascript side
   * <p>
   * All other adapters are created at first time use TODO cgu please review this (from imo)
   * <p>
   * see {@link JsonResponse#addAdapter(IJsonAdapter)}
   */
  boolean isRoot();

  void attach();

  boolean isAttached();

  void dispose();

  void handleUiEvent(JsonEvent event, JsonResponse res);

}
