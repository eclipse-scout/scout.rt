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

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.session.ISession;

/**
 * Cookie store which wraps a {@link ApacheMultiSessionCookieStore} implementation, invoking all operations using the specified
 * {@link ISession}. May be used if async threads access the cookie store where {@link ISession#CURRENT} is not set
 * correctly.
 */
@Bean
public class SpecificSessionCookieStore implements CookieStore {

  protected ISession m_session;
  protected CookieStore m_defaultCookieStore;

  public ISession getSession() {
    return m_session;
  }

  public SpecificSessionCookieStore withSession(ISession session) {
    m_session = session;
    return this;
  }

  public CookieStore getDefaultCookieStore() {
    return m_defaultCookieStore;
  }

  public SpecificSessionCookieStore withDefaultCookieStore(CookieStore defaultCookieStore) {
    m_defaultCookieStore = defaultCookieStore;
    return this;
  }

  @Override
  public void addCookie(Cookie cookie) {
    runWithSession(() -> getDefaultCookieStore().addCookie(cookie));
  }

  @Override
  public List<Cookie> getCookies() {
    return callWithSession(getDefaultCookieStore()::getCookies);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean clearExpired(Date date) {
    return callWithSession(() -> getDefaultCookieStore().clearExpired(date));
  }

  @Override
  public void clear() {
    runWithSession(getDefaultCookieStore()::clear);
  }

  protected void runWithSession(IRunnable runnable) {
    callWithSession(() -> {
      runnable.run();
      return null;
    });
  }

  protected <R> R callWithSession(Callable<R> callable) {
    return RunContexts.copyCurrent(true)
        .withThreadLocal(ISession.CURRENT, getSession())
        .call(callable);
  }
}
