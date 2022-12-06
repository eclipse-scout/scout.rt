/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.ui.html.json.fixtures;

import static org.mockito.Mockito.spy;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.MainJsonObjectFactory;

public class UiSessionMock extends UiSession {

  private boolean m_spyOnJsonAdapter;

  public UiSessionMock() {
    setCurrentJsonResponseInternal(createJsonStartupResponse());
    setClientSessionInternal(TestEnvironmentClientSession.get());
    setUiSessionIdInternal("UI:Session:Mock");
  }

  public void setSpyOnJsonAdapter(boolean spyOnJsonAdapter) {
    m_spyOnJsonAdapter = spyOnJsonAdapter;
  }

  public boolean isSpyOnJsonAdapter() {
    return m_spyOnJsonAdapter;
  }

  @Override
  protected <M, A extends IJsonAdapter<M>> A newJsonAdapter(M model, IJsonAdapter<?> parent) {
    if (isSpyOnJsonAdapter()) {
      String id = createUniqueId();
      @SuppressWarnings("unchecked")
      A adapter = (A) spy(MainJsonObjectFactory.get().createJsonAdapter(model, this, id, parent));
      adapter.init();
      return adapter;
    }
    return super.newJsonAdapter(model, parent);
  }
}
