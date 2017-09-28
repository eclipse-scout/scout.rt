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

import org.eclipse.scout.rt.platform.BEANS;

public final class DataModelAttributeOp implements DataModelConstants {

  private DataModelAttributeOp() {
  }

  public static IDataModelAttributeOp create(int operator, String shortText, String text) {
    for (IDataModelAttributeOperatorProvider provider : BEANS.all(IDataModelAttributeOperatorProvider.class)) {
      IDataModelAttributeOp attributeOp = provider.createOperator(operator, shortText, text);
      if (attributeOp != null) {
        return attributeOp;
      }
    }
    return null;
  }

  /**
   * @return a new {@link IDataModelAttributeOp} for a {@link DataModelConstants#OPERATOR_*}
   */
  public static IDataModelAttributeOp create(int operator) {
    return create(operator, null, null);
  }
}
