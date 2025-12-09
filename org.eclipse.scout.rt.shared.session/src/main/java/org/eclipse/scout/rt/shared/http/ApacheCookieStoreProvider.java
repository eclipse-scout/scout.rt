/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.http;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.ISession;

public class ApacheCookieStoreProvider implements ICookieStoreProvider {

  @Override
  public CookieStore provideDefault() {
    return BEANS.get(ApacheMultiSessionCookieStore.class);
  }

  @Override
  public CookieStore provideSessionSpecific() {
    ISession currentSession = ISession.CURRENT.get();
    if (currentSession != null) {
      return new SpecificSessionCookieStore(currentSession, provideDefault());
    }
    else {
      return provideDefault();
    }
  }
}
