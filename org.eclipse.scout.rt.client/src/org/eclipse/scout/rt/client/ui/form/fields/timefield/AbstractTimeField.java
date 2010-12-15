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
package org.eclipse.scout.rt.client.ui.form.fields.timefield;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.scout.commons.TypeCastUtility;
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

public abstract class AbstractTimeField extends AbstractValueField<Double> implements ITimeField {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractTimeField.class);

  private ITimeFieldUIFacade m_uiFacade;
  private String m_format;

  public AbstractTimeField() {
  }

  /*
   * configuration
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  @ConfigPropertyValue("null")
  protected String getConfiguredFormat() {
    return null;
  }

  @Override
  protected void initConfig() {
    m_uiFacade = new P_UIFacade();
    super.initConfig();
    setFormat(getConfiguredFormat());
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

  public Double getTimeValue() {
    return getValue();
  }

  public ITimeFieldUIFacade getUIFacade() {
    return m_uiFacade;
  }

  // format value for display
  @Override
  protected String formatValueInternal(Double validValue) {
    if (validValue == null) return "";

    long time = (long) (validValue.doubleValue() * MILLIS_PER_DAY + 0.5);
    Calendar c = Calendar.getInstance();
    c.set(Calendar.MILLISECOND, (int) (time % 1000));
    time = time / 1000;
    c.set(Calendar.SECOND, (int) (time % 60));
    time = time / 60;
    c.set(Calendar.MINUTE, (int) (time % 60));
    time = time / 60;
    c.set(Calendar.HOUR_OF_DAY, (int) (time % 24));
    DateFormat df = getDateFormat();
    String displayValue = df.format(c.getTime());
    return displayValue;
  }

  // validate value for ranges, mandatory, ...
  @Override
  protected Double validateValueInternal(Double rawValue) throws ProcessingException {
    Double validValue = null;
    rawValue = super.validateValueInternal(rawValue);
    validValue = rawValue;
    //
    if (validValue != null) {
      double d = validValue.doubleValue();
      if (d < 0) {
        validValue = new Double(1.0 - ((-d) - Math.floor(-d)));
      }
      else if (d > 1) {
        validValue = new Double(d - Math.floor(d));
      }
    }
    return validValue;
  }

  // convert string to time
  @Override
  protected Double parseValueInternal(String text) throws ProcessingException {
    Double retVal = null;
    if (text != null && text.trim().length() == 0) text = null;

    if (text != null) {
      if (text.matches("[0-9]{3}")) {
        text = "0" + text; // "230" -> 02:30
      }
      if (text.matches("[0-9]{2}")) {
        int hours = TypeCastUtility.castValue(text, Integer.class).intValue();
        if (hours >= 24) {
          text = "00" + text; // "23" -> 23:00 but "30" -> 00:30
        }
      }
      Date time = null;
      ArrayList<DateFormat> dfList = getParseDateFormat();
      ParseException pe = null;
      for (DateFormat df : dfList) {
        try {
          time = df.parse(text);
          if (time != null) {
            break;
          }
        }
        catch (ParseException e) {
          if (pe == null) pe = e;
        }
      }
      if (time == null) {
        throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), pe);
      }
      // truncate value
      DateFormat df = getDateFormat();
      try {
        time = df.parse(df.format(time));
      }
      catch (ParseException e) {
        throw new ProcessingException(ScoutTexts.get("InvalidValueMessageX", text), e);
      }
      Calendar c = Calendar.getInstance();
      c.setTime(time);
      double t = ((c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)) * 60 + c.get(Calendar.SECOND)) * 1000 + c.get(Calendar.MILLISECOND);
      retVal = new Double(t / MILLIS_PER_DAY);
    }

    return retVal;
  }

  private DateFormat getDateFormat() {
    DateFormat df = null;
    if (getFormat() != null) {
      df = new SimpleDateFormat(getFormat());
    }
    else {
      df = DateFormat.getTimeInstance(DateFormat.SHORT);
      df.setLenient(true);
    }
    return df;
  }

  private ArrayList<DateFormat> getParseDateFormat() {
    ArrayList<DateFormat> dfList = new ArrayList<DateFormat>();
    if (getFormat() != null) {
      dfList.add(new SimpleDateFormat(getFormat()));
    }
    else {
      dfList.add(DateFormat.getTimeInstance(DateFormat.SHORT));
      dfList.add(new SimpleDateFormat("HHmm")); // "1230" --> 12:30
      dfList.add(new SimpleDateFormat("HH")); // "12" --> 12:00
    }
    return dfList;
  }

  private class P_UIFacade implements ITimeFieldUIFacade {

    public void setTimeFromUI(Double d) {
      try {
        setValue(d);
      }
      catch (Throwable t) {
        SERVICES.getService(IExceptionHandlerService.class).handleException(new ProcessingException("Unexpected", t));
      }
    }

    public boolean setTextFromUI(String newText) {
      if (newText != null && newText.length() == 0) newText = null;
      // parse always, validity might change even if text is same
      return parseValue(newText);
    }
  }
}
