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
import org.eclipse.scout.rt.ui.html.json.AbstractJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonClientSession;
import org.eclipse.scout.rt.ui.html.json.JsonEventProcessor;
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;
import org.eclipse.scout.rt.ui.html.json.desktop.JsonDesktop;
import org.mockito.Mockito;

public class JsonSessionMock extends AbstractJsonSession {
  private JsonClientSession m_jsonClientSession;
  private JsonDesktop m_jsonDesktopMock;
  private JsonEventProcessor m_jsonEventProcessor;

  public JsonSessionMock() {
    init(null, null);
  }

  @Override
  public void init(HttpServletRequest request, JsonStartupRequest jsonStartupRequest) {
    m_jsonClientSession = Mockito.mock(JsonClientSession.class);
    m_jsonDesktopMock = Mockito.mock(JsonDesktop.class);
    Mockito.when(m_jsonClientSession.getJsonDesktop()).thenReturn(m_jsonDesktopMock);
    m_jsonEventProcessor = new JsonEventProcessor(this);

    // For a fully initialized jsonSession use TestEnvironmentJsonSession
  }

  @SuppressWarnings("unchecked")
  @Override
  public JsonClientSession getJsonClientSession() {
    return m_jsonClientSession;
  }

  @Override
  public JsonEventProcessor getJsonEventProcessor() {
    return m_jsonEventProcessor;
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
