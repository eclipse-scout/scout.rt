package org.eclipse.scout.rt.ui.html.json;

import java.util.Collection;

/**
 * Creates JSON output for a Scout model object.
 *
 * @param <T>
 *          Type of Scout model
 */
public interface IJsonAdapter<T extends Object> extends IJsonMapper {

  IJsonSession getJsonSession();

  String getId();

  /**
   * Returns a string used to identify the object-type in the JSON output
   * (JSON attribute 'objectType').
   */
  String getObjectType();

  /**
   * Returns the Scout model object.
   */
  T getModel();

  void init();

  void attach();

  boolean isAttached();

  void dispose();

  void handleUiEvent(JsonEvent event, JsonResponse res);

  /**
   * An adapter may clean up it's event filters when this method is called after an UI event has been processed.
   * By default this method does nothing.
   */
  void cleanUpEventFilters();

  <A extends IJsonAdapter<?>> A attachAdapter(Object model);

  <A extends IJsonAdapter<?>> A getAdapter(Object model);

  Collection<IJsonAdapter<?>> getAdapters(Collection<?> models);

  IJsonAdapter<?> getParent();
}
