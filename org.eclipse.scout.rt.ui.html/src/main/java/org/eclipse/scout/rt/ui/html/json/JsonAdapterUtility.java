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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.ModelVariant;
import org.eclipse.scout.rt.platform.filter.IFilter;
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
   * Returns a list of IDs of the JSON adapters for the given models. This method requires that the adapter has already
   * been created before. The method will never create a new adapter instance.
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
   * Resolves the adapter for the given formField, even when it is not a direct child adapter of the given
   * parentJsonAdapter (but the child adapter of a child adapter). If the formField does not belong to the adapter/field
   * hierarchy of the given parent, <code>null</code> is returned.
   */
  public static IJsonAdapter<?> findChildAdapter(IJsonAdapter<?> parentJsonAdapter, IFormField formField) {
    // Find all parent model fields of the given formField (ordered from top to bottom)
    List<IFormField> fieldHierarchy = getFieldHierarchy(formField);

    // Starting from the given parent adapter, resolve the corresponding adapters for all fields in
    // the hierarchy. Eventually, we should find the adapter that corresponds to the given formField.
    IJsonAdapter<?> formFieldAdapter = parentJsonAdapter;
    for (IFormField field : fieldHierarchy) {
      if (formFieldAdapter != null) {
        formFieldAdapter = formFieldAdapter.getAdapter(field);
      }
    }
    return formFieldAdapter;
  }

  /**
   * Returns an ordered list with the given formField as last element and the top-most parent field as first element.
   * <p>
   * Example: StringField -> [ GroupBox, GroupBox, TabBox, GroupBox, SequenceBox, StringField ].
   */
  private static List<IFormField> getFieldHierarchy(IFormField formField) {
    List<IFormField> fieldHierarchy = new ArrayList<>();
    while (formField != null) {
      fieldHierarchy.add(0, formField);
      formField = formField.getParentField();
    }
    return fieldHierarchy;
  }

  /**
   * @return a {@link JSONArray} with the IDs of the given adapters.
   */
  public static <T> JSONArray adapterIdsToJson(Collection<IJsonAdapter<T>> adapters) {
    return adapterIdsToJson(adapters, null);
  }

  /**
   * @return a {@link JSONArray} with the IDs of the given adapters that accept the given filter (or all adapters of the
   *         filter is <code>null</code>).
   */
  public static <T> JSONArray adapterIdsToJson(Collection<IJsonAdapter<T>> adapters, IFilter<T> filter) {
    if (adapters == null) {
      return null;
    }
    JSONArray array = new JSONArray();
    for (IJsonAdapter<T> adapter : adapters) {
      if (filter == null || filter.accept(adapter.getModel())) {
        array.put(adapter.getId());
      }
    }
    return array;
  }
}
