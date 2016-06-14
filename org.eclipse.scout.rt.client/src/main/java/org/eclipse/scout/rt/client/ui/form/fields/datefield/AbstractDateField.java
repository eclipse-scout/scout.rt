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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.IDateFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.client.ui.form.fields.ParsingFailedStatus;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ProcessingStatus;
import org.eclipse.scout.rt.platform.exception.VetoException;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.date.DateFormatProvider;
import org.eclipse.scout.rt.platform.util.date.DateUtility;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.TEXTS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Value field for date and time values.
 * <p>
 * <strong>Note:</strong> By default, all {@link java.util.Date} objects are converted to
 * {@link org.eclipse.scout.rt.shared.servicetunnel.StaticDate StaticDate} during serialization and converted back to
 * <code>Date</code> objects during de-serialization in order to be independent of time zone and daylight saving time.
 * I.e. the string representation of a date stays the same when it is sent through the service tunnel, but not the date
 * itself. {@link org.eclipse.scout.rt.client.ui.form.fields.datefield.AbstractUTCDateField AbstractUTCDateField} can be
 * used instead, if this is not the desired behavior.
 * <p>
 * <strong>Example:</strong>
 * </p>
 * <blockquote>
 *
 * <pre>
 * //Consider a form containing a date field:
 * ...
 * public class MyDateField extends AbstractDateField {
 * }
 *
 * //Use SimpleDateFormat to get a String representation of the date.
 * Date d = formData.getMyDate().getValue();
 * DateFormat dateFormat = new SimpleDateFormat(&quot;yyyy.MM.dd - HH:mm:ss&quot;, Locale.ENGLISH);
 * String formattedDate = dateFormat.format(d);
 * System.out.println(formattedDate);
 *
 * //Send the formData to the server using a service:
 * BEANS.get(IMyService.class).load(MyFormData formData)
 *
 * //Use SimpleDateFormat to get a String representation of the date in the service implementation.
 * public MyFormData load(MyFormData formData) {
 *     Date d = formData.getMyDate().getValue();
 *     DateFormat dateFormat = new SimpleDateFormat(&quot;yyyy.MM.dd - HH:mm:ss&quot;, Locale.ENGLISH);
 *     String formattedDate = dateFormat.format(d);
 *     System.out.println(formattedDate);
 * }
 * //The two println statements result in the same value on server and client independent of the timezone of the client and server.
 * </pre>
 *
 * </blockquote>
 * </p>
 * <p>
 * <strong>Default values:</strong> Default hasDate=true and hasTime=false
 * </p>
 *
 * @see org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelObjectReplacer ServiceTunnelObjectReplacer
 */
