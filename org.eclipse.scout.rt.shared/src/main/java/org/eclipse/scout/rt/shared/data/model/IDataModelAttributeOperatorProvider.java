/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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
@ApplicationScoped
public interface IDataModelAttributeOperatorProvider {

  /**
   * Inject operators for the provided attribute.
   *
   * @param attribute
   *          Data model attribute.
   * @param operatorList
   *          Live and mutable list of operators.
   */
  void injectOperators(IDataModelAttribute attribute, List<IDataModelAttributeOp> operatorList);

  /**
   * Create a data model attribute operation for the given operator.
   *
   * @param operator
   *          Operator
   * @param shortText
   *          Short text (optional, otherwise default will be used)
   * @param text
   *          Text (optional, otherwise default will be used)
   * @return Data model attribute operation
   */
  IDataModelAttributeOp createOperator(int operator, String shortText, String text);
}
