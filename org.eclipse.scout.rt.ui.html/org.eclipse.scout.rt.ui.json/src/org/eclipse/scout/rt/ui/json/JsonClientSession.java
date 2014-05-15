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
package org.eclipse.scout.rt.ui.json;

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
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ILocaleListener;
import org.eclipse.scout.rt.client.LocaleChangeEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonClientSession extends AbstractJsonRenderer<IClientSession> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JsonClientSession.class);

  private ILocaleListener m_localeListener;
  private boolean m_localeManagedByModel;

  public JsonClientSession(IClientSession modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    m_localeManagedByModel = false;
  }

  @Override
  public String getObjectType() {
    return "ClientSession";
  }

  @Override
  protected void attachModel() {
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      getModelObject().addLocaleListener(m_localeListener);
    }

    if (!getModelObject().isActive()) {
      //FIXME copied from session service. Moved here to be able to attach locale listener first
      ClientSyncJob job = new ClientSyncJob("Session startup", getModelObject()) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          getCurrentSession().startSession(Activator.getDefault().getBundle());
        }
      };
      //must run now to use correct jaas and subject context of calling thread. Especially relevant when running in a servlet thread (rwt)
      job.runNow(new NullProgressMonitor());
    }
  }

  @Override
  protected void detachModel() {
    if (m_localeListener != null) {
      getModelObject().removeLocaleListener(m_localeListener);
      m_localeListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putProperty(json, "desktop", modelObjectToJson(getModelObject().getDesktop()));
    putProperty(json, "locale", localeToJson(getModelObject().getLocale()));
    return json;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) {
  }

  protected void processRequestLocale(final Locale locale) {
    if (m_localeManagedByModel) {
      return;
    }
    new ClientSyncJob("Desktop opened", getJsonSession().getClientSession()) {
      @Override
      protected void runVoid(IProgressMonitor monitor) throws Throwable {
        if (!getModelObject().getLocale().equals(locale)) {
          getModelObject().setLocale(locale);
        }
      }
    }.runNow(new NullProgressMonitor());
  }

  protected JSONObject decimalFormatSymbolsToJson(DecimalFormatSymbols symbols) {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("digit", String.valueOf(symbols.getDigit()));
      jsonObject.put("zeroDigit", String.valueOf(symbols.getZeroDigit()));
      jsonObject.put("decimalSeparator", String.valueOf(symbols.getDecimalSeparator()));
      jsonObject.put("groupingSeparator", String.valueOf(symbols.getGroupingSeparator()));
      jsonObject.put("minusSign", String.valueOf(symbols.getMinusSign()));
      jsonObject.put("patternSeparator", String.valueOf(symbols.getPatternSeparator()));
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
    return jsonObject;
  }

  protected JSONObject dateFormatSymbolsToJson(DateFormatSymbols symbols) {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("months", new JSONArray(symbols.getMonths()));
      jsonObject.put("monthsShort", new JSONArray(symbols.getShortMonths()));
      jsonObject.put("weekdays", new JSONArray(Arrays.copyOfRange(symbols.getWeekdays(), 1, 8)));
      jsonObject.put("weekdaysShort", new JSONArray(Arrays.copyOfRange(symbols.getShortWeekdays(), 1, 8)));
      jsonObject.put("am", symbols.getAmPmStrings()[Calendar.AM]);
      jsonObject.put("pm", symbols.getAmPmStrings()[Calendar.PM]);
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
    return jsonObject;
  }

  protected JSONObject localeToJson(Locale locale) {
    JSONObject jsonObject = new JSONObject();
    try {
      DecimalFormat defaultDecimalFormat = getDefaultDecimalFormat(locale);
      SimpleDateFormat defaultDateFormat = getDefaultSimpleDateFormat(locale);
      jsonObject.put("decimalFormatPatternDefault", defaultDecimalFormat.toLocalizedPattern());
      jsonObject.put("dateFormatPatternDefault", defaultDateFormat.toPattern());
      jsonObject.put("decimalFormatSymbols", decimalFormatSymbolsToJson(defaultDecimalFormat.getDecimalFormatSymbols()));
      jsonObject.put("dateFormatSymbols", dateFormatSymbolsToJson(defaultDateFormat.getDateFormatSymbols()));
    }
    catch (JSONException e) {
      throw new JsonException(e);
    }
    return jsonObject;
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
        if (isInitialized()) {//If Locale changes during session startup (execLoadSession) it is not necessary to notify the gui
          getJsonSession().currentJsonResponse().addActionEvent("localeChanged", getId(), localeToJson(locale));
        }
      }
    }
  }

}
