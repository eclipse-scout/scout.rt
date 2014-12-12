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

import org.json.JSONArray;

public class JsonAdapterUtility {

  /**
   * Returns the ID of the JSON adapter for the given model.
   * This method requires that the adapter has already been created before.
   * The method will never create a new adapter instance.
   */
  public static String getAdapterIdForModel(IJsonSession jsonSession, Object model, IJsonAdapter<?> parent) {
    IJsonAdapter<?> adapter = jsonSession.getJsonAdapter(model, parent);
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
  public static JSONArray getAdapterIdsForModel(IJsonSession jsonSession, Collection<?> models, IJsonAdapter<?> parent) {
    JSONArray jsonAdapterIds = new JSONArray();
    for (Object model : models) {
      String adapterId = getAdapterIdForModel(jsonSession, model, parent);
      jsonAdapterIds.put(adapterId);
    }
    return jsonAdapterIds;
  }
}
