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
package org.eclipse.scout.rt.ui.html.json;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ILocaleListener;
import org.eclipse.scout.rt.client.LocaleChangeEvent;
import org.eclipse.scout.rt.ui.html.Activator;
import org.json.JSONObject;

public class JsonClientSession extends AbstractJsonAdapter<IClientSession> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonClientSession.class);

  private ILocaleListener m_localeListener;
  private boolean m_localeManagedByModel;
  private boolean m_started;

  public JsonClientSession(IClientSession model, IJsonSession jsonSession, String id) {
    super(model, jsonSession, id);
    m_localeManagedByModel = false;
    m_started = false;
  }

  @Override
  public String getObjectType() {
    //Currently there is no representation on client side
    return null;
  }

  void startUp() {
    // same as in attachModel. Necessary for locale workaround. Find better solution in scout model
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      getModel().addLocaleListener(m_localeListener);
    }
    if (!getModel().isActive()) {
      // FIXME CGU: copied from session service. Moved here to be able to attach locale listener first
      getModel().startSession(Activator.getDefault().getBundle());
    }
    if (!getModel().getDesktop().isOpened()) {
      getModel().getDesktop().getUIFacade().fireDesktopOpenedFromUI();
    }
    if (!getModel().getDesktop().isGuiAvailable()) {
      getModel().getDesktop().getUIFacade().fireGuiAttached();
    }

    // attach child adapters - we cannot do this in attachModel() as normal
    // since the desktop is not yet created when attachModel runs.
    // see AbstractJsonSession#init()
    attachAdapter(getModel().getDesktop());
    m_started = true;
  }

  public boolean isStarted() {
    return m_started;
  }

  @Override
  public void dispose() {
    super.dispose();
    if (m_localeListener != null) {
      getModel().removeLocaleListener(m_localeListener);
      m_localeListener = null;
    }
  }

  @Override
  protected void detachModel() {
    if (m_localeListener != null) {
      getModel().removeLocaleListener(m_localeListener);
      m_localeListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "desktop", getModel().getDesktop());
    putProperty(json, "locale", localeToJson(getModel().getLocale()));
    return json;
  }

  protected void processRequestLocale(final Locale locale) {
    if (m_localeManagedByModel) {
      return;
    }
    ClientJob job = new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (!getModel().getLocale().equals(locale)) {
          getModel().setLocale(locale);
        }
      }
    };
    job.runNow(new NullProgressMonitor());
    try {
      job.throwOnError();
    }
    catch (ProcessingException e) {
      throw new JsonException(e);
    }
  }

  protected JSONObject decimalFormatSymbolsToJson(DecimalFormatSymbols symbols) {
    JSONObject json = new JSONObject();
    putProperty(json, "digit", String.valueOf(symbols.getDigit()));
    putProperty(json, "zeroDigit", String.valueOf(symbols.getZeroDigit()));
    putProperty(json, "decimalSeparator", String.valueOf(symbols.getDecimalSeparator()));
    putProperty(json, "groupingSeparator", String.valueOf(symbols.getGroupingSeparator()));
    putProperty(json, "minusSign", String.valueOf(symbols.getMinusSign()));
    putProperty(json, "patternSeparator", String.valueOf(symbols.getPatternSeparator()));
    return json;
  }

  protected JSONObject dateFormatSymbolsToJson(DateFormatSymbols symbols) {
    JSONObject json = new JSONObject();
    putProperty(json, "months", JsonObjectUtility.newJSONArray(symbols.getMonths()));
    putProperty(json, "monthsShort", JsonObjectUtility.newJSONArray(symbols.getShortMonths()));
    putProperty(json, "weekdays", JsonObjectUtility.newJSONArray(Arrays.copyOfRange(symbols.getWeekdays(), 1, 8)));
    putProperty(json, "weekdaysShort", JsonObjectUtility.newJSONArray(Arrays.copyOfRange(symbols.getShortWeekdays(), 1, 8)));
    putProperty(json, "am", symbols.getAmPmStrings()[Calendar.AM]);
    putProperty(json, "pm", symbols.getAmPmStrings()[Calendar.PM]);
    return json;
  }

  protected JSONObject localeToJson(Locale locale) {
    JSONObject json = new JSONObject();
    DecimalFormat defaultDecimalFormat = getDefaultDecimalFormat(locale);
    SimpleDateFormat defaultDateFormat = getDefaultSimpleDateFormat(locale);
    putProperty(json, "languageTag", locale.toLanguageTag());
    putProperty(json, "decimalFormatPatternDefault", defaultDecimalFormat.toLocalizedPattern());
    putProperty(json, "dateFormatPatternDefault", defaultDateFormat.toPattern());
    putProperty(json, "decimalFormatSymbols", decimalFormatSymbolsToJson(defaultDecimalFormat.getDecimalFormatSymbols()));
    putProperty(json, "dateFormatSymbols", dateFormatSymbolsToJson(defaultDateFormat.getDateFormatSymbols()));
    return json;
  }

  private static DecimalFormat getDefaultDecimalFormat(Locale locale) {
    NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
    if (numberFormat instanceof DecimalFormat) {
      return (DecimalFormat) numberFormat;
    }
    LOG.info("No locale specific decimal format available, using default locale");
    return new DecimalFormat();
  }

  private static SimpleDateFormat getDefaultSimpleDateFormat(Locale locale) {
    DateFormat format = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
    if (format instanceof SimpleDateFormat) {
      return (SimpleDateFormat) format;
    }
    LOG.info("No locale specific date format available, using default locale");
    return new SimpleDateFormat();
  }

  private class P_LocaleListener implements ILocaleListener {
    @Override
    public void localeChanged(LocaleChangeEvent event) {
      final Locale locale = event.getLocale();
      if (!CompareUtility.equals(getJsonSession().currentHttpRequest().getLocale(), locale)) {
        m_localeManagedByModel = true;
        // If Locale changes during session startup (execLoadSession) it is not necessary to notify the GUI
        if (isStarted()) {
          addActionEvent("localeChanged", localeToJson(locale));
        }
      }
    }
  }

}
