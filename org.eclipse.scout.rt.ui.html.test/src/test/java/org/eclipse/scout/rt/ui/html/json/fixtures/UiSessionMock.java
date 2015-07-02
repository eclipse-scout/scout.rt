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
import org.eclipse.scout.rt.ui.html.json.JsonStartupRequest;

public class UiSessionMock extends UiSession {

  public UiSessionMock() {
  }

  @Override
  public void init(HttpServletRequest httpRequest, JsonStartupRequest jsonStartupRequest) {
    // NOP
  }

  @Override
  public IClientSession getClientSession() {
    return TestEnvironmentClientSession.get();
  }
}
