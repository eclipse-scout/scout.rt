/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONArray;

public final class JsonAdapterUtility {

  private JsonAdapterUtility() {
  }

  public static <M> String getAdapterIdForModel(IUiSession uiSession, M model, IJsonAdapter<?> parent) {
    return getAdapterIdForModel(uiSession, model, parent, null);
  }

  /**
   * Returns the ID of the JSON adapter for the given model.
   * This method requires that the adapter has already been created before.
   * The method will never create a new adapter instance.
   */
  public static <M> String getAdapterIdForModel(IUiSession uiSession, M model, IJsonAdapter<?> parent, IFilter<M> filter) {
    if (filter != null && !filter.accept(model)) {
      return null;
    }
    IJsonAdapter<?> adapter = uiSession.getJsonAdapter(model, parent);
    if (adapter == null) {
      throw new IllegalArgumentException("No adapter registered for model=" + model);
    }
    return adapter.getId();
  }

  /**
   * Returns a list of IDs of the JSON adapters for the given models.
   * This method requires that the adapter has already been created before.
   * The method will never create a new adapter instance.
   */
  public static <M> JSONArray getAdapterIdsForModel(IUiSession uiSession, Collection<M> models, IJsonAdapter<?> parent, IFilter<M> filter) {
    JSONArray jsonAdapterIds = new JSONArray();
    for (M model : models) {
      String adapterId = getAdapterIdForModel(uiSession, model, parent, filter);
      if (adapterId != null) {
        jsonAdapterIds.put(adapterId);
      }
    }
    return jsonAdapterIds;
  }

  public static <M extends Object> JSONArray getAdapterIdsForModel(IUiSession uiSession, Collection<M> models, IJsonAdapter<?> parent) {
    return getAdapterIdsForModel(uiSession, models, parent, null);
  }

  public static String getObjectType(String objectType, Object model) {
    if (model.getClass().isAnnotationPresent(ModelVariant.class)) {
      ModelVariant modelVariant = model.getClass().getAnnotation(ModelVariant.class);
      return objectType + "." + modelVariant.value();
    }
    else {
      return objectType;
    }
  }
}
