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

import java.math.BigInteger;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.AbstractBigIntegerField;

/**
 * Column holding {@link BigInteger}
 */
public abstract class AbstractBigIntegerColumn extends AbstractNumberColumn<BigInteger> implements IBigIntegerColumn {

  public AbstractBigIntegerColumn() {
    super();
  }

  /*
   * Configuration
   */
  @Override
  @ConfigProperty(ConfigProperty.BIG_INTEGER)
  @Order(160)
  protected BigInteger getConfiguredMaxValue() {
    return null;
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_INTEGER)
  @Order(170)
  protected BigInteger getConfiguredMinValue() {
    return null;
  }

  /*
   * Runtime
   */

  @Override
  protected BigInteger parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    BigInteger validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof BigInteger) {
      validValue = (BigInteger) rawValue;
    }
    else {
      throw new ProcessingException("invalid BigInteger value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected AbstractBigIntegerField getEditorField() {
    return new AbstractBigIntegerField() {
    };
  }

}
