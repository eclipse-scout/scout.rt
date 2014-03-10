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

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ILocaleListener;
import org.eclipse.scout.rt.client.LocaleChangeEvent;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonClientSession extends AbstractJsonRenderer<IClientSession> {
  private ILocaleListener m_localeListener;
  private boolean m_localeManagedByModel;
  private JsonDesktop m_jsonDesktop;

  public JsonClientSession(IClientSession modelObject, IJsonSession jsonSession, String id) {
    super(modelObject, jsonSession, id);
    m_localeManagedByModel = false;
  }

  @Override
  protected void attachModel() {
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      getModelObject().addLocaleListener(m_localeListener);
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
  public void init() throws JsonUIException {
    super.init();

    m_jsonDesktop = new JsonDesktop(getModelObject().getDesktop(), getJsonSession());
    m_jsonDesktop.init();
  }

  @Override
  public JSONObject toJson() throws JsonUIException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("desktop", m_jsonDesktop.toJson());
      jsonObject.put("locale", localeToJson(getModelObject().getLocale()));
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
    return jsonObject;
  }

  @Override
  public void handleUiEvent(JsonEvent event, JsonResponse res) throws JsonUIException {
    //FIXME A little strange that startup doesn't actually trigger startup of the client session
    if ("startup".equals(event.getEventType())) {
      handleUiStartupEvent(event, res);
    }
  }

  protected void handleUiStartupEvent(JsonEvent event, JsonResponse res) throws JsonUIException {
    res.addActionEvent("initialized", getId(), toJson());
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

  protected JSONObject decimalFormatSymbolsToJson(DecimalFormatSymbols symbols) throws JsonUIException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("groupingSeparator", String.valueOf(symbols.getGroupingSeparator()));
      jsonObject.put("decimalSeparator", String.valueOf(symbols.getDecimalSeparator()));
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
    return jsonObject;
  }

  protected JSONObject localeToJson(Locale locale) throws JsonUIException {
    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put("decimalFormatSymbols", decimalFormatSymbolsToJson(new DecimalFormatSymbols(locale)));
    }
    catch (JSONException e) {
      throw new JsonUIException(e);
    }
    return jsonObject;
  }

  private class P_LocaleListener implements ILocaleListener {
    @Override
    public void localeChanged(LocaleChangeEvent event) {
      final Locale locale = event.getLocale();
      if (!CompareUtility.equals(getJsonSession().currentHttpRequest().getLocale(), locale)) {
        m_localeManagedByModel = true;
        getJsonSession().currentJsonResponse().addActionEvent("localeChanged", getId(), localeToJson(locale));
      }
    }
  }

}
