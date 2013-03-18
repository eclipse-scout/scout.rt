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
import org.eclipse.scout.rt.client.ui.form.fields.integerfield.AbstractIntegerField;

/**
 * Column holding Integer
 */
public abstract class AbstractIntegerColumn extends AbstractNumberColumn<Integer> implements IIntegerColumn {

  public AbstractIntegerColumn() {
    super();
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(160)
  @ConfigPropertyValue("null")
  protected Integer getConfiguredMaxValue() {
    return null;
  }

  @ConfigProperty(ConfigProperty.INTEGER)
  @Order(170)
  @ConfigPropertyValue("null")
  protected Integer getConfiguredMinValue() {
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
  protected Integer parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    Integer validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Integer) {
      validValue = (Integer) rawValue;
    }
    else if (rawValue instanceof Number) {
      validValue = ((Number) rawValue).intValue();
    }
    else {
      throw new ProcessingException("invalid Integer value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  @Override
  protected AbstractIntegerField getEditorField() {
    return new AbstractIntegerField() {
      @Override
      protected void initConfig() {
        super.initConfig();
        propertySupport.putPropertiesMap(AbstractIntegerColumn.this.propertySupport.getPropertiesMap());
      }
    };
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getNumberFormat().format(((Integer) cell.getValue()).intValue()));
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
