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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.commons.annotations.ConfigProperty;
import org.eclipse.scout.commons.annotations.ConfigPropertyValue;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractValueField;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.service.SERVICES;

public abstract class AbstractDateField extends AbstractValueField<Date> implements IDateField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractDateField.class);

  private IDateFieldUIFacade m_uiFacade;
  private String m_format;
  private long m_autoTimeMillis;

  public AbstractDateField() {
  }

  /*
   * Configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(240)
  @ConfigPropertyValue("true")
  protected boolean getConfiguredHasDate() {
    return true;
  }

  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(241)
  @ConfigPropertyValue("false")
  protected boolean getConfiguredHasTime() {
    return false;
  }

  /**
   * When a date without time is picked, this time value is used as hh/mm/ss.
   */
  @ConfigProperty(ConfigProperty.LONG)
  @Order(270)
  @ConfigPropertyValue("0")
  protected long getConfiguredAutoTimeMillis() {
    return 0;
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
  }

  public void setFormat(String s) {
    m_format = s;
    if (isInitialized()) {
      if (isAutoDisplayText()) {
        setDisplayText(execFormatValue(getValue()));
      }
    }
  }

  public String getFormat() {
    return m_format;
  }

  public boolean isHasTime() {
    return propertySupport.getPropertyBool(PROP_HAS_TIME);
  }

  public void setHasTime(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_TIME, b);
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  public boolean isHasDate() {
    return propertySupport.getPropertyBool(PROP_HAS_DATE);
  }

  public void setHasDate(boolean b) {
    propertySupport.setPropertyBool(PROP_HAS_DATE, b);
    if (isInitialized()) {
      setValue(getValue());
    }
  }

  public void setAutoTimeMillis(long l) {
    m_autoTimeMillis = l;
  }

  public void setAutoTimeMillis(int hour, int minute, int second) {
    setAutoTimeMillis(((hour * 60L + minute) * 60L + second) * 1000L);
  }

  public long getAutoTimeMillis() {
    return m_autoTimeMillis;
  }

  public void adjustDate(int days, int months, int years) {
    Date d = getValue();
    if (d == null) {
      d = new Date();
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

  public void adjustTime(int minutes, int hours, int reserved) {
    Date d = getValue();
    if (d == null) {
      d = new Date();
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

  public IDateFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // format value for display
  @Override
  protected String formatValueInternal(Date validValue) {
    if (validValue == null) return "";
    DateFormat df = getDateFormat();
    String displayValue = df.format(validValue);
    return displayValue;
  }

  // validate value for ranges, mandatory, ...
  @Override
  protected Date validateValueInternal(Date rawValue) throws ProcessingException {
    Date validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    return validValue;
  }

  // convert string to a real Date
  @Override
  protected Date parseValueInternal(String text) throws ProcessingException {
    Date retVal = null;
    if (text != null && text.trim().length() == 0) text = null;
    if (text == null) {
      return retVal;
    }
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
      List<DateFormat> dfList = createDateFormatsForParsing(text);
      ParseException pe = null;
      boolean includesTime = true;
      for (DateFormat df : dfList) {
        try {
          df.setLenient(false);
          retVal = df.parse(text);
          if (retVal != null) {
            if (df instanceof SimpleDateFormat) {
              String pattern = ((SimpleDateFormat) df).toPattern();
              includesTime = pattern.matches(".*[hHM].*");
            }
            else {
              includesTime = true;
            }
            break;
          }
        }
        catch (ParseException e) {
          if (pe == null) pe = e;
        }
      }
      if (retVal == null) {
        throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), pe);
      }
      // range check -2000 ... +9000
      Calendar cal = Calendar.getInstance();
      cal.setTime(retVal);
      if (cal.get(Calendar.YEAR) < -2000) cal.set(Calendar.YEAR, -2000);
      if (cal.get(Calendar.YEAR) > 9000) cal.set(Calendar.YEAR, 9000);
      retVal = cal.getTime();
      if (!includesTime) {
        retVal = applyAutoTime(retVal);
      }
    }
    // truncate value
    DateFormat df = getDateFormat();
    try {
      //re-set the year, since it might have been truncated to previous century, ticket 87172
      Calendar cal = Calendar.getInstance();
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

  // commodity value access
  public Double getTimeValue() {
    if (getValue() == null) return null;
    Calendar c = Calendar.getInstance();
    c.setTime(getValue());
    double t = ((c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) * 60 + c.get(Calendar.SECOND)) * 1000 + c.get(Calendar.MILLISECOND);
    Double d = new Double(t / MILLIS_PER_DAY);
    // range check;
    if (d.doubleValue() < 0) d = new Double(0);
    if (d.doubleValue() > 1) d = new Double(1);
    return d;
  }

  /**
   * @since Build 200
   * @rn imo, 06.04.2006, only adjust date not date/time
   */
  private Date applyAutoTime(Date d) {
    if (d == null) return d;
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

  public DateFormat getDateFormat() {
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat());
    }
    else {
      if (isHasTime()) {
        df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
      }
      else {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM);
      }
      df.setLenient(true);
    }
    return df;
  }

  public DateFormat getIsolatedDateFormat() {
    DateFormat f = getDateFormat();
    if (f instanceof SimpleDateFormat) {
      String pat = ((SimpleDateFormat) f).toPattern();
      int h = pat.toLowerCase().indexOf('h');
      if (h >= 0) {
        try {
          return new SimpleDateFormat(pat.substring(0, h).trim());
        }
        catch (Throwable t) {
          LOG.error("could not isolate date pattern from '" + pat + "'", t);
        }
      }
    }
    return f;
  }

  public DateFormat getIsolatedTimeFormat() {
    DateFormat f = getDateFormat();
    if (f instanceof SimpleDateFormat) {
      String pat = ((SimpleDateFormat) f).toPattern();
      int h = pat.toLowerCase().indexOf('h');
      if (h >= 0) {
        try {
          return new SimpleDateFormat(pat.substring(h).trim());
        }
        catch (Throwable t) {
          LOG.error("could not isolate time pattern from '" + pat + "'", t);
        }
      }
    }
    return null;
  }

  protected List<DateFormat> createDateFormatsForParsing(String text) {
    ArrayList<DateFormat> dfList = new ArrayList<DateFormat>();
    if (getFormat() != null) {
      dfList.add(new SimpleDateFormat(getFormat()));
    }
    DateFormat df = null;
    if (isHasTime()) {
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM));
      df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
      dfList.add(new SimpleDateFormat(((SimpleDateFormat) df).toPattern() + ":SSS"));
      dfList.add(df);
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM));
      dfList.add(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG));

      for (DateFormat t : new DateFormat[]{
                DateFormat.getDateInstance(DateFormat.SHORT),
                DateFormat.getDateInstance(DateFormat.MEDIUM),
                DateFormat.getDateInstance(DateFormat.LONG)}) {
        if (t instanceof SimpleDateFormat) {
          dfList.add(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " HH:mm"));
          dfList.add(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " HHmm"));
          dfList.add(new SimpleDateFormat(((SimpleDateFormat) t).toPattern() + " HH"));
          dfList.add(t);
        }
      }
    }
    else {
      StringBuffer dateFormat = new StringBuffer();
      if (text.matches("[0-9]{6}")) {
        DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.SHORT);
        if (templateFmt instanceof SimpleDateFormat) {
          String pattern = ((SimpleDateFormat) templateFmt).toPattern();
          for (char c : pattern.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
              dateFormat.append(c);
            }
          }
          dfList.add(new SimpleDateFormat(dateFormat.toString()));
        }
      }
      else if (text.matches("[0-9]{8}")) {
        DateFormat templateFmt = DateFormat.getDateInstance(DateFormat.MEDIUM);
        if (templateFmt instanceof SimpleDateFormat) {
          String pattern = ((SimpleDateFormat) templateFmt).toPattern();
          for (char c : pattern.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
              dateFormat.append(c);
            }
          }
          dfList.add(new SimpleDateFormat(dateFormat.toString()));
        }
      }
      df = DateFormat.getDateInstance(DateFormat.SHORT);
      dfList.add(df);
      df = DateFormat.getDateInstance(DateFormat.MEDIUM);
      dfList.add(df);
      df = DateFormat.getDateInstance(DateFormat.LONG);
      dfList.add(df);
      //add convenience patterns for english locales
      if (Locale.getDefault().getLanguage().equals("en")) {
        dfList.add(new SimpleDateFormat("M / d / yy"));
        dfList.add(new SimpleDateFormat("MMM d,yyyy"));
        dfList.add(new SimpleDateFormat("MMMM d,yyyy"));
      }
    }
    // Allow "," instead of ".", because some keyboard layouts have a comma instead of
    // a dot on the numeric keypad
    List<DateFormat> commaDfList = new ArrayList<DateFormat>();
    for (DateFormat format : dfList) {
      if (format instanceof SimpleDateFormat) {
        String pattern = ((SimpleDateFormat) format).toPattern();
        if (pattern.contains(".")) {
          commaDfList.add(new SimpleDateFormat(pattern.replace(".", ",")));
        }
      }
    }
    dfList.addAll(commaDfList);
    return dfList;
  }

  private class P_UIFacade implements IDateFieldUIFacade {

    public boolean setDateTextFromUI(String newDate) {
      if (!isHasDate()) {
        //nop
        return false;
      }
      if (newDate != null && newDate.length() == 0) {
        newDate = null;
      }
      // parse always, validity might change even if text is same
      Date currentValue = getValue();
      if (newDate == null) {
        return parseValue(null);
      }
      if (!isHasTime()) {
        return parseValue(newDate);
      }
      //add existing time
      String currentTime = getIsolatedTimeFormat().format(currentValue != null ? currentValue : new Date());
      return parseValue(newDate + " " + currentTime);
    }

    public boolean setTimeTextFromUI(String newTime) {
      if (!isHasTime()) {
        //nop
        return false;
      }
      if (newTime != null && newTime.length() == 0) {
        newTime = null;
      }
      // parse always, validity might change even if text is same
      Date currentValue = getValue();
      if (newTime == null && (currentValue == null || !isHasDate())) {
        return parseValue(null);
      }
      String currentDate = getIsolatedDateFormat().format(currentValue != null ? currentValue : new Date());
      if (newTime == null) {
        newTime = getIsolatedTimeFormat().format(currentValue != null ? currentValue : new Date());
      }
      return parseValue(currentDate + " " + newTime);
    }

    public boolean setDateTimeTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) {
        newText = null;
      }
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }

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
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }

    public void setTimeFromUI(Date d) {
      try {
        Date oldDate = getValue();
        if (d != null) {
          // preserve date
          if (oldDate != null) {
            Calendar calOld = Calendar.getInstance();
            calOld.setTime(oldDate);
            Calendar calNew = Calendar.getInstance();
            calNew.setTime(d);
            calNew.set(Calendar.YEAR, calOld.get(Calendar.YEAR));
            calNew.set(Calendar.MONTH, calOld.get(Calendar.MONTH));
            calNew.set(Calendar.DATE, calOld.get(Calendar.DATE));
            d = calNew.getTime();
          }
        }
        else if (isHasDate() && oldDate != null) {
          d = applyAutoTime(oldDate);
        }
        setValue(d);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }

    public void setDateTimeFromUI(Date d) {
      try {
        setValue(d);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }

    public void fireDateShiftActionFromUI(int level, int value) {
      try {
        execShiftDate(level, value);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }

    public void fireTimeShiftActionFromUI(int level, int value) {
      try {
        execShiftTime(level, value);
      }
      catch (ProcessingException e) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(e);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }
  }
}
