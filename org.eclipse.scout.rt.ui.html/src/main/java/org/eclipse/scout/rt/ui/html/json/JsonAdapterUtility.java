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
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ui.IWidget;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONArray;

public final class JsonAdapterUtility {

  private JsonAdapterUtility() {
  }

  public static <M> String getAdapterIdForModel(IUiSession uiSession, M model, IJsonAdapter<?> parent) {
    return getAdapterIdForModel(uiSession, model, parent, null);
  }

  /**
   * Returns the ID of the JSON adapter for the given model. This method requires that the adapter has already been
   * created before. The method will never create a new adapter instance.
   */
  public static <M> String getAdapterIdForModel(IUiSession uiSession, M model, IJsonAdapter<?> parent, Predicate<M> filter) {
    if (filter != null && !filter.test(model)) {
      return null;
    }
    IJsonAdapter<?> adapter = uiSession.getJsonAdapter(model, parent);
    if (adapter == null) {
      throw new IllegalArgumentException("No adapter registered for model=" + model);
    }
    return adapter.getId();
  }

  /**
   * Returns a list of IDs of the JSON adapters for the given models. This method requires that the adapter has already
   * been created before. The method will never create a new adapter instance.
   */
  public static <M> JSONArray getAdapterIdsForModel(IUiSession uiSession, Collection<M> models, IJsonAdapter<?> parent, Predicate<M> filter) {
    JSONArray jsonAdapterIds = new JSONArray();
    for (M model : models) {
      String adapterId = getAdapterIdForModel(uiSession, model, parent, filter);
      if (adapterId != null) {
        jsonAdapterIds.put(adapterId);
      }
    }
    return jsonAdapterIds;
  }

  public static <M> JSONArray getAdapterIdsForModel(IUiSession uiSession, Collection<M> models, IJsonAdapter<?> parent) {
    return getAdapterIdsForModel(uiSession, models, parent, null);
  }

  public static String getObjectType(String objectType, Object model) {
    if (model.getClass().isAnnotationPresent(ModelVariant.class)) {
      ModelVariant modelVariant = model.getClass().getAnnotation(ModelVariant.class);
      if (StringUtility.hasText(modelVariant.value())) {
        return objectType + ModelVariant.SEPARATOR + modelVariant.value();
      }
    }
    return objectType;
  }

  /**
   * Resolves the adapter for the given widget, even when it is not a direct child adapter of the given
   * parentJsonAdapter (but the child adapter of a child adapter). If the widget does not belong to the adapter/widget
   * hierarchy of the given parent, <code>null</code> is returned.
   */
  public static IJsonAdapter<?> findChildAdapter(IJsonAdapter<?> parentJsonAdapter, IWidget widget) {
    if (parentJsonAdapter != null) {
      for (IJsonAdapter<?> potential : parentJsonAdapter.getUiSession().getJsonAdapters(widget)) {
        if (potential.hasAncestor(parentJsonAdapter)) {
          return potential;
        }
      }
    }
    return null;
  }

  /**
   * @return a {@link JSONArray} with the IDs of the given adapters.
   */
  public static <T> JSONArray adapterIdsToJson(Collection<IJsonAdapter<T>> adapters) {
    return adapterIdsToJson(adapters, null);
  }

  /**
   * @return a {@link JSONArray} with the IDs of the given adapters that accept the given filter (or all adapters of the
   * filter is <code>null</code>).
   */
  public static <T> JSONArray adapterIdsToJson(Collection<IJsonAdapter<T>> adapters, Predicate<T> filter) {
    if (adapters == null) {
      return null;
    }
    JSONArray array = new JSONArray();
    for (IJsonAdapter<T> adapter : adapters) {
      if (filter == null || filter.test(adapter.getModel())) {
        array.put(adapter.getId());
      }
    }
    return array;
  }
}
