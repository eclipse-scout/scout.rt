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
package org.eclipse.scout.rt.ui.html.json.fixtures;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.mockito.Mockito;

public class JsonSessionMock extends AbstractJsonSession {
  private JsonClientSession m_jsonClientSession;
  private JsonDesktopMock m_jsonDesktopMock;

  public JsonSessionMock() {
    init(null, null);
  }

  @Override
  public void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest) {
    m_jsonClientSession = Mockito.mock(JsonClientSession.class);
    m_jsonDesktopMock = new JsonDesktopMock(Mockito.mock(IDesktop.class), this, createUniqueIdFor(null), m_jsonClientSession);
//    JsonAdapterMock<Object> jsonDesktop = new JsonAdapterMock<Object>(new Object(), this, createUniqueIdFor(null), m_jsonClientSession)
    Mockito.when(m_jsonClientSession.getJsonDesktop()).thenReturn(m_jsonDesktopMock);
//    Mockito.when(jsonDesktop.getJsonSession()).thenReturn(this);

    // For a fully initialized jsonSession use TestEnvironmentJsonSession
  }

  @SuppressWarnings("unchecked")
  @Override
  public JsonClientSession getJsonClientSession() {
    return m_jsonClientSession;
  }

  @Override
  protected IClientSession createClientSession() {
    return null;
  }

  @Override
  public IClientSession getClientSession() {
    return TestEnvironmentClientSession.get();
  }

}
