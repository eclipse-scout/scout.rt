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
package org.eclipse.scout.rt.client.ui.form.fields.datefield;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.annotations.ClassId;
import org.eclipse.scout.commons.annotations.ConfigOperation;
import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.BooleanHolder;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftDateChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.DateFieldChains.DateFieldShiftTimeChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.datefield.IDateFieldExtension;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractBasicField;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.shared.ScoutTexts;

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
public abstract class AbstractDateField extends AbstractBasicField<Date> implements IDateField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDateField.class);

  private static enum ParseContext {
    Date, Time
  }

  private static final ThreadLocal<ParseContext> PARSE_CONTEXT = new ThreadLocal<AbstractDateField.ParseContext>();

  private IDateFieldUIFacade m_uiFacade;
  private String m_format;
  private long m_autoTimeMillis;
  private Date m_autoDate;

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
   * When a date without time is picked, this time value is used as hh/mm/ss.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(270)
  protected long getConfiguredAutoTimeMillis() {
    return 0;
  }

  /**
   * When a time without date is picked, this date value is used as day.<br>
   * <b>NOTE:</b> in case of null the current date will be taken.
   */
  @Order(270)
  protected Date getConfiguredAutoDate() {
    return null;
  }

  /**
   * Depending whether subclass overrides this method
   * <p>
   * Default is as follows<br>
   * Level 0: shift day up/down [UP, DOWN]<br>
   * Level 1: shift month up/down [shift-UP,shift-DOWN]<br>
   * Level 2: shift year up/down [ctrl-UP,ctrl-DOWN]
   * <p>
   * see {@link #adjustDate(int, int, int)}
   */
  @ConfigOperation
  protected void execShiftDate(int level, int value) throws ProcessingException {
    switch (level) {
      case 0: {
        adjustDate(value, 0, 0);
        break;
      }
      case 1: {
        adjustDate(0, value, 0);
        break;
      }
      case 2: {
        adjustDate(0, 0, value);
        break;
      }
    }
  }

  /**
   * Depending whether subclass overrides this method
   * <p>
   * Default is as follows<br>
   * Level 0: shift minute up/down [UP, DOWN]<br>
   * Level 1: shift hour up/down [shift-UP, shift-DOWN]<br>
   * Level 2: nop [ctrl-UP, ctrl-DOWN]<br>
   * <p>
   * see {@link #adjustDate(int, int, int)}
   */
  @ConfigOperation
  protected void execShiftTime(int level, int value) throws ProcessingException {
    switch (level) {
      case 0: {
        adjustTime(value, 0, 0);
        break;
      }
      case 1: {
        adjustTime(0, value, 0);
        break;
      }
      case 2: {
        adjustTime(0, 0, value);
        break;
      }
    }
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFormat(getConfiguredFormat());
    setHasDate(getConfiguredHasDate());
    setHasTime(getConfiguredHasTime());
    setAutoTimeMillis(getConfiguredAutoTimeMillis());
    setAutoDate(getConfiguredAutoDate());
  }

  /**
   * UpdateDisplayTextOnModify is not supported for DateTimeField.
   */
  @Override
  public final void setUpdateDisplayTextOnModify(boolean b) {
    super.setUpdateDisplayTextOnModify(b);
    preventUpdateDisplaytextOnModifiyOnDateTimeField();
  }

  @Override
  public void setFormat(String s) {
    m_format = s;
    if (isInitialized()) {
      if (shouldUpdateDisplayText(false)) {
        setDisplayText(interceptFormatValue(getValue()));
      }
    }
  }

  @Override
  public String getFormat() {
    return m_format;
  }

  @Override
  public boolean isHasTime() {
    return propertySupport.getPropertyBool(PROP_HAS_TIME);
  }

  @Override
  public void setHasTime(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_TIME, b);
    preventUpdateDisplaytextOnModifiyOnDateTimeField();
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  private void preventUpdateDisplaytextOnModifiyOnDateTimeField() {
    if (isUpdateDisplayTextOnModify() && isHasDate() && isHasTime()) {
      LOG.error("UpdateDisplayTextOnModify is not supported for combined Date Time Field.");
      setUpdateDisplayTextOnModify(false);
    }
  }

  @Override
  public boolean isHasDate() {
    return propertySupport.getPropertyBool(PROP_HAS_DATE);
  }

  @Override
  public void setHasDate(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_DATE, b);
    preventUpdateDisplaytextOnModifiyOnDateTimeField();
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  @Override
  public void setAutoTimeMillis(long l) {
    m_autoTimeMillis = l;
  }

  @Override
  public void setAutoDate(Date d) {
    m_autoDate = d;
  }

  @Override
  public void setAutoTimeMillis(int hour, int minute, int second) {
    setAutoTimeMillis(((hour * 60L + minute) * 60L + second) * 1000L);
  }

  @Override
  public long getAutoTimeMillis() {
    return m_autoTimeMillis;
  }

  public Date getAutoDate() {
    return m_autoDate;
  }

  @Override
  public void adjustDate(int days, int months, int years) {
    Date d = getValue();
    if (d == null) {
      d = applyAutoDate(d);
      d = applyAutoTime(d);
    }
    else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      cal.add(Calendar.DATE, days);
      cal.add(Calendar.MONTH, months);
      cal.add(Calendar.YEAR, years);
      d = cal.getTime();
    }
    setValue(d);
  }

  @Override
  public void adjustTime(int minutes, int hours, int reserved) {
    Date d = getValue();
    if (d == null) {
      d = applyAutoDate(d);
      d = applyAutoTime(d);
    }
    else {
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      cal.add(Calendar.MINUTE, minutes);
      cal.add(Calendar.HOUR_OF_DAY, hours);
      d = cal.getTime();
    }
    setValue(d);
  }

  @Override
  public IDateFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // format value for display
  @Override
  protected String formatValueInternal(Date validValue) {
    if (validValue == null) {
      return "";
    }
    DateFormat df = getDateFormat();
    String displayValue = df.format(validValue);
    return displayValue;
  }

  // validate value for ranges, mandatory, ...
  @Override
  protected Date validateValueInternal(Date rawValue) throws ProcessingException {
    Date validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    if (rawValue != null) {
      try {
        // apply format
        DateFormat df = getDateFormat();
        rawValue = df.parse(df.format(rawValue));
      }
      catch (Throwable t) {
        // nop, take raw value
      }
    }
    validValue = rawValue;
    return validValue;
  }

  /**
   * convert string to a real Date
   */
  @Override
  protected Date parseValueInternal(String text) throws ProcessingException {
    if (!StringUtility.hasText(text)) {
      return null;
    }
    Date d = null;
    boolean customFormat = (getFormat() != null);
    if (isHasDate() && isHasTime()) {
      ParseContext pctx = PARSE_CONTEXT.get();
      if (pctx == null) {
        d = parseDateTimeInternal(text, customFormat ? getDateFormat() : null);
      }
      else if (pctx == ParseContext.Date) {
        d = parseDateInternal(text, customFormat ? getIsolatedDateFormat() : null);
        Date currentValue = getValue();
        if (currentValue != null) {
          d = DateUtility.createDateTime(d, currentValue);
        }
      }
      else if (pctx == ParseContext.Time) {
        d = parseTimeInternal(text, customFormat ? getIsolatedTimeFormat() : null);
        Date currentValue = getValue();
        currentValue = applyAutoDate(currentValue);
        d = DateUtility.createDateTime(currentValue, d);
      }
    }
    else if (isHasDate() && !isHasTime()) {
      d = parseDateInternal(text, customFormat ? getDateFormat() : null);
    }
    else if (!isHasDate() && isHasTime()) {
      d = parseTimeInternal(text, customFormat ? getDateFormat() : null);
    }
    // truncate value
    DateFormat df = getDateFormat();
    try {
      //preserve the year, since it might have been truncated to previous century, ticket 87172
      Calendar cal = Calendar.getInstance();
      cal.setTime(d);
      int year = cal.get(Calendar.YEAR);
      d = df.parse(df.format(d));
      cal.setTime(d);
      cal.set(Calendar.YEAR, year);
      d = cal.getTime();
    }
    catch (ParseException e) {
      throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), e);
    }
    return d;
  }

  @Override
  public Double getTimeValue() {
    return DateUtility.convertDateToDoubleTime(getValue());
  }

  @Override
  public void setTimeValue(Double d) {
    setValue(DateUtility.convertDoubleTimeToDate(d));
  }

  /**
   * @since Build 200
   * @rn imo, 06.04.2006, only adjust date not date/time
   */
  private Date applyAutoTime(Date d) {
    if (d == null) {
      return d;
    }
    Calendar timeCal = Calendar.getInstance();
    long autoTime = getAutoTimeMillis();
    if (autoTime == 0L && isHasTime()) {
      // use current time
    }
    else {
      timeCal.set(Calendar.MILLISECOND, (int) (autoTime % 1000));
      autoTime = autoTime / 1000;
      timeCal.set(Calendar.SECOND, (int) (autoTime % 60));
      autoTime = autoTime / 60;
      timeCal.set(Calendar.MINUTE, (int) (autoTime % 60));
      autoTime = autoTime / 60;
      timeCal.set(Calendar.HOUR_OF_DAY, (int) (autoTime % 24));
    }
    Calendar c = Calendar.getInstance();
    c.setTime(d);
    c.set(Calendar.MILLISECOND, timeCal.get(Calendar.MILLISECOND));
    c.set(Calendar.SECOND, timeCal.get(Calendar.SECOND));
    c.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
    c.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
    d = c.getTime();
    return d;
  }

  private Date applyAutoDate(Date d) {
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
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat(), NlsLocale.get());
    }
    else {
      if (isHasDate() && !isHasTime()) {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
      }
      else if (!isHasDate() && isHasTime()) {
        df = DateFormat.getTimeInstance(DateFormat.SHORT, NlsLocale.get());
      }
      else {
        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get());
      }
      df.setLenient(true);
    }
    return df;
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
        catch (Throwable t) {
          LOG.error("could not isolate date pattern from '" + pat + "'", t);
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
        catch (Throwable t) {
          LOG.error("could not isolate time pattern from '" + pat + "'", t);
        }
      }
    }
    return null;
  }

  /**
   * parse date only
   */
  private Date parseDateInternal(String text, DateFormat defaultFormat) throws ProcessingException {
    Date retVal = null;
    BooleanHolder includesTime = new BooleanHolder(false);
    Matcher verboseDeltaMatcher = Pattern.compile("([+-])([0-9]+)").matcher(text);
    if (verboseDeltaMatcher.matches()) {
      int i = Integer.parseInt(verboseDeltaMatcher.group(2));
      if (verboseDeltaMatcher.group(1).equals("-")) {
        i = -i;
      }
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.DATE, i);
      retVal = cal.getTime();
    }
    else {
      retVal = parseDateFormatsInternal(text, defaultFormat, includesTime);
      if (retVal == null) {
        throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text));
      }
    }
    // range check -2000 ... +9000
    Calendar cal = Calendar.getInstance();
    cal.setTime(retVal);
    if (cal.get(Calendar.YEAR) < -2000) {
      cal.set(Calendar.YEAR, -2000);
    }
    if (cal.get(Calendar.YEAR) > 9000) {
      cal.set(Calendar.YEAR, 9000);
    }
    retVal = cal.getTime();
    //adapt time
    retVal = applyAutoTime(retVal);
    return retVal;
  }

  /**
   * parse date and time
   */
  private Date parseDateTimeInternal(String text, DateFormat defaultFormat) throws ProcessingException {
    Date retVal = null;
    if (!StringUtility.hasText(text)) {
      return null;
    }
    BooleanHolder includesTime = new BooleanHolder(false);
    Matcher verboseDeltaMatcher = Pattern.compile("([+-])([0-9]+)").matcher(text);
    if (verboseDeltaMatcher.matches()) {
      int i = Integer.parseInt(verboseDeltaMatcher.group(2));
      if ("-".equals(verboseDeltaMatcher.group(1))) {
        i = -i;
      }
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.DATE, i);
      retVal = cal.getTime();
    }
    else {
      retVal = parseDateTimeFormatsInternal(text, defaultFormat, includesTime);
      if (retVal == null) {
        throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text));
      }
    }
    // range check -2000 ... +9000
    Calendar cal = Calendar.getInstance();
    cal.setTime(retVal);
    if (cal.get(Calendar.YEAR) < -2000) {
      cal.set(Calendar.YEAR, -2000);
    }
    if (cal.get(Calendar.YEAR) > 9000) {
      cal.set(Calendar.YEAR, 9000);
    }
    retVal = cal.getTime();
    if (!includesTime.getValue()) {
      retVal = applyAutoTime(retVal);
    }
    return retVal;
  }

  /**
   * parse time only
   */
  private Date parseTimeInternal(String text, DateFormat defaultFormat) throws ProcessingException {
    Date retVal = null;
    if (!StringUtility.hasText(text)) {
      return null;
    }
    BooleanHolder includesTime = new BooleanHolder(false);
    retVal = parseTimeFormatsInternal(text, defaultFormat, includesTime);
    if (retVal == null) {
      throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text));
    }
    // range check -2000 ... +9000
    Calendar cal = Calendar.getInstance();
    cal.setTime(retVal);
    if (cal.get(Calendar.YEAR) < -2000) {
      cal.set(Calendar.YEAR, -2000);
    }
    if (cal.get(Calendar.YEAR) > 9000) {
      cal.set(Calendar.YEAR, 9000);
    }
    retVal = cal.getTime();
    // truncate value
    DateFormat df = getDateFormat();
    try {
      //re-set the year, since it might have been truncated to previous century, ticket 87172
      cal = Calendar.getInstance();
      cal.setTime(retVal);
      int year = cal.get(Calendar.YEAR);
      retVal = df.parse(df.format(retVal));
      cal.setTime(retVal);
      cal.set(Calendar.YEAR, year);
      retVal = cal.getTime();
    }
    catch (ParseException e) {
      throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), e);
    }
    return retVal;
  }

  private Date parseDateFormatsInternal(String text, DateFormat defaultFormat, BooleanHolder includesTime) {
    Date d;
    if (defaultFormat != null) {
      d = parseHelper(defaultFormat, text, includesTime);
      if (d != null) {
        return d;
      }
    }
    StringBuffer dateFormat = new StringBuffer();
    if (text.matches("[0-9]{6}")) {
      DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.SHORT, NlsLocale.get());
      if (templateFmt instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) templateFmt).toPattern();
        for (char c : pattern.toCharArray()) {
          if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            dateFormat.append(c);
          }
        }
        d = parseHelper(new SimpleDateFormat(dateFormat.toString(), NlsLocale.get()), text, includesTime);
        //no further checks
        return d;
      }
    }
    else if (text.matches("[0-9]{8}")) {
      DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
      if (templateFmt instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) templateFmt).toPattern();
        for (char c : pattern.toCharArray()) {
          if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            dateFormat.append(c);
          }
        }
        d = parseHelper(new SimpleDateFormat(dateFormat.toString(), NlsLocale.get()), text, includesTime);
        //no further checks
        return d;
      }
    }
    d = parseHelper(DateFormat.getDateInstance(DateFormat.SHORT, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateInstance(DateFormat.MEDIUM, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateInstance(DateFormat.LONG, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    //add convenience patterns for english locales
    if (NlsLocale.get().getLanguage().equals("en")) {
      d = parseHelper(new SimpleDateFormat("M / d / yy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
      d = parseHelper(new SimpleDateFormat("MMM d,yyyy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
      d = parseHelper(new SimpleDateFormat("MMMM d,yyyy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
    }
    return null;
  }

/*
      d=parseHelper(df, text, includesTime);
      if(d!=null) return d;
 */
  private Date parseDateTimeFormatsInternal(String text, DateFormat defaultFormat, BooleanHolder includesTime) {
    Date d = null;
    if (defaultFormat != null) {
      d = parseHelper(defaultFormat, text, includesTime);
      if (d != null) {
        return d;
      }
    }
    StringBuffer dateFormat = new StringBuffer();
    if (text.matches("[0-9]{6}")) {
      DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.SHORT, NlsLocale.get());
      if (templateFmt instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) templateFmt).toPattern();
        for (char c : pattern.toCharArray()) {
          if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            dateFormat.append(c);
          }
        }
        d = parseHelper(new SimpleDateFormat(dateFormat.toString(), NlsLocale.get()), text, includesTime);
        //no further checks
        return d;
      }
    }
    else if (text.matches("[0-9]{8}")) {
      DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.MEDIUM, NlsLocale.get());
      if (templateFmt instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) templateFmt).toPattern();
        for (char c : pattern.toCharArray()) {
          if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            dateFormat.append(c);
          }
        }
        d = parseHelper(new SimpleDateFormat(dateFormat.toString(), NlsLocale.get()), text, includesTime);
        //no further checks
        return d;
      }
    }
    //time
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, NlsLocale.get())).toPattern() + ":SSS", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    for (DateFormat t : new DateFormat[]{
        DateFormat.getDateInstance(DateFormat.SHORT, NlsLocale.get()),
        DateFormat.getDateInstance(DateFormat.MEDIUM, NlsLocale.get()),
        DateFormat.getDateInstance(DateFormat.LONG, NlsLocale.get())}) {
      if (t instanceof SimpleDateFormat) {
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " h:mm a", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " hhmm a", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " hmm a", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " h a", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " h:mma", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " hhmma", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " ha", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " H:mm", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " HHmm", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " HH", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " Hmm", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " H", NlsLocale.get()), text, includesTime);
        if (d != null) {
          return d;
        }
        d = parseHelper(t, text, includesTime);
        if (d != null) {
          return d;
        }
      }
    }
    //date
    //add convenience patterns for english locales
    if (NlsLocale.get().getLanguage().equals("en")) {
      d = parseHelper(new SimpleDateFormat("M / d / yy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
      d = parseHelper(new SimpleDateFormat("MMM d,yyyy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
      d = parseHelper(new SimpleDateFormat("MMMM d,yyyy", NlsLocale.get()), text, includesTime);
      if (d != null) {
        return d;
      }
    }
    return null;
  }

  private Date parseTimeFormatsInternal(String text, DateFormat defaultFormat, BooleanHolder includesTime) {
    Date d = null;
    if (defaultFormat != null) {
      d = parseHelper(defaultFormat, text, includesTime);
      if (d != null) {
        return d;
      }
    }
    //
    if (text.matches("[0-9]{3}")) {
      text = "0" + text; // "230" -> 02:30
    }
    if (text.matches("[0-9]{2}")) {
      int hours = Integer.parseInt(text);
      if (hours >= 24) {
        text = "00" + text; // "23" -> 23:00 but "30" -> 00:30
      }
    }
    if (defaultFormat != null) {
      d = parseHelper(defaultFormat, text, includesTime);
      if (d != null) {
        return d;
      }
    }
    d = parseHelper(DateFormat.getTimeInstance(DateFormat.SHORT, NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("h:mm a", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("hhmm a", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("hmm a", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("h a", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("h:mma", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("hhmma", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("ha", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("H:mm", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("HHmm", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("HH", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("Hmm", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    d = parseHelper(new SimpleDateFormat("H", NlsLocale.get()), text, includesTime);
    if (d != null) {
      return d;
    }
    return null;
  }

  private Date parseHelper(DateFormat df, String text, BooleanHolder includesTime) {
    // >>> ticket #105'126
    if (df instanceof SimpleDateFormat) {
      String pattern = ((SimpleDateFormat) df).toPattern();
      df = new SimpleDateFormat(pattern.replaceAll("yyyy", "yy"), NlsLocale.get()); // Always use century interpretation
    }
    // <<< ticket #105'126

    Date d = null;
    if (d == null) {
      try {
        df.setLenient(false);
        d = df.parse(text);
      }
      catch (ParseException e) {
        //nop
      }
    }
    // Allow "," instead of ".", because some keyboard layouts have a comma instead of
    // a dot on the numeric keypad
    if (d == null) {
      try {
        if (df instanceof SimpleDateFormat) {
          String pattern = ((SimpleDateFormat) df).toPattern();
          if (pattern.contains(".")) {
            SimpleDateFormat df2 = new SimpleDateFormat(pattern.replace('.', ','), NlsLocale.get());
            df2.setLenient(false);
            d = df2.parse(text);
          }
        }
      }
      catch (ParseException e) {
        //nop
      }
    }
    //eval
    if (d != null) {
      if (df instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) df).toPattern();
        if (pattern.matches(".*[hHm].*")) {
          includesTime.setValue(true);
        }
      }
      else {
        includesTime.setValue(true);
      }
    }
    return d;
  }

  private class P_UIFacade extends AbstractBasicField.P_UIFacade implements IDateFieldUIFacade {

    @Override
    public boolean setDateTextFromUI(String newDate) {
      if (!isHasDate()) {
        //nop
        return false;
      }
      if (newDate != null && newDate.length() == 0) {
        newDate = null;
      }
      setWhileTyping(false);
      if (!isHasTime()) {
        return parseValue(newDate);
      }
      if (newDate == null) {
        return parseValue(null);
      }
      //date part only
      try {
        PARSE_CONTEXT.set(ParseContext.Date);
        return parseValue(newDate);
      }
      finally {
        PARSE_CONTEXT.set(null);
      }
    }

    @Override
    public boolean setTimeTextFromUI(String newTime) {
      if (!isHasTime()) {
        //nop
        return false;
      }
      if (newTime != null && newTime.length() == 0) {
        newTime = null;
      }
      setWhileTyping(false);
      if (!isHasDate()) {
        return parseValue(newTime);
      }
      //time part
      try {
        PARSE_CONTEXT.set(ParseContext.Time);
        return parseValue(newTime);
      }
      finally {
        PARSE_CONTEXT.set(null);
      }
    }

    @Override
    public boolean setDateTimeTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      setWhileTyping(false);
      return parseValue(newText);
    }

    @Override
    public void setDateFromUI(Date d) {
      try {
        if (d != null) {
          // preserve time
          Date oldDate = getValue();
          if (oldDate != null) {
            Calendar calOld = Calendar.getInstance();
            calOld.setTime(oldDate);
            Calendar calNew = Calendar.getInstance();
            calNew.setTime(d);
            calNew.set(Calendar.HOUR, calOld.get(Calendar.HOUR));
            calNew.set(Calendar.HOUR_OF_DAY, calOld.get(Calendar.HOUR_OF_DAY));
            calNew.set(Calendar.MINUTE, calOld.get(Calendar.MINUTE));
            calNew.set(Calendar.SECOND, calOld.get(Calendar.SECOND));
            calNew.set(Calendar.MILLISECOND, calOld.get(Calendar.MILLISECOND));
            d = calNew.getTime();
          }
          else {
            d = applyAutoTime(d);
          }
        }
        setValue(d);
      }
      catch (Throwable t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
      }
    }

    @Override
    public void setTimeFromUI(Date d) {
      try {
        Date oldDate = getValue();
        if (d != null) {
          oldDate = applyAutoDate(oldDate);
          // preserve date
          Calendar calOld = Calendar.getInstance();
          calOld.setTime(oldDate);
          Calendar calNew = Calendar.getInstance();
          calNew.setTime(d);
          calNew.set(Calendar.YEAR, calOld.get(Calendar.YEAR));
          calNew.set(Calendar.MONTH, calOld.get(Calendar.MONTH));
          calNew.set(Calendar.DATE, calOld.get(Calendar.DATE));
          d = calNew.getTime();
        }
        else if (isHasDate() && oldDate != null) {
          d = applyAutoTime(oldDate);
        }
        setValue(d);
      }
      catch (Throwable t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
      }
    }

    @Override
    public void setDateTimeFromUI(Date d) {
      try {
        setValue(d);
      }
      catch (Throwable t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
      }
    }

    @Override
    public void fireDateShiftActionFromUI(int level, int value) {
      try {
        interceptShiftDate(level, value);
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
      catch (Throwable t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
      }
    }

    @Override
    public void fireTimeShiftActionFromUI(int level, int value) {
      try {
        interceptShiftTime(level, value);
      }
      catch (ProcessingException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
      catch (Throwable t) {
        BEANS.get(ExceptionHandler.class).handle(new ProcessingException("Unexpected", t));
      }
    }

    @Override
    public boolean setTextFromUI(String newText, boolean whileTyping) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      setWhileTyping(whileTyping);
      return parseValue(newText);
    }
  }

  protected final void interceptShiftTime(int level, int value) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    DateFieldShiftTimeChain chain = new DateFieldShiftTimeChain(extensions);
    chain.execShiftTime(level, value);
  }

  protected final void interceptShiftDate(int level, int value) throws ProcessingException {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    DateFieldShiftDateChain chain = new DateFieldShiftDateChain(extensions);
    chain.execShiftDate(level, value);
  }

  protected static class LocalDateFieldExtension<OWNER extends AbstractDateField> extends LocalBasicFieldExtension<Date, OWNER> implements IDateFieldExtension<OWNER> {

    public LocalDateFieldExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execShiftTime(DateFieldShiftTimeChain chain, int level, int value) throws ProcessingException {
      getOwner().execShiftTime(level, value);
    }

    @Override
    public void execShiftDate(DateFieldShiftDateChain chain, int level, int value) throws ProcessingException {
      getOwner().execShiftDate(level, value);
    }
  }

  @Override
  protected IDateFieldExtension<? extends AbstractDateField> createLocalExtension() {
    return new LocalDateFieldExtension<AbstractDateField>(this);
  }
}
