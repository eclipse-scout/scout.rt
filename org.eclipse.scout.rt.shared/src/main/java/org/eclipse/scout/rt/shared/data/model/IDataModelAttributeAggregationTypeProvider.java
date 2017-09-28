/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
   *          Data model attribute.
   * @param aggregationTypeList
   *          Live and mutable list of aggregation types.
   */
  void injectAggregationTypes(IDataModelAttribute attribute, List<Integer> aggregationTypeList);
}
