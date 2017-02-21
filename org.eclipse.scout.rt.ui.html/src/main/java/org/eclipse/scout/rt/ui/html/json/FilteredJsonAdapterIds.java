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
