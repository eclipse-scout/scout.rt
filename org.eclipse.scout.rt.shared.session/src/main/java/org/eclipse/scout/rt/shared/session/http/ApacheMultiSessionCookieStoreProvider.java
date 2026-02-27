/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.session.http;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.http.IMultiSessionCookieStoreProvider;
import org.eclipse.scout.rt.shared.session.ISession;

/**
 * {@link IMultiSessionCookieStoreProvider} implementation providing {@link ApacheMultiSessionCookieStore} instances.
 */
@Order(4900)
public class ApacheMultiSessionCookieStoreProvider implements IMultiSessionCookieStoreProvider {

  @Override
  public CookieStore provideDynamic() {
    return newCookieStore();
  }

  @Override
  public CookieStore provideCurrentSession(CookieStore store) {
    ISession currentSession = ISession.CURRENT.get();
    if (currentSession != null) {
      return BEANS.get(SpecificSessionCookieStore.class)
          .withSession(currentSession)
          .withDefaultCookieStore(store);
    }
    return store;
  }

  protected CookieStore newCookieStore() {
    return BEANS.get(ApacheMultiSessionCookieStore.class);
  }
}
