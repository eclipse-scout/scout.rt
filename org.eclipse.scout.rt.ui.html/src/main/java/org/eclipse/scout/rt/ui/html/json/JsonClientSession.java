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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

public class JsonClientSession<T extends IClientSession> extends AbstractJsonAdapter<T> {

  private PropertyChangeListener m_localeListener;

  public JsonClientSession(T model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    // Currently there is no representation on client side
    return null;
  }

  @Override
  protected void attachModel() {
    if (m_localeListener == null) {
      m_localeListener = new P_LocaleListener();
      getModel().addPropertyChangeListener(IClientSession.PROP_LOCALE, m_localeListener);
    }
  }

  @Override
  protected void attachChildAdapters() {
    attachAdapter(getModel().getDesktop());
  }

  @Override
  protected void detachModel() {
    if (m_localeListener != null) {
      getModel().removePropertyChangeListener(m_localeListener);
      m_localeListener = null;
    }
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "desktop", getModel().getDesktop());
    return json;
  }

  private class P_LocaleListener implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      getUiSession().sendLocaleChangedEvent((Locale) evt.getNewValue());
    }

  }
}