@ClassId("f73eed8c-1e70-4903-a23f-4a29d884e5ea")
public abstract class AbstractDateField extends AbstractValueField<Date> implements IDateField {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractDateField.class);

  private IDateFieldUIFacade m_uiFacade;

  public AbstractDateField() {
    this(true);
  }

  public AbstractDateField(boolean callInitializer) {
    super(callInitializer);
  }

  /**
   * The date/time format, for a description see {@link SimpleDateFormat}
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(231)
  protected String getConfiguredDateFormatPattern() {
    return null;
  }

  @ConfigProperty(ConfigProperty.STRING)
  @Order(232)
  protected String getConfiguredTimeFormatPattern() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  protected boolean getConfiguredHasDate() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(241)
  protected boolean getConfiguredHasTime() {
    return false;
  }

  /**
   * Date to be used when setting a value "automatically", e.g. when the date picker is opened initially or when a date
   * or time is entered and the other component has to be filled. If no auto date is set (which is the default), the
   * current date and time is used.
   */
  @Order(270)
  protected Date getConfiguredAutoDate() {
    return null;
  }

  /**
   * <b>Important:</b> Make sure that this method only uses formats that are supported by the UI. Otherwise, a formatted
   * date cannot be parsed again.
   */
  @Override
  protected String execFormatValue(Date value) {
    return super.execFormatValue(value);
  }

  @Override
  protected void initConfig() {
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    super.initConfig();

    setHasDate(getConfiguredHasDate());
    setHasTime(getConfiguredHasTime());
    setAutoDate(getConfiguredAutoDate());

    setDateFormatPattern(getConfiguredDateFormatPattern());
    setTimeFormatPattern(getConfiguredTimeFormatPattern());
    setFormat(getConfiguredFormat());
    setAllowedDates(Collections.<Date> emptyList());
  }

  @Override
  public void setFormat(String format) {
    format = checkFormatPatternSupported(format);

    String dateFormatPattern = null;
    String timeFormatPattern = null;
    if (format != null) {
      // Try to extract date and time parts of pattern
      int h = format.toLowerCase().indexOf('h');
      if (h >= 0) {
        dateFormatPattern = format.substring(0, h).trim();
        timeFormatPattern = format.substring(h).trim();
      }
      else {
        if (isHasDate()) {
          dateFormatPattern = format;
          timeFormatPattern = null;
          if (isHasTime()) {
            LOG.warn("Could not extract time part from pattern '{}', using default pattern.", format);
          }
        }
        else {
          dateFormatPattern = null;
          timeFormatPattern = (isHasTime() ? format : null);
        }
      }
    }
    setDateFormatPattern(dateFormatPattern);
    setTimeFormatPattern(timeFormatPattern);
  }

  @Override
  public String getFormat() {
    String s = "";
    if (isHasDate()) {
      s = StringUtility.join(" ", s, getDateFormatPattern());
    }
    if (isHasTime()) {
      s = StringUtility.join(" ", s, getTimeFormatPattern());
    }
    return s;
  }

  @Override
  public void setDateFormatPattern(String dateFormatPattern) {
    dateFormatPattern = checkFormatPatternSupported(dateFormatPattern);
    if (dateFormatPattern == null) {
      dateFormatPattern = BEANS.get(DateFormatProvider.class).getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_DATE, NlsLocale.get());
    }
    propertySupport.setPropertyString(PROP_DATE_FORMAT_PATTERN, dateFormatPattern);
    // Always update display text (format may be the same, but language might have changed)
    refreshDisplayText();
  }

  @Override
  public String getDateFormatPattern() {
    return propertySupport.getPropertyString(PROP_DATE_FORMAT_PATTERN);
  }

  @Override
  public void setTimeFormatPattern(String timeFormatPattern) {
    timeFormatPattern = checkFormatPatternSupported(timeFormatPattern);
    if (timeFormatPattern == null) {
      timeFormatPattern = BEANS.get(DateFormatProvider.class).getDateFormatPattern(DateFormatProvider.PATTERN_STYLE_ISOLATED_TIME, NlsLocale.get());
    }
    propertySupport.setPropertyString(PROP_TIME_FORMAT_PATTERN, timeFormatPattern);
    // Always update display text (format may be the same, but language might have changed)
    refreshDisplayText();
  }

  @Override
  public String getTimeFormatPattern() {
    return propertySupport.getPropertyString(PROP_TIME_FORMAT_PATTERN);
  }

  protected String checkFormatPatternSupported(String formatPattern) {
    // FIXME bsh: How to implement?
    return formatPattern;
  }

  @Override
  public boolean isHasTime() {
    return propertySupport.getPropertyBool(PROP_HAS_TIME);
  }

  @Override
  public void setHasTime(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_TIME, b);
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public boolean isHasDate() {
    return propertySupport.getPropertyBool(PROP_HAS_DATE);
  }

  @Override
  public void setHasDate(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_DATE, b);
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public void setAutoDate(Date autoDate) {
    propertySupport.setProperty(PROP_AUTO_DATE, autoDate);
  }

  @Override
  public Date getAutoDate() {
    return (Date) propertySupport.getProperty(PROP_AUTO_DATE);
  }

  @Override
  public IDateFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  @Override
  protected String formatValueInternal(Date validValue) {
    if (validValue == null) {
      return "";
    }
    DateFormat dateFormat;
    StringBuilder sb = new StringBuilder();
    dateFormat = getIsolatedDateFormat();
    sb.append(dateFormat == null ? "" : dateFormat.format(validValue));
    sb.append("\n");
    dateFormat = getIsolatedTimeFormat();
    sb.append(dateFormat == null ? "" : dateFormat.format(validValue));
    return sb.toString();
  }

  @Override
  protected Date validateValueInternal(Date rawValue) {
    rawValue = super.validateValueInternal(rawValue);

    if (rawValue == null) {
      return null;
    }

    // Check if date is allowed (if allowed dates are set)
    if (getAllowedDates().size() > 0) {
      Date truncDate = DateUtility.truncDate(rawValue);
      boolean found = false;
      for (Date allowedDate : getAllowedDates()) {
        if (allowedDate.compareTo(truncDate) == 0) {
          found = true;
          break;
        }
      }
      if (!found) {
        throw new VetoException(new ProcessingStatus(TEXTS.get("DateIsNotAllowed"), IStatus.ERROR));
      }
    }

    return rawValue;
  }

  @Override
  public Double getTimeValue() {
    return DateUtility.convertDateToDoubleTime(getValue());
  }

  @Override
  public void setTimeValue(Double d) {
    setValue(DateUtility.convertDoubleTimeToDate(d));
  }

  protected Date applyAutoDate(Date d) {
    if (d != null) {
      return d;
    }
    d = getAutoDate();
    if (d == null) {
      // use today's date
      d = new Date();
    }
    return d;
  }

  @Override
  public DateFormat getDateFormat() {
    String format = getFormat();
    if (format != null) {
      return new SimpleDateFormat(format, NlsLocale.get());
    }
    return null;
  }

  @Override
  public DateFormat getIsolatedDateFormat() {
    DateFormat f = getDateFormat();
    if (f instanceof SimpleDateFormat) {
      String pat = ((SimpleDateFormat) f).toPattern();
      int h = pat.toLowerCase().indexOf('h');
      if (h >= 0) {
        try {
          return new SimpleDateFormat(pat.substring(0, h).trim(), NlsLocale.get());
        }
        catch (Exception e) {
          LOG.error("could not isolate date pattern from '{}'", pat, e);
        }
      }
    }
    return f;
  }

  @Override
  public DateFormat getIsolatedTimeFormat() {
    DateFormat f = getDateFormat();
    if (f instanceof SimpleDateFormat) {
      String pat = ((SimpleDateFormat) f).toPattern();
      int h = pat.toLowerCase().indexOf('h');
      if (h >= 0) {
        try {
          return new SimpleDateFormat(pat.substring(h).trim(), NlsLocale.get());
        }
        catch (Exception e) {
          LOG.error("could not isolate time pattern from '{}'", pat, e);
        }
      }
    }
    return null;
  }

  @Override
  public void setAllowedDates(List<Date> allowedDates) {
    if (allowedDates == null) {
      allowedDates = Collections.emptyList();
    }
    else {
      // Make sure each date is truncated and the list of dates is ordered by date
      List<Date> sortedTruncatedDates = new ArrayList<>(allowedDates.size());
      for (Date date : allowedDates) {
        sortedTruncatedDates.add(DateUtility.truncDate(date));
      }
      Collections.sort(sortedTruncatedDates);
      allowedDates = sortedTruncatedDates;
    }
    propertySupport.setProperty(PROP_ALLOWED_DATES, allowedDates);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Date> getAllowedDates() {
    return new ArrayList<>((List<Date>) propertySupport.getProperty(PROP_ALLOWED_DATES));
  }

  protected class P_UIFacade implements IDateFieldUIFacade {

    @Override
    public void setDateTimeFromUI(Date date) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      setValue(date);
    }

    @Override
    public void setDisplayTextFromUI(String text) {
      if (!isEnabled() || !isVisible()) {
        return;
      }
      setDisplayText(text);
    }

    @Override
    public void setParseErrorFromUI() {
      String invalidMessage = getDisplayText().replace("\n", " ");
      ParsingFailedStatus status = new ParsingFailedStatus(ScoutTexts.get("InvalidValueMessageX", invalidMessage), getDisplayText());
      addErrorStatus(status);
    }

    @Override
    public void removeParseErrorFromUI() {
      removeErrorStatus(ParsingFailedStatus.class);
    }
  }

  protected static class LocalDateFieldExtension<OWNER extends AbstractDateField> extends LocalValueFieldExtension<Date, OWNER> implements IDateFieldExtension<OWNER> {

    public LocalDateFieldExtension(OWNER owner) {
      super(owner);
    }

  }

  @Override
  protected IDateFieldExtension<? extends AbstractDateField> createLocalExtension() {
    return new LocalDateFieldExtension<AbstractDateField>(this);
  }
}
