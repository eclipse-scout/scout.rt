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
package org.eclipse.scout.rt.ui.html.json.fixtures;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.ui.html.UiSession;

public class UiSessionMock extends UiSession {

  public UiSessionMock() {
    setCurrentJsonResponseInternal(createJsonStartupResponse());
    setClientSessionInternal(TestEnvironmentClientSession.get());
    setUiSessionIdInternal("UI:Session:Mock");
  }
}
