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
import org.eclipse.scout.rt.client.ui.form.fields.doublefield.AbstractDoubleField;

/**
 * Column holding Double
 */
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

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(200)
  @ConfigPropertyValue("null")
  protected Double getConfiguredMaxValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.DOUBLE)
  @Order(210)
  @ConfigPropertyValue("null")
  protected Double getConfiguredMinValue() {
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
  protected AbstractDoubleField getEditorField() {
    return new AbstractDoubleField() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractDoubleColumn.this.propertySupport.getPropertiesMap());
      }
    };
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getNumberFormat().format(((Double) cell.getValue()).doubleValue()));
    }
    else {
      cell.setText("");
    }
  }

  @Override
  public NumberFormat getNumberFormat() {
    if (super.getNumberFormat() == null) {
      NumberFormat fmt;
      if (isPercent()) {
        fmt = NumberFormat.getPercentInstance(LocaleThreadLocal.get());
      }
      else {
        fmt = NumberFormat.getNumberInstance(LocaleThreadLocal.get());
      }
      if (fmt instanceof DecimalFormat) {
        ((DecimalFormat) fmt).setMultiplier(getMultiplier());
        if (getFormat() != null) {
          ((DecimalFormat) fmt).applyPattern(getFormat());
        }
        else {
          fmt.setMinimumFractionDigits(getMinFractionDigits());
          fmt.setMaximumFractionDigits(getMaxFractionDigits());
          fmt.setGroupingUsed(isGroupingUsed());
        }
      }
      else {
        fmt.setMinimumFractionDigits(getMinFractionDigits());
        fmt.setMaximumFractionDigits(getMaxFractionDigits());
        fmt.setGroupingUsed(isGroupingUsed());
      }
      setNumberFormat(fmt);
    }
    return super.getNumberFormat();
  }

}
