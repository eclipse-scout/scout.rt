/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

/**
 * Creates JSON output for a Scout model object.
 *
 * @param <T>
 *          Type of Scout model
 */
public interface IJsonAdapter<T> extends IJsonObject {

  IUiSession getUiSession();

  String getId();

  /**
   * Returns a string used to identify the object-type in the JSON output (JSON attribute 'objectType').
   */
  String getObjectType();

  @Override
  JSONObject toJson();

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
   * An adapter may clean up it's event filters when this method is called after an UI event has been processed. By
   * default this method does nothing.
   */
  void cleanUpEventFilters();

  // TODO [7.0] cgu, bsh: The following methods should be renamed:
  // attachAdapter -> attachChildAdapter
  // getAdapter -> getChildAdapter
  // getAdapters -> getChildAdapters
  // getParent -> getParentAdapter

  <A extends IJsonAdapter<? super MODEL>, MODEL> A attachAdapter(MODEL model, Predicate<MODEL> filter);

  <MODEL> List<IJsonAdapter<?>> attachAdapters(Collection<MODEL> models);

  <MODEL> List<IJsonAdapter<?>> attachAdapters(Collection<MODEL> models, Predicate<MODEL> filter);

  <A extends IJsonAdapter<? super Object>> A getAdapter(Object model);

  <A extends IJsonAdapter<? super MODEL>, MODEL> A getAdapter(MODEL model, Predicate<MODEL> filter);

  Collection<IJsonAdapter<?>> getAdapters(Collection<?> models);

  <MODEL> Collection<IJsonAdapter<?>> getAdapters(Collection<MODEL> models, Predicate<MODEL> filter);

  IJsonAdapter<?> getParent();

  /**
   * Consumes all buffered model events, if there are any. The buffer will be empty afterwards.
   */
  void processBufferedEvents();
}
