/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonAdapterRegistry;
import org.eclipse.scout.rt.ui.html.json.fixtures.JsonAdapterMock;

/**
 * Utility providing access to protected methods of {@link UiSession} - for testing purposes only!
 */
public class UiSessionTestUtility {

  public static void endRequest(UiSession uiSession) {
    uiSession.setCurrentJsonResponseInternal(uiSession.createJsonResponse());
    uiSession.httpContext().clear();
  }

  public static <M, A extends IJsonAdapter<M>> A newJsonAdapter(UiSession uiSession, M model, IJsonAdapter<?> parent) {
    return uiSession.newJsonAdapter(model, parent);
  }

  /**
   * Creates a new {@link IJsonAdapter} for the given model using the {@link JsonAdapterMock} as parent adapter.
   */
  public static <M, A extends IJsonAdapter<M>> A newJsonAdapter(UiSession uiSession, M model) {
    return uiSession.newJsonAdapter(model, new JsonAdapterMock());
  }

  public static JsonAdapterRegistry getJsonAdapterRegistry(UiSession session) {
    return session.jsonAdapterRegistry();
  }

  public static HttpSession getHttpSession(UiSession session) {
    return session.sessionStore().getHttpSession();
  }
}
