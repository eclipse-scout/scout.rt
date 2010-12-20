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
import java.util.Date;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.basic.cell.Cell;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;

/**
 * Column holding Strings
 */
public abstract class AbstractDateColumn extends AbstractColumn<Date> implements IDateColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private boolean m_hasTime;
  private boolean m_hasDate;
  private DateFormat m_fmt;

  public AbstractDateColumn() {
    super();
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

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredHasDate() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(151)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredHasTime() {
    return false;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setHasDate(getConfiguredHasDate());
    setHasTime(getConfiguredHasTime());
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

  public void setHasDate(boolean b) {
    m_hasDate = b;
    m_fmt = null;
  }

  public void setHasTime(boolean b) {
    m_hasTime = b;
    m_fmt = null;
  }

  public boolean isHasDate() {
    return m_hasDate;
  }

  public boolean isHasTime() {
    return m_hasTime;
  }

  @Override
  protected Date parseValueInternal(ITableRow row, Object rawValue) throws ProcessingException {
    //legacy support
    if (rawValue instanceof Number) {
      rawValue = convertDoubleTimeToDate((Number) rawValue);
    }
    Date validValue = null;
    if (rawValue == null) {
      validValue = null;
    }
    else if (rawValue instanceof Date) {
      validValue = (Date) rawValue;
    }
    else {
      throw new ProcessingException("invalid Date value in column '" + getClass().getSimpleName() + "': " + rawValue + " class=" + rawValue.getClass());
    }
    return validValue;
  }

  private Date convertDoubleTimeToDate(Number d) {
    if (d == null) {
      return null;
    }
    int m = (int) (((long) (d.doubleValue() * MILLIS_PER_DAY + 0.5)) % MILLIS_PER_DAY);
    Calendar c = Calendar.getInstance();
    c.clear();
    c.set(Calendar.MILLISECOND, m % 1000);
    m = m / 1000;
    c.set(Calendar.SECOND, m % 60);
    m = m / 60;
    c.set(Calendar.MINUTE, m % 60);
    m = m / 60;
    c.set(Calendar.HOUR_OF_DAY, m % 24);
    return c.getTime();
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) throws ProcessingException {
    AbstractDateField f = new AbstractDateField() {
    };
    f.setFormat(getFormat());
    f.setHasDate(isHasDate());
    f.setHasTime(isHasTime());
    return f;
  }

  @Override
  protected void decorateCellInternal(Cell cell, ITableRow row) {
    super.decorateCellInternal(cell, row);
    if (cell.getValue() != null) {
      cell.setText(getDateFormat().format((Date) cell.getValue()));
    }
    else {
      cell.setText("");
    }
  }

  private DateFormat getDateFormat() {
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat());
    }
    else {
      if (isHasDate() && !isHasTime()) {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
      }
      else if (!isHasDate() && isHasTime()) {
        df = DateFormat.getTimeInstance(DateFormat.SHORT);
      }
      else {
        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      }
      df.setLenient(true);
    }
    return df;
  }

}
