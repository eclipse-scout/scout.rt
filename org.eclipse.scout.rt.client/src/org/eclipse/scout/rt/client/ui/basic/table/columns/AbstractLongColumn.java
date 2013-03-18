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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.longfield.AbstractLongField;

/**
 * Column holding Long
 */
public abstract class AbstractLongColumn extends AbstractNumberColumn<Long> implements ILongColumn {

  public AbstractLongColumn() {
    super();
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.LONG)
  @Order(160)
  @ConfigPropertyValue("null")
  protected Long getConfiguredMaxValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.LONG)
  @Order(170)
  @ConfigPropertyValue("null")
  protected Long getConfiguredMinValue() {
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setMaxValue(getConfiguredMaxValue());
    setMinValue(getConfiguredMinValue());
  }

  /*
   * Runtime
   */

  @Override
  protected Long parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    Long validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Long) {
      validValue = (Long) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = ((Number) rawValue).longValue();
    }
    else {
      throw new ProcessingException("invalid Long value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected AbstractLongField getEditorField() {
    return new AbstractLongField() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractLongColumn.this.propertySupport.getPropertiesMap());
      }
    };
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getNumberFormat().format(((Long) cell.getValue()).longValue()));
    }
    else {
      cell.setText("");
    }
  }

  @Override
  public NumberFormat getNumberFormat() {
    if (super.getNumberFormat() == null) {
      if (getFormat() != null) {
        DecimalFormat x = (DecimalFormat) DecimalFormat.getNumberInstance(LocaleThreadLocal.get());
        x.applyPattern(getFormat());
        x.setMinimumFractionDigits(0);
        x.setMaximumFractionDigits(0);
        setNumberFormat(x);
      }
      else {
        NumberFormat y = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
        y.setMinimumFractionDigits(0);
        y.setMaximumFractionDigits(0);
        y.setGroupingUsed(isGroupingUsed());
        setNumberFormat(y);
      }
    }
    return super.getNumberFormat();
  }

}
