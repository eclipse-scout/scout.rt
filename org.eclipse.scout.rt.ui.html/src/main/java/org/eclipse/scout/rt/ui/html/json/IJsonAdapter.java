/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
 *     Type of Scout model
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

  <A extends IJsonAdapter<M>, M> A attachAdapter(M model, Predicate<M> filter);

  <M> List<IJsonAdapter<?>> attachAdapters(Collection<M> models);

  <M> List<IJsonAdapter<?>> attachAdapters(Collection<M> models, Predicate<M> filter);

  <A extends IJsonAdapter<M>, M> A getAdapter(M model);

  <A extends IJsonAdapter<M>, M> A getAdapter(M model, Predicate<M> filter);

  Collection<IJsonAdapter<?>> getAdapters(Collection<?> models);

  <M> Collection<IJsonAdapter<?>> getAdapters(Collection<M> models, Predicate<M> filter);

  IJsonAdapter<?> getParent();

  boolean hasAncestor(IJsonAdapter<?> ancestor);

  /**
   * Consumes all buffered model events, if there are any. The buffer will be empty afterwards.
   */
  void processBufferedEvents();
}
