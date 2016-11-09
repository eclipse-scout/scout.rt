package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.1
 */
@ApplicationScoped
public interface IDataModelAttributeAggregationTypeProvider {

  /**
   * Inject aggregation types for the provided attribute
   *
   * @param attribute
   *          Data model attribute.
   * @param aggregationTypeList
   *          Live and mutable list of aggregation types.
   */
  void injectAggregationTypes(IDataModelAttribute attribute, List<Integer> aggregationTypeList);
}
