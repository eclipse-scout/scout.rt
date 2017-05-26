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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.scout.rt.client.extension.ui.basic.table.columns.IDateColumnExtension;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.IFormField;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractDateField;
import org.eclipse.scout.rt.client.ui.form.fields.datefield.IDateField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;

/**
 * Column holding Date
 */
@ClassId("9185f9ed-3dc2-459b-b06b-f39c6c6fed2e")
public abstract class AbstractDateColumn extends AbstractColumn<Date> implements IDateColumn {
  // DO NOT init members, this has the same effect as if they were set AFTER
  // initConfig()
  private String m_format;
  private boolean m_hasTime;
  private boolean m_hasDate;
  private Date m_autoDate;

  public AbstractDateColumn() {
    this(true);
  }

  public AbstractDateColumn(boolean callInitializer) {
    super(callInitializer);
  }

  /*
   * Configuration
   */

  /**
   * Configures the format used to render the value. See the {@link DateFormat} class for more information about the
   * expected format.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return Format of this column.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(140)
  protected String getConfiguredFormat() {
    return null;
  }

  /**
   * Configures whether the value represented by this column has a date. If {@link #getConfiguredFormat()} is set, this
   * configuration has no effect.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if the value represented by this column has a date, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(150)
  protected boolean getConfiguredHasDate() {
    return true;
  }

  /**
   * Configures whether the value represented by this column has a time. If {@link #getConfiguredFormat()} is set, this
   * configuration has no effect.
   * <p>
   * Subclasses can override this method. Default is {@code false}.
   *
   * @return {@code true} if the value represented by this column has a time, {@code false} otherwise.
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(151)
  protected boolean getConfiguredHasTime() {
    return false;
  }

  /**
   * Only relevant when the date field has both the date and the time part enabled. When the user picks a date or the
   * time, the other value is set automatically using the return value of this method. If it is <code>null</code> (the
   * default), the current date and time is used.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(152)
  protected Date getConfiguredAutoDate() {
    return null;
  }

  /**
   * Configures the format used to group this column. See the {@link DateFormat} class for more information about the
   * expected format.
   * <p>
   * Subclasses can override this method. Default is YYYY (for full year only).
   *
   * @return Format for grouping this column.
   */
  @ConfigProperty(ConfigProperty.OBJECT)
  @Order(153)
  protected String getConfiguredGroupFormat() {
    return "yyyy";
  }

  @Override
  protected boolean getConfiguredUiSortPossible() {
    return true;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setFormat(getConfiguredFormat());
    setHasDate(getConfiguredHasDate());
    setHasTime(getConfiguredHasTime());
    setAutoDate(getConfiguredAutoDate());
    setGroupFormat(getConfiguredGroupFormat());
  }

  /*
   * Runtime
   */
  @Override
  public void setFormat(String s) {
    m_format = s;
    updateDisplayTexts();
  }

  @Override
  public String getFormat() {
    return m_format;
  }

  @Override
  public void setHasDate(boolean b) {
    m_hasDate = b;
    refreshValues();
  }

  @Override
  public void setHasTime(boolean b) {
    m_hasTime = b;
    refreshValues();
  }

  @Override
  public boolean isHasDate() {
    return m_hasDate;
  }

  @Override
  public boolean isHasTime() {
    return m_hasTime;
  }

  @Override
  public Date getAutoDate() {
    return m_autoDate;
  }

  @Override
  public void setAutoDate(Date autoDate) {
    m_autoDate = autoDate;
  }

  @Override
  public String getGroupFormat() {
    return propertySupport.getPropertyString(PROP_GROUP_FORMAT);
  }

  @Override
  public void setGroupFormat(String groupFormat) {
    propertySupport.setPropertyString(PROP_GROUP_FORMAT, groupFormat);
  }

  @Override
  protected IFormField prepareEditInternal(ITableRow row) {
    IDateField f = (IDateField) getDefaultEditor();
    mapEditorFieldProperties(f);
    return f;
  }

  @Override
  protected IValueField<Date> createDefaultEditor() {
    return new AbstractDateField() {
    };
  }

  protected void mapEditorFieldProperties(IDateField f) {
    super.mapEditorFieldProperties(f);
    f.setFormat(getFormat());
    f.setHasDate(isHasDate());
    f.setHasTime(isHasTime());
    f.setAutoDate(getAutoDate());
  }

  @Override
  protected String formatValueInternal(ITableRow row, Date value) {
    if (value != null) {
      return getDateFormat().format(value);
    }
    return "";
  }

  protected DateFormat getDateFormat() {
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat(), NlsLocale.get());
    }
    else {
      if (isHasDate() && !isHasTime()) {
        df = BEANS.get(DateFormatProvider.class).getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
      }
      else if (!isHasDate() && isHasTime()) {
        df = BEANS.get(DateFormatProvider.class).getTimeInstance(DateFormat.SHORT, NlsLocale.get());
      }
      else {
        df = BEANS.get(DateFormatProvider.class).getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get());
      }
      df.setLenient(true);
    }
    return df;
  }

  protected static class LocalDateColumnExtension<OWNER extends AbstractDateColumn> extends LocalColumnExtension<Date, OWNER> implements IDateColumnExtension<OWNER> {

    public LocalDateColumnExtension(OWNER owner) {
      super(owner);
    }
  }

  @Override
  protected IDateColumnExtension<? extends AbstractDateColumn> createLocalExtension() {
    return new LocalDateColumnExtension<AbstractDateColumn>(this);
  }

}
