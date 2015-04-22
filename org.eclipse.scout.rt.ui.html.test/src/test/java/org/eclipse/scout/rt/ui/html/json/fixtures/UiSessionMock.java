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
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.mockito.Mockito;

public class UiSessionMock extends UiSession {
  private JsonClientSession m_jsonClientSession;
  private JsonDesktop m_jsonDesktopMock;

  public UiSessionMock() {
    init(null, null);
  }

  @Override
  public void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest) {
    m_jsonClientSession = Mockito.mock(JsonClientSession.class);
    m_jsonDesktopMock = Mockito.mock(JsonDesktop.class);
    Mockito.when(m_jsonClientSession.getJsonDesktop()).thenReturn(m_jsonDesktopMock);

    // For a fully initialized uiSession use TestEnvironmentUiSession
  }

  @SuppressWarnings("unchecked")
  @Override
  public JsonClientSession getJsonClientSession() {
    return m_jsonClientSession;
  }

  @Override
  public IClientSession getClientSession() {
    return TestEnvironmentClientSession.get();
  }
}
