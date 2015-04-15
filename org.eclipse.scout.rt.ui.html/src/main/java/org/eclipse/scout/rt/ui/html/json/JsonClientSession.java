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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.ILocaleListener;
import org.eclipse.scout.rt.client.LocaleChangeEvent;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.json.JSONObject;

public class JsonClientSession<T extends IClientSession> extends AbstractJsonAdapter<T> {

  private ILocaleListener m_localeListener;
  private boolean m_started;
  private JsonDesktop<IDesktop> m_jsonDesktop;

  public JsonClientSession(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_started = false;
  }

  @Override
  public String getObjectType() {
    // Currently there is no representation on client side
    return null;
  }

  public void startUp() {
    Assertions.assertTrue(ModelJobs.isModelThread(), "startUp() must be run in model job context");

    // TODO BSH Why is this attached before startSession?
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      getModel().addLocaleListener(m_localeListener);
    }
    if (!getModel().isActive()) {
      // FIXME CGU: copied from session service. Moved here to be able to attach locale listener first
      getModel().startSession();
    }

    if (getModel().getLoadError() != null) {
      throw new JsonException(getModel().getLoadError());
    }

    // attach child adapters - we cannot do this in attachModel() as normal
    // since the desktop is not yet created when attachModel runs.
    // see UiSession#init()
    m_jsonDesktop = attachAdapter(getModel().getDesktop());

    if (!getModel().getDesktop().isOpened()) {
      getModel().getDesktop().getUIFacade().fireDesktopOpenedFromUI();
    }
    if (!getModel().getDesktop().isGuiAvailable()) {
      getModel().getDesktop().getUIFacade().fireGuiAttached();
    }

    m_started = true;
  }

  public boolean isStarted() {
    return m_started;
  }

  @Override
  public JsonDesktop<IDesktop> getJsonDesktop() {
    return m_jsonDesktop;
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
    return json;
  }

  private class P_LocaleListener implements ILocaleListener {

    @Override
    public void localeChanged(LocaleChangeEvent event) {
      if (!isStarted()) {
        // If Locale changes during session startup (execLoadSession) it is not necessary to notify the GUI
        return;
      }
      getUiSession().sendLocaleChangedEvent(event.getLocale());
    }
  }
}
