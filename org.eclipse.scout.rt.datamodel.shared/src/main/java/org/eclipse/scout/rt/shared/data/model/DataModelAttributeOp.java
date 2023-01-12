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

import org.eclipse.scout.rt.platform.BEANS;

public final class DataModelAttributeOp implements DataModelConstants {

  private DataModelAttributeOp() {
  }

  public static IDataModelAttributeOp create(int operator, String shortText, String text) {
    return create(operator, shortText, text, null);
  }

  public static IDataModelAttributeOp create(int operator, String shortText, String text, String explanationText) {
    for (IDataModelAttributeOperatorProvider provider : BEANS.all(IDataModelAttributeOperatorProvider.class)) {
      IDataModelAttributeOp attributeOp = provider.createOperator(operator, shortText, text, explanationText);
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
