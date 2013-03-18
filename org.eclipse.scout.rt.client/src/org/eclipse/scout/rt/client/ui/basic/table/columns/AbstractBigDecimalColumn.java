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

/**
 * C
 */
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.bigdecimalfield.AbstractBigDecimalField;

/**
 * Column holding Double
 */
public abstract class AbstractBigDecimalColumn extends AbstractDecimalColumn<BigDecimal> implements IBigDecimalColumn {

  public AbstractBigDecimalColumn() {
    super();
  }

  @Override
  protected BigDecimal parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    BigDecimal validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof BigDecimal) {
      validValue = (BigDecimal) rawValue;
    }
    else if (rawValue instanceof Long) {
      validValue = new BigDecimal(((Long) rawValue).toString());
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
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    return super.prepareEditInternal(row);
  }

  @Override
  protected AbstractBigDecimalField getEditorField() {
    return new AbstractBigDecimalField() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractBigDecimalColumn.this.propertySupport.getPropertiesMap());
      }
    };
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getNumberFormat().format(cell.getValue()));
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
