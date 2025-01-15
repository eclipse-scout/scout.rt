/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.data.model;

import java.util.List;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * @since 6.1
 */
@FunctionalInterface
@ApplicationScoped
public interface IDataModelAttributeAggregationTypeProvider {

  /**
   * Inject aggregation types for the provided attribute
   *
   * @param attribute
   *     Data model attribute.
   * @param aggregationTypeList
   *     Live and mutable list of aggregation types.
   */
  void injectAggregationTypes(IDataModelAttribute attribute, List<Integer> aggregationTypeList);
}
