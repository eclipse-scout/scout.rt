package org.eclipse.scout.rt.ui.html.json;

import java.util.Collection;

import org.eclipse.scout.commons.filter.IFilter;

/**
 * Creates JSON output for a Scout model object.
 *
 * @param <T>
 *          Type of Scout model
 */
public interface IJsonAdapter<T extends Object> extends IJsonObject {

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

  /**
   * Init method which is called by the factory <em>after</em> the constructor has been executed. The default
   * implementation calls <code>attachModel()</code> and <code>attachChildAdapters()</code>.
   */
  void init();

  boolean isInitialized();

  boolean isDisposed();

  void dispose();

  void handleUiEvent(JsonEvent event);

  /**
   * An adapter may clean up it's event filters when this method is called after an UI event has been processed.
   * By default this method does nothing.
   */
  void cleanUpEventFilters();

  <A extends IJsonAdapter<?>, M> A attachAdapter(M model, IFilter<M> filter);

  <A extends IJsonAdapter<?>> A getAdapter(Object model);

  Collection<IJsonAdapter<?>> getAdapters(Collection<?> models);

  IJsonAdapter<?> getParent();

  /**
   * Consumes all buffered model events, if there are any. The buffer will be empty afterwards.
   */
  void processBufferedEvents();
}
