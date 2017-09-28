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

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.json.JSONArray;

/**
 * A holder for a list of adapters and a filter than can be put into a {@link JsonPropertyChangeEvent}. The filter is
 * only applied when {@link #toJson()} is called.
 */
public class FilteredJsonAdapterIds<MODEL> implements IJsonObject {

  private final Collection<IJsonAdapter<MODEL>> m_adapters;
  private final IFilter<MODEL> m_filter;

  public FilteredJsonAdapterIds(Collection<IJsonAdapter<MODEL>> adapters, IFilter<MODEL> filter) {
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
