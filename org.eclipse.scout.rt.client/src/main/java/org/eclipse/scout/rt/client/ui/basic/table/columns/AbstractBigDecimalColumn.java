/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import java.math.BigDecimal;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IBigDecimalColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.IBigDecimalField;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

@ClassId("cf4984e1-7ec9-4442-a9d8-23145b0e1614")
public abstract class AbstractBigDecimalColumn extends AbstractDecimalColumn<BigDecimal> implements IBigDecimalColumn {

  public AbstractBigDecimalColumn() {
    this(true);
  }

  public AbstractBigDecimalColumn(boolean callInitializer) {
    super(callInitializer);
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_DECIMAL)
  @Order(200)
  protected BigDecimal getConfiguredMaxValue() {
    return null;
  }

  @Override
  @ConfigProperty(ConfigProperty.BIG_DECIMAL)
  @Order(210)
  protected BigDecimal getConfiguredMinValue() {
    return null;
  }

  @Override
  protected BigDecimal parseValueInternal(ITableRow row, Object rawValue) {
    BigDecimal validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof BigDecimal) {
      validValue = (BigDecimal) rawValue;
    }
    else if (rawValue instanceof Long) {
      validValue = new BigDecimal(rawValue.toString());
    }
    else if (rawValue instanceof Double) {
      validValue = BigDecimal.valueOf((Double) rawValue);
    }
    else {
      throw new ProcessingException("invalid BigDecimal value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected IBigDecimalField createDefaultEditor() {
    return new AbstractBigDecimalField() {
    };
  }

  protected static class LocalBigDecimalColumnExtension<OWNER extends AbstractBigDecimalColumn> extends LocalDecimalColumnExtension<BigDecimal, OWNER> implements IBigDecimalColumnExtension<OWNER> {

    public LocalBigDecimalColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IBigDecimalColumnExtension<? extends AbstractBigDecimalColumn> createLocalExtension() {
    return new LocalBigDecimalColumnExtension<>(this);
  }

}
