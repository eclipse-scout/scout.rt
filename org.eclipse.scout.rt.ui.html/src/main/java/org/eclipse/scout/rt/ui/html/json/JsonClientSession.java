/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json;

import java.util.Locale;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.json.JSONObject;

public class JsonClientSession<CLIENT_SESSION extends IClientSession> extends AbstractJsonPropertyObserver<CLIENT_SESSION> {

  public JsonClientSession(CLIENT_SESSION model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
  }

  @Override
  public String getObjectType() {
    // Currently there is no representation on client side
    return null;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getDesktop());
  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    putAdapterIdProperty(json, "desktop", getModel().getDesktop());
    return json;
  }

  @Override
  protected void handleModelPropertyChange(String propertyName, Object oldValue, Object newValue) {
    if (IClientSession.PROP_LOCALE.equals(propertyName)) {
      getUiSession().sendLocaleChangedEvent((Locale) newValue);
    }
    else {
      super.handleModelPropertyChange(propertyName, oldValue, newValue);
    }
  }
}
