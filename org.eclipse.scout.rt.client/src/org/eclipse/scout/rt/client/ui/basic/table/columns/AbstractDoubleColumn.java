/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.IDoubleField;

/**
 * Column holding Double
 */
@ClassId("fc6f348b-e7dc-4654-8b7c-f4c9f2f5cd08")
public abstract class AbstractDoubleColumn extends AbstractDecimalColumn<Double> implements IDoubleColumn {

  public AbstractDoubleColumn() {
    super();
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */
  @Override
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(200)
  protected Double getConfiguredMaxValue() {
    return null;
  }

  @Override
  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(210)
  protected Double getConfiguredMinValue() {
    return null;
  }

  /*
   * Runtime
   */

  @Override
  protected Double parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    Double validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Double) {
      validValue = (Double) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = ((Number) rawValue).doubleValue();
    }
    else {
      throw new ProcessingException("invalid Double value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected IDoubleField getEditorField() {
    return new AbstractDoubleField() {
    };
  }

}
