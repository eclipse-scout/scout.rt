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
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * Provider for {@link CookieStore} implementation which is capable to store cookies by Scout session.
 * The {@link IMultiSessionCookieStoreProvider} implementation is optional at runtime and only available in the Scout application using sessions.
 */
@ApplicationScoped
public interface IMultiSessionCookieStoreProvider {

  /**
   * @return {@link CookieStore} which is able to separately handle cookies for multiple sessions.
   * The session is determined by the actual {@link RunContext} performing the HTTP request.
   */
  CookieStore provideDynamic();

  /**
   * @return {@link CookieStore} wrapper, which is able to separately handle cookies for multiple sessions using the given {@code store} as cookie container.
   * The session is determined by the actual {@link RunContext} creating the {@link CookieStore} and independent of the {@link RunContext} performing the actual HTTP request.
   */
  CookieStore provideCurrentSession(CookieStore store);
}
