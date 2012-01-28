package org.eclipse.scout.testing.client.servicetunnel.http;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;

/**
 * HTTP cookie store implementation that distinguishes between different {@link IClientSession} connecting concurrently
 * to the same backend (i.e. same URL).
 */
public class MultiClientSessionCookieStore implements CookieStore {

  private final Object m_cookieStoreLock = new Object();
  private final Map<IClientSession, CookieStore> m_cookieStores;
  private final CookieStore m_defaultCookieStore;

  public MultiClientSessionCookieStore() {
    m_cookieStores = new HashMap<IClientSession, CookieStore>();
    m_defaultCookieStore = new CookieManager().getCookieStore();
  }

  @Override
  public void add(URI uri, HttpCookie cookie) {
    getDelegate().add(uri, cookie);
  }

  @Override
  public List<HttpCookie> get(URI uri) {
    return getDelegate().get(uri);
  }

  @Override
  public List<HttpCookie> getCookies() {
    return getDelegate().getCookies();
  }

  @Override
  public List<URI> getURIs() {
    return getDelegate().getURIs();
  }

  @Override
  public boolean remove(URI uri, HttpCookie cookie) {
    return getDelegate().remove(uri, cookie);
  }

  @Override
  public boolean removeAll() {
    return getDelegate().removeAll();
  }

  private CookieStore getDelegate() {
    IClientSession currentSession = ClientJob.getCurrentSession();
    if (currentSession == null) {
      // Bugzilla 369115 changed the behavior of ClientJob.getCurrentSession, so that it is not using
      // the currently executed job anymore for determining the client session. This fix provides the
      // old functionality.
      Job currentJob = Job.getJobManager().currentJob();
      if (currentJob instanceof IClientSessionProvider) {
        currentSession = ((IClientSessionProvider) currentJob).getClientSession();
      }
    }
    if (currentSession == null) {
      return m_defaultCookieStore;
    }
    synchronized (m_cookieStoreLock) {
      CookieStore cookieStore = m_cookieStores.get(currentSession);
      if (cookieStore == null) {
        cookieStore = new CookieManager().getCookieStore();
        m_cookieStores.put(currentSession, cookieStore);
      }
      return cookieStore;
    }
  }

  public void sessionStopped(IClientSession clientSession) {
    synchronized (m_cookieStoreLock) {
      m_cookieStores.remove(clientSession);
    }
  }
}
