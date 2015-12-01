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

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IBigIntegerColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.AbstractBigIntegerField;
import org.eclipse.scout.rt.client.ui.form.fields.bigintegerfield.IBigIntegerField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

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
  protected BigInteger parseValueInternal(ITableRow row, Object rawValue) {
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
  protected IBigIntegerField createDefaultEditor() {
    return new AbstractBigIntegerField() {
    };
  }

  protected static class LocalBigIntegerColumnExtension<OWNER extends AbstractBigIntegerColumn> extends LocalNumberColumnExtension<BigInteger, OWNER> implements IBigIntegerColumnExtension<OWNER> {

    public LocalBigIntegerColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBigIntegerColumnExtension<? extends AbstractBigIntegerColumn> createLocalExtension() {
    return new LocalBigIntegerColumnExtension<AbstractBigIntegerColumn>(this);
  }

}
