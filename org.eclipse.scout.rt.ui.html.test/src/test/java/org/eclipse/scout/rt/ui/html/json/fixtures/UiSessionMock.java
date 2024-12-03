/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
      A adapter = (A) MainJsonObjectFactory.get().createJsonAdapter(model, this, id, parent);
      // The new adapter automatically registered itself in the registry (see constructor of AbstractJsonAdapter).
      // To prevent the "real" instance (instead of the spy) from being returned when resolving the adapter ID, we
      // have to manually update the registry.
      unregisterJsonAdapter(adapter);
      A adapterSpy = spy(adapter);
      registerJsonAdapter(adapterSpy);
      adapterSpy.init();
      return adapterSpy;
    }
    return super.newJsonAdapter(model, parent);
  }
}
