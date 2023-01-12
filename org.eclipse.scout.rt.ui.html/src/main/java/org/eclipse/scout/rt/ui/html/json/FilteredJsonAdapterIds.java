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

import org.json.JSONArray;

/**
 * A holder for a list of adapters and a filter than can be put into a {@link JsonPropertyChangeEvent}. The filter is
 * only applied when {@link #toJson()} is called.
 */
public class FilteredJsonAdapterIds<MODEL> implements IJsonObject {

  private final Collection<IJsonAdapter<MODEL>> m_adapters;
  private final Predicate<MODEL> m_filter;

  public FilteredJsonAdapterIds(Collection<IJsonAdapter<MODEL>> adapters, Predicate<MODEL> filter) {
    m_adapters = adapters;
    m_filter = filter;
  }

  public JSONArray getFilteredAdapterIds() {
    return JsonAdapterUtility.adapterIdsToJson(m_adapters, m_filter);
  }

  @Override
  public Object toJson() {
    return getFilteredAdapterIds();
  }
}
