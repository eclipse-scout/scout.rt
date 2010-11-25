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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.timefield.AbstractTimeField;

/**
 * Column holding Strings
 */
public abstract class AbstractTimeColumn extends AbstractColumn<Double> implements ITimeColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private DateFormat m_fmt;

  public AbstractTimeColumn() {
    super();
  }

  @Override
  protected int getConfiguredHorizontalAlignment() {
    return 1;
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(140)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
  }

  /*
   * Runtime
   */
  public void setFormat(String s) {
    m_format = s;
    m_fmt = null;
  }

  public String getFormat() {
    return m_format;
  }

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
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractTimeField f = new AbstractTimeField() {
    };
    f.setFormat(getFormat());
    f.setLabelVisible(false);
    f.setValue(getValue(row));
    return f;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      long time = (long) (((Double) cell.getValue()).doubleValue() * MILLIS_PER_DAY + 0.5);
      Calendar c = Calendar.getInstance();
      c.set(Calendar.MILLISECOND, (int) (time % 1000));
      time = time / 1000;
      c.set(Calendar.SECOND, (int) (time % 60));
      time = time / 60;
      c.set(Calendar.MINUTE, (int) (time % 60));
      time = time / 60;
      c.set(Calendar.HOUR_OF_DAY, (int) (time % 24));
      cell.setText(getDateFormat().format(c.getTime()));
    }
    else {
      cell.setText("");
    }
  }

  private DateFormat getDateFormat() {
    if (m_fmt == null) {
      if (getFormat() != null) {
        m_fmt = new SimpleDateFormat(getFormat());
      }
      else {
        m_fmt = DateFormat.getTimeInstance(DateFormat.SHORT);
        m_fmt.setLenient(true);
      }
    }
    return m_fmt;
  }

}
